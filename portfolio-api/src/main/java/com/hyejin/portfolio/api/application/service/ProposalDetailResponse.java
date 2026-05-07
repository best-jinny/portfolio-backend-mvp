package com.hyejin.portfolio.api.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProposalDetailResponse(
    UUID proposalId,
    String status,
    String title,
    int recommendedHorizonMonths,
    List<AllocationResponse> allocations,
    List<CapitalGrowthPointResponse> capitalGrowth
) {
    public record AllocationResponse(UUID assetId, BigDecimal initialWeight, String role) {
    }

    public record CapitalGrowthPointResponse(
        int month,
        BigDecimal cumulativePrincipal,
        BigDecimal expectedValue,
        BigDecimal expectedProfit
    ) {
    }
}
