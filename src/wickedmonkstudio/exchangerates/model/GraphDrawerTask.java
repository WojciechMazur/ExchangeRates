package wickedmonkstudio.exchangerates.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;
import javafx.scene.chart.XYChart;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import wickedmonkstudio.exchangerates.event.ExchangeRateEvent;
import wickedmonkstudio.exchangerates.event.ExchangeRateListener;
import wickedmonkstudio.exchangerates.view.Controller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Wojciech on 20.05.2017.
 */
public class GraphDrawerTask extends Task {
    Controller controller = null;
    private ArrayList<ExchangeRateListener> exchangeRateListenerArrayList = new ArrayList<>();

    private ObjectMapper objectMapper = new ObjectMapper();
    private InputStreamReader inputStreamReader;

    public GraphDrawerTask(Controller controller){
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

    /**
     * Not used
     * @return
     * @throws IOException
     */
    public ExchangeRates getExchangeRate() throws IOException {
        URL url = null;
        try {
            url = new URL("http://api.fixer.io/latest");
            URLConnection urlConnection = url.openConnection();
            inputStreamReader= new InputStreamReader(urlConnection.getInputStream());
            return objectMapper.readValue(inputStreamReader, ExchangeRates.class);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(inputStreamReader!=null)
                inputStreamReader.close();
        }
        return null;
    }

    /**
     * Not used
     * @param urlString
     * @return
     * @throws IOException
     */
    public ExchangeRates getExchangeRate(String urlString) throws IOException {
        try {
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
            return objectMapper.readValue(inputStreamReader, ExchangeRates.class);
        } finally {
            if (inputStreamReader != null)
                inputStreamReader.close();
        }
    }

    /**
     * Not used
     * @param date
     * @return
     * @throws IOException
     */
    public ExchangeRates getExchangeRate(LocalDate date)throws IOException{
        try{
            URL url = new URL("http://api.fixer.io/"+date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            URLConnection urlConnection = url.openConnection();
            inputStreamReader =  new InputStreamReader(urlConnection.getInputStream());
            return objectMapper.readValue(inputStreamReader, ExchangeRates.class);
        }finally {
            if (inputStreamReader != null)
                inputStreamReader.close();
        }
    }

    public ExchangeRates getExchangeRate(LocalDate date, String base, String symbols) throws IOException {
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
        URI uri = uriBuilder.build();
        return uri;
    }

}
