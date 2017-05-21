package wickedmonkstudio.exchangerates.event;

import wickedmonkstudio.exchangerates.model.ExchangeRates;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.EventObject;
import java.util.Map;

/**
 * Created by Wojciech on 20.05.2017.
 */
public class ExchangeRateEvent extends EventObject{
    private String baseCurrency;
    private LocalDate date;
    private Map<String, Double> rates;


    public ExchangeRateEvent(Object source, ExchangeRates data){
        super(source);
        this.baseCurrency=data.getBase();
        this.date=data.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        this.rates=data.getRates();
    }

    public String getBase() {
        return baseCurrency;
    }

    public void setBase(String base) {
        this.baseCurrency = base;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Map<String, Double> getRates() {
        return rates;
    }

    public void setRates(Map<String, Double> rates) {
        this.rates = rates;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append("Exchange rates:\n")
                .append("base:")
                .append(this.baseCurrency)
                .append("\ndate: ")
                .append(this.date.toString())
                .append("rates: \n{")
                .append(rates.toString());
        return builder.toString();
    }
}
