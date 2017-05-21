package wickedmonkstudio.exchangerates.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.TilePane;
import javafx.util.Pair;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import wickedmonkstudio.exchangerates.event.ExchangeRateEvent;
import wickedmonkstudio.exchangerates.event.ExchangeRateListener;
import wickedmonkstudio.exchangerates.model.ExchangeRates;
import wickedmonkstudio.exchangerates.model.FixerIORequestTask;
import wickedmonkstudio.exchangerates.model.MinMaxRecord;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class Controller {

    @FXML private AnchorPane mainPane;
    @FXML private TextArea logArea;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button generateChartButton;
    @FXML private TilePane selectCurrencysTilePane;
    @FXML private ComboBox<String> baseCurrencyComboBox;

     private LineChart<String,Number> ratesChart;
     private HashMap<String, XYChart.Series> dataSeries = new HashMap<>();
     private HashMap<String, MinMaxRecord> currencyRecords = new HashMap<>();
     private FixerIORequestTask fixerIORequestTask;

    private TreeSet<String> availableCurrencySet = new TreeSet<>();

    CategoryAxis xAxis = new CategoryAxis();
    NumberAxis yAxis = new NumberAxis();

    private LocalDate startDate;
    private LocalDate endDate;


    private String baseCurrency;
    private String exchangeCurrency;

    @FXML public void initialize(){
        initAvailableCurrencyList();
        initCurrencyCheckBoxes();
        initBaseCurrencyComboBox();


        ratesChart= new LineChart<String, Number>(xAxis, yAxis);
        xAxis.setLabel("Exchange rates");
        yAxis.setLabel("Value according to base currency");
        ratesChart.setPrefWidth(mainPane.getPrefWidth());
        mainPane.getChildren().add(ratesChart);
        ratesChart.setTitle("Exchange rates over time");
        ratesChart.setCreateSymbols(false);


        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        generateChartButton.setOnAction(event -> {

            exchangeCurrency = getSelectedCurrency();
            logArea.setText("Collecting data, please wait...");
            startDate=startDatePicker.getValue();
            endDate=endDatePicker.getValue();
            fixerIORequestTask =new FixerIORequestTask(this);
            fixerIORequestTask.addListener(exchangeRateListener);
           new Thread(fixerIORequestTask).start();
        });

    }

    private void initBaseCurrencyComboBox() {
        baseCurrencyComboBox.setItems(FXCollections.observableArrayList(availableCurrencySet));
        baseCurrencyComboBox.setOnAction(e -> {
            baseCurrency = baseCurrencyComboBox.getValue();
        });
    }

    private String getSelectedCurrency() {
        StringBuilder builder = new StringBuilder();
        for(Node node : selectCurrencysTilePane.getChildren()){
            if(node instanceof CheckBox && ((CheckBox) node).isSelected()) {
                if(builder.length()>0)
                    builder.append(", ");
                builder.append(((CheckBox) node).getText());
            }
        }
        return builder.toString();
    }

    private void initCurrencyCheckBoxes() {
        Iterator iterator = availableCurrencySet.iterator();
        while (iterator.hasNext()){
            CheckBox box = new CheckBox(iterator.next().toString());
            if(box.getText().equals("PLN"))
                box.setSelected(true);
            selectCurrencysTilePane.getChildren().add(box);
        }
    }

    private void initAvailableCurrencyList() {
        ExchangeRates request = getFixerIODefaultRequest();
        availableCurrencySet.add(request.getBase());
        for(Map.Entry<String, Double> entry : request.getRates().entrySet())
                availableCurrencySet.add(entry.getKey());
    }

    private ExchangeRates getFixerIODefaultRequest() {
        InputStreamReader inputStreamReader = null;

        URL url = null;
        try {
            url = new URL("http://api.fixer.io/latest");
            URLConnection urlConnection = url.openConnection();
            inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(inputStreamReader, ExchangeRates.class);
        }catch (UnknownHostException e){
            logArea.appendText("\nCannot connect to server. Check your Internet connection");
            System.out.println("\nCannot connect to server. Check your Internet connection");
            HashMap<String, Double> defaultCurrency = new HashMap<>();
            defaultCurrency.put("EUR", null);
            defaultCurrency.put("GBP", null);
            defaultCurrency.put("USD", null);
            defaultCurrency.put("PLN", null);
            return new ExchangeRates("PLN", Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()), defaultCurrency);
        }
        catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStreamReader != null)
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }

    private ExchangeRateListener exchangeRateListener = new ExchangeRateListener() {
        @Override
        public void handleGraphDrawning(ExchangeRateEvent event) {
            Platform.runLater(() -> {
            for(Map.Entry<String, Double> entry : event.getRates().entrySet()) {
                if (!getDataSeries().containsKey(entry.getKey())) {
                    XYChart.Series<String, Number> newSeries = new XYChart.Series<>();
                    newSeries.setName(entry.getKey());
                    getDataSeries().put(entry.getKey(), newSeries);
                    getRatesChart().getData().add(newSeries);
                }
                getDataSeries().get(entry.getKey()).getData()
                        .add(new XYChart.Data<>(event.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), entry.getValue()));
            }
            });
        }

        @Override
        public void raportExchangeRates() {
            Platform.runLater(() -> {
                logArea.setText("\nBase currency: "+baseCurrency);
                for(Map.Entry<String, MinMaxRecord> entry : currencyRecords.entrySet()){
                    logArea.appendText("\nCurrency: "+entry.getKey());
                    logArea.appendText("\n\tMinimal rate: "+entry.getValue().getMinimalValue().getValue() + " at " + entry.getValue().getMinimalValue().getKey());
                    logArea.appendText("\n\tMaximal rate: "+entry.getValue().getMaximalValue().getValue() + " at " + entry.getValue().getMaximalValue().getKey());
                }
            });
        }

        @Override
        public void handleExchangeRecordValues(ExchangeRateEvent event) {
            for(Map.Entry<String, Double> entry : event.getRates().entrySet()){
                if(!currencyRecords.containsKey(entry.getKey())){
                    currencyRecords.put(entry.getKey(), new MinMaxRecord(
                            new Pair<LocalDate, Double>(event.getDate(), entry.getValue()),
                            new Pair<LocalDate, Double>(event.getDate(), entry.getValue())));
                }else {
                    if(entry.getValue() > currencyRecords.get(entry.getKey()).getMaximalValue().getValue())
                        currencyRecords.get(entry.getKey()).setMaximalValue(new Pair<>(event.getDate(), entry.getValue()));
                    if(entry.getValue() < currencyRecords.get(entry.getKey()).getMinimalValue().getValue())
                        currencyRecords.get(entry.getKey()).setMinimalValue(new Pair<>(event.getDate(), entry.getValue()));
                }

            }
        }
    };

    public LineChart<String, Number> getRatesChart() {
        return ratesChart;
    }

    public HashMap<String, XYChart.Series> getDataSeries() {

        return dataSeries;
    }

    public TextArea getLogArea() {
        return logArea;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getExchangeCurrency() {
        return exchangeCurrency;
    }
}
