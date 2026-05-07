package com.hyejin.portfolio.proposal.application.service;

import com.hyejin.portfolio.asset.domain.Asset;
import com.hyejin.portfolio.asset.domain.ExposureTag;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record PortfolioExposureSummary(
    List<Asset> assets,
    Map<ExposureTag, BigDecimal> tagExposure,
    BigDecimal leverageAdjustedExposure,
    boolean hasHighVolatilityAsset,
    boolean hasStabilizer
) {
}
