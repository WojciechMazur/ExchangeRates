package wickedmonkstudio.exchangerates.model;

import javafx.util.Pair;

import java.time.LocalDate;

/**
 * Created by Wojciech on 21.05.2017.
 */
public class MinMaxRecord {
    private Pair<LocalDate, Double> minimalValue;
    private Pair<LocalDate, Double> maximalValue;

    public MinMaxRecord(Pair<LocalDate, Double> minimalValue, Pair<LocalDate, Double> maximalValue) {
        this.minimalValue = minimalValue;
        this.maximalValue = maximalValue;
    }

    public Pair<LocalDate, Double> getMinimalValue() {
        return minimalValue;
    }

    public void setMinimalValue(Pair<LocalDate, Double> minimalValue) {
        this.minimalValue = minimalValue;
    }

    public Pair<LocalDate, Double> getMaximalValue() {
        return maximalValue;
    }

    public void setMaximalValue(Pair<LocalDate, Double> maximalValue) {
        this.maximalValue = maximalValue;
    }
}
