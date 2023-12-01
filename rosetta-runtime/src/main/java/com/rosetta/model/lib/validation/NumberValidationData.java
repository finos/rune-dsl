package com.rosetta.model.lib.validation;

import java.math.BigDecimal;

public class NumberValidationData extends ValidationData{
    private BigDecimal min;
    private BigDecimal max;
    private int digits;
    private int fractionalDigits;
    private BigDecimal actual;

    public NumberValidationData(BigDecimal min, BigDecimal max, int digits, int fractionalDigits, BigDecimal actual) {
        this.min = min;
        this.max = max;
        this.digits = digits;
        this.fractionalDigits = fractionalDigits;
        this.actual = actual;
    }

    public BigDecimal getMin() {
        return min;
    }

    public BigDecimal getMax() {
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
