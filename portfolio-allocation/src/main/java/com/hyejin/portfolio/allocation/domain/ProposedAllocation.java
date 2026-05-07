package com.hyejin.portfolio.allocation.domain;

import com.hyejin.portfolio.common.Percentage;

import java.util.UUID;

public record ProposedAllocation(
    UUID assetId,
    Percentage initialWeight,
    Percentage monthlyWeight,
    String role,
    String rationaleAnchor
) {
}
