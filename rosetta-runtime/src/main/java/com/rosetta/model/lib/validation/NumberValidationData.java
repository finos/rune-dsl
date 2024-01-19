package com.rosetta.model.lib.validation;

import java.math.BigDecimal;
import java.util.Optional;

public class NumberValidationData extends ValidationData{
    private Optional<BigDecimal> min;
    private Optional<BigDecimal> max;
    private int digits;
    private int fractionalDigits;
    private BigDecimal actual;

    public NumberValidationData(Optional<BigDecimal> min, Optional<BigDecimal> max, int digits, int fractionalDigits, BigDecimal actual) {
        this.min = min;
        this.max = max;
        this.digits = digits;
        this.fractionalDigits = fractionalDigits;
        this.actual = actual;
    }

    public Optional<BigDecimal> getMin() {
        return min;
    }

    public Optional<BigDecimal> getMax() {
        return max;
    }

    public int getDigits() {
        return digits;
    }

    public int getFractionalDigits() {
        return fractionalDigits;
    }

    public BigDecimal getActual() {
        return actual;
    }
}
