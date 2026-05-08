package com.hyejin.portfolio.proposal.application.service;

import com.hyejin.portfolio.proposal.domain.ProposalAssetSnapshot;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.List;

public class AnalyzePortfolioExposuresService {
    public PortfolioExposureSummary analyze(List<ProposalAssetSnapshot> assets) {
        if (assets == null || assets.isEmpty()) {
            throw new IllegalArgumentException("assets must not be empty");
        }
        var equalWeight = BigDecimal.ONE.divide(BigDecimal.valueOf(assets.size()), MathContext.DECIMAL64);
        var tagExposure = new HashMap<String, BigDecimal>();
        var leverageAdjustedExposure = BigDecimal.ZERO;
        var hasHighVolatility = false;
        var hasStabilizer = false;

        for (var asset : assets) {
            var adjustedWeight = equalWeight.multiply(asset.leverageMultiplier(), MathContext.DECIMAL64);
            leverageAdjustedExposure = leverageAdjustedExposure.add(adjustedWeight);
            for (var tag : asset.exposureTags()) {
                tagExposure.merge(tag, adjustedWeight, BigDecimal::add);
            }
            hasHighVolatility = hasHighVolatility || asset.exposureTags().contains("HIGH_VOLATILITY");
            hasStabilizer = hasStabilizer || asset.exposureTags().contains("DEFENSIVE_CONSUMER");
        }

        return new PortfolioExposureSummary(List.copyOf(assets), tagExposure, leverageAdjustedExposure, hasHighVolatility, hasStabilizer);
    }
}
