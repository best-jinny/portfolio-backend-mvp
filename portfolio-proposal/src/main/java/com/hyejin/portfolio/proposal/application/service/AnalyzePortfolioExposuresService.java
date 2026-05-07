package com.hyejin.portfolio.proposal.application.service;

import com.hyejin.portfolio.asset.domain.Asset;
import com.hyejin.portfolio.asset.domain.ExposureTag;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.EnumMap;
import java.util.List;

public class AnalyzePortfolioExposuresService {
    public PortfolioExposureSummary analyze(List<Asset> assets) {
        if (assets == null || assets.isEmpty()) {
            throw new IllegalArgumentException("assets must not be empty");
        }
        var equalWeight = BigDecimal.ONE.divide(BigDecimal.valueOf(assets.size()), MathContext.DECIMAL64);
        var tagExposure = new EnumMap<ExposureTag, BigDecimal>(ExposureTag.class);
        var leverageAdjustedExposure = BigDecimal.ZERO;
        var hasHighVolatility = false;
        var hasStabilizer = false;

        for (var asset : assets) {
            var adjustedWeight = equalWeight.multiply(asset.leverageMultiplier(), MathContext.DECIMAL64);
            leverageAdjustedExposure = leverageAdjustedExposure.add(adjustedWeight);
            for (var tag : asset.exposureTags()) {
                tagExposure.merge(tag, adjustedWeight, BigDecimal::add);
            }
            hasHighVolatility = hasHighVolatility || asset.exposureTags().contains(ExposureTag.HIGH_VOLATILITY);
            hasStabilizer = hasStabilizer || asset.exposureTags().contains(ExposureTag.DEFENSIVE_CONSUMER);
        }

        return new PortfolioExposureSummary(List.copyOf(assets), tagExposure, leverageAdjustedExposure, hasHighVolatility, hasStabilizer);
    }
}
