package wickedmonkstudio.exchangerates.event;

import java.util.EventListener;

/**
 * Created by Wojciech on 20.05.2017.
 */
public interface ExchangeRateListener extends EventListener {
    void handleGraphDrawning(ExchangeRateEvent event);
//    void logExchangeRate(ExchangeRateEvent event);
    void raportExchangeRates();
    void handleExchangeRecordValues(ExchangeRateEvent event);

}
