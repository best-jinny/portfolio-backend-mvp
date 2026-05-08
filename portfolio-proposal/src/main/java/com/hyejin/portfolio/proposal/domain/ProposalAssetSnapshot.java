package com.hyejin.portfolio.proposal.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record ProposalAssetSnapshot(
    UUID assetId,
    String symbol,
    String displayName,
    List<String> exposureTags,
    BigDecimal leverageMultiplier,
    int momentumWindowMonths,
    String catalyst,
    List<String> reviewTriggers
) {
    public ProposalAssetSnapshot {
        Objects.requireNonNull(assetId, "assetId must not be null");
        Objects.requireNonNull(symbol, "symbol must not be null");
        Objects.requireNonNull(displayName, "displayName must not be null");
        Objects.requireNonNull(leverageMultiplier, "leverageMultiplier must not be null");
        exposureTags = List.copyOf(exposureTags == null ? List.of() : exposureTags);
        reviewTriggers = List.copyOf(reviewTriggers == null ? List.of() : reviewTriggers);
    }
}
