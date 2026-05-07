package com.hyejin.portfolio.common;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PercentageTest {
    @Test
    void percentageMustBeBetweenZeroAndOne() {
        assertThat(Percentage.of(new BigDecimal("0.25")).value()).isEqualByComparingTo("0.25");

        assertThatThrownBy(() -> Percentage.of(new BigDecimal("-0.01")))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Percentage.of(new BigDecimal("1.01")))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
