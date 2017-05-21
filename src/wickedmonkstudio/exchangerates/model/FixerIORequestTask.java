package wickedmonkstudio.exchangerates.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import wickedmonkstudio.exchangerates.event.ExchangeRateEvent;
import wickedmonkstudio.exchangerates.event.ExchangeRateListener;
import wickedmonkstudio.exchangerates.view.Controller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FixerIORequestTask extends Task {
    private Controller controller = null;
    private ArrayList<ExchangeRateListener> exchangeRateListenerArrayList = new ArrayList<>();

    private ObjectMapper objectMapper = new ObjectMapper();
    private InputStreamReader inputStreamReader;

    public FixerIORequestTask(Controller controller){
        this.controller=controller;
        this.controller.getDataSeries().clear();
        this.controller.getRatesChart().getData().clear();

    }

    @Override
    protected Object call() throws Exception {
        for(LocalDate date = controller.getStartDate(); !controller.getEndDate().equals(date.plusDays(1));date=date.plusDays(1)){
            ExchangeRates rates = getExchangeRate(date, controller.getBaseCurrency(), controller.getExchangeCurrency());
            ExchangeRateEvent current = new ExchangeRateEvent(this, rates);
            processRequstedExchangeRate(current);

            if(isCancelled())
                break;
        }

        processExchangeRatesReport();

        return "Finished";
    }

   private void processRequstedExchangeRate(ExchangeRateEvent event) {
        ArrayList<ExchangeRateListener> listenersCopy;
        if(exchangeRateListenerArrayList.size()==0)
            return;
        listenersCopy=(ArrayList<ExchangeRateListener>) exchangeRateListenerArrayList.clone();

        for(ExchangeRateListener listener : listenersCopy){
            listener.handleGraphDrawning(event);
            listener.handleExchangeRecordValues(event);
        }
    }

    private void processExchangeRatesReport(){
        ArrayList<ExchangeRateListener> listenersCopy;
        if(exchangeRateListenerArrayList.size()==0)
            return;
        listenersCopy=(ArrayList<ExchangeRateListener>) exchangeRateListenerArrayList.clone();

        for(ExchangeRateListener listener : listenersCopy){
            listener.raportExchangeRates();
        }
    }

    public void addListener(ExchangeRateListener listener){
        if(!exchangeRateListenerArrayList.contains(listener))
            exchangeRateListenerArrayList.add(listener);
    }

    public void removeListener(ExchangeRateListener listener){
        if(exchangeRateListenerArrayList.contains(listener))
            exchangeRateListenerArrayList.remove(listener);
    }

    private ExchangeRates getExchangeRate(LocalDate date, String base, String symbols) throws IOException {
        try {
            int status;
            HttpURLConnection httpURLConnection;
            URL url = buildURL(date, base, symbols).toURL();
            do {
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                if((status=httpURLConnection.getResponseCode())==429)
                    Thread.sleep(50);
            }while (status== 429);
            inputStreamReader= new InputStreamReader(httpURLConnection.getInputStream());
            return objectMapper.readValue(inputStreamReader, ExchangeRates.class);
        } catch (MalformedURLException | URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(inputStreamReader!=null)
                inputStreamReader.close();
        }
        return null;
    }

    private URI buildURL(LocalDate date, String base, String symbols) throws URISyntaxException {
        List<NameValuePair> nameValuePairList = new ArrayList<>(1);
        if(base!=null && base.length()>0)
            nameValuePairList.add(new BasicNameValuePair("base", base));
        if(symbols!=null && symbols.length()>0)
            nameValuePairList.add(new BasicNameValuePair("symbols", symbols));

        URIBuilder uriBuilder = new URIBuilder()
                .setScheme("http")
                .setHost("api.fixer.io")
                .setPath(date.format(DateTimeFormatter.ofPattern("/yyyy-MM-dd")))
                .setParameters(nameValuePairList);
        return uriBuilder.build();
    }

}
