package wickedmonkstudio.exchangerates.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.Map;

/**
 * Created by Wojciech on 19.05.2017.
 */
public class ExchangeRates {
    private String baseCurrency;
    private Date date;
    private Map<String, Double> rates;

   public ExchangeRates(){
       this(null, null, null);
   }

    public ExchangeRates(String base, Date date, Map<String, Double> rates) {
        this.baseCurrency = base;
        this.date = date;
        this.rates = rates;
    }

    public String getBase() {
        return baseCurrency;
    }

    public void setBase(String base) {
        this.baseCurrency = base;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
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
