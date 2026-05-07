package com.hyejin.portfolio.allocation.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AllocationPlan(
    UUID id,
    UUID proposalId,
    int recommendedHorizonMonths,
    List<ProposedAllocation> allocations
) {
    public AllocationPlan {
        if (recommendedHorizonMonths <= 0) {
            throw new IllegalArgumentException("recommended horizon must be positive");
        }
        if (allocations == null || allocations.isEmpty()) {
            throw new IllegalArgumentException("allocations must not be empty");
        }
        var sum = allocations.stream()
            .map(allocation -> allocation.initialWeight().value())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(BigDecimal.ONE) != 0) {
            throw new IllegalArgumentException("initial weights must sum to 1");
        }
        allocations = List.copyOf(allocations);
    }
}
