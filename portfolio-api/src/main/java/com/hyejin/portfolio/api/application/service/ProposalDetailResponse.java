package com.hyejin.portfolio.api.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProposalDetailResponse(
    UUID proposalId,
    String status,
    String title,
    String summary,
    int recommendedHorizonMonths,
    HorizonRationaleResponse horizonRationale,
    List<SignalResponse> signals,
    List<ActionResponse> actions,
    List<AllocationResponse> allocations,
    List<CapitalGrowthPointResponse> capitalGrowth
) {
    public record AllocationResponse(UUID assetId, BigDecimal initialWeight, String role) {
    }

    public record HorizonRationaleResponse(
        int recommendedHorizonMonths,
        int reviewAfterMonths,
        String userExplanation,
        List<String> primaryDrivers,
        List<String> reviewTriggers
    ) {
    }

    public record SignalResponse(
        String type,
        String severity,
        String title,
        String userExplanation,
        String dataBasis,
        List<String> affectedSymbols
    ) {
    }

    public record ActionResponse(
        String actionType,
        String title,
        String userExplanation,
        String reviewTrigger,
        List<String> affectedSymbols,
        List<UUID> evidenceIds
    ) {
    }

    public record CapitalGrowthPointResponse(
        int month,
        BigDecimal cumulativePrincipal,
        BigDecimal expectedValue,
        BigDecimal expectedProfit
    ) {
    }
}
