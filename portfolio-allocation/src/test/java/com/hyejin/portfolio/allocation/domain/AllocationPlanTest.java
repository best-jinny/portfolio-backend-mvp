package com.hyejin.portfolio.allocation.domain;

import com.hyejin.portfolio.common.Percentage;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AllocationPlanTest {
    @Test
    void initialWeightsMustSumToOne() {
        assertThatThrownBy(() -> new AllocationPlan(
            UUID.randomUUID(),
            UUID.randomUUID(),
            12,
            List.of(
                new ProposedAllocation(UUID.randomUUID(), Percentage.of(new BigDecimal("0.40")), null, "core", "r1"),
                new ProposedAllocation(UUID.randomUUID(), Percentage.of(new BigDecimal("0.40")), null, "satellite", "r2")
            )
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
