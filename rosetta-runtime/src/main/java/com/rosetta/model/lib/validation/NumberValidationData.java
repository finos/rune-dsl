package com.rosetta.model.lib.validation;

import java.math.BigDecimal;
import java.util.Optional;

public class NumberValidationData extends ValidationData{
    private Optional<BigDecimal> min;
    private Optional<BigDecimal> max;
    private Optional<Integer> digits;
    private Optional<Integer> fractionalDigits;
    private BigDecimal actual;

    public NumberValidationData(Optional<BigDecimal> min, Optional<BigDecimal> max, Optional<Integer> digits, Optional<Integer> fractionalDigits, BigDecimal actual) {
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

    public Optional<Integer> getDigits() {
        return digits;
    }

    public Optional<Integer> getFractionalDigits() {
        return fractionalDigits;
    }

    public BigDecimal getActual() {
        return actual;
    }
}
