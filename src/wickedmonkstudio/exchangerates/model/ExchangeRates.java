package wickedmonkstudio.exchangerates.model;

import java.util.Date;
import java.util.Map;

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

    public Date getDate() {
        return date;
    }

    public Map<String, Double> getRates() {
        return rates;
    }

    @Override
    public String toString() {
        return "Exchange rates:\n" +
                "base:" +
                this.baseCurrency +
                "\ndate: " +
                this.date.toString() +
                "rates: \n{" +
                rates.toString();
    }

}
