package com.hyejin.portfolio.simulation.domain;

import java.math.BigDecimal;

public record CapitalGrowthPoint(
    int month,
    BigDecimal cumulativePrincipal,
    BigDecimal expectedValue,
    BigDecimal expectedProfit
) {
    public CapitalGrowthPoint {
        if (month < 0) {
            throw new IllegalArgumentException("month must not be negative");
        }
        if (cumulativePrincipal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("principal must not be negative");
        }
    }
}
