package wickedmonkstudio.exchangerates.view;

import javafx.application.Platform;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.util.Pair;
import org.apache.http.NameValuePair;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import wickedmonkstudio.exchangerates.event.ExchangeRateEvent;
import wickedmonkstudio.exchangerates.event.ExchangeRateListener;
import wickedmonkstudio.exchangerates.model.ExchangeRates;
import wickedmonkstudio.exchangerates.model.GraphDrawerTask;
import wickedmonkstudio.exchangerates.model.MinMaxRecord;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Controller {


    @FXML private AnchorPane mainPane;
    @FXML private TextArea logArea;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button generateChartButton;
     private LineChart<String,Number> ratesChart;
     private HashMap<String, XYChart.Series> dataSeries = new HashMap<>();
     private HashMap<String, MinMaxRecord> currencyRecords = new HashMap<>();
     private GraphDrawerTask graphDrawerTask;



    CategoryAxis xAxis = new CategoryAxis();
    NumberAxis yAxis = new NumberAxis();

    private LocalDate startDate;
    private LocalDate endDate;


    private String baseCurrency = "EUR";
    private String exchangeCurrency = "PLN, USD";

    @FXML public void initialize(){
        ratesChart= new LineChart<String, Number>(xAxis, yAxis);
        xAxis.setLabel("Exchange rates");
        yAxis.setLabel("Value according to base currency");
        ratesChart.setPrefWidth(mainPane.getPrefWidth());
        mainPane.getChildren().add(ratesChart);
        ratesChart.setTitle("Exchange rates over time");


        endDatePicker.setValue(LocalDate.now());
        generateChartButton.setOnAction(event -> {
            startDate=startDatePicker.getValue();
            endDate=endDatePicker.getValue();
            graphDrawerTask=new GraphDrawerTask(this);
            graphDrawerTask.addListener(exchangeRateListener);
            graphDrawerTask.setOnSucceeded( event1 -> {
                logArea.appendText("\n--------------------------------\n");
            });
           new Thread(graphDrawerTask).start();
        });

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
                logArea.appendText("Base currency: "+baseCurrency);
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
