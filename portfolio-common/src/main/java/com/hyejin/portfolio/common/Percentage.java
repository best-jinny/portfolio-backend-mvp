package com.hyejin.portfolio.common;

import java.math.BigDecimal;
import java.util.Objects;

public record Percentage(BigDecimal value) {
    public Percentage {
        Objects.requireNonNull(value, "value must not be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("percentage must be greater than or equal to 0");
        }
        if (value.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("percentage must be less than or equal to 1");
        }
    }

    public static Percentage of(BigDecimal value) {
        return new Percentage(value);
    }
}
