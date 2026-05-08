package com.hyejin.portfolio.proposal.application.service;

import com.hyejin.portfolio.proposal.domain.ProposalAssetSnapshot;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record PortfolioExposureSummary(
    List<ProposalAssetSnapshot> assets,
    Map<String, BigDecimal> tagExposure,
    BigDecimal leverageAdjustedExposure,
    boolean hasHighVolatilityAsset,
    boolean hasStabilizer
) {
}
