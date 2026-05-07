package com.hyejin.portfolio.proposal.application.service;

import com.hyejin.portfolio.asset.domain.ExposureTag;
import com.hyejin.portfolio.proposal.domain.InsightSignal;
import com.hyejin.portfolio.proposal.domain.InsightSignalType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DetectInsightSignalsService {
    public List<InsightSignal> detect(PortfolioExposureSummary summary) {
        var signals = new ArrayList<InsightSignal>();

        if (exposure(summary, ExposureTag.SEMICONDUCTOR_CYCLE).compareTo(new BigDecimal("0.50")) >= 0) {
            signals.add(new InsightSignal(
                InsightSignalType.OVERLAPPING_EXPOSURE,
                "HIGH",
                "Semiconductor cycle is doing more work than it first appears",
                "Several selected assets depend on semiconductor momentum. The issue is not Korea versus US exposure; it is that the same semiconductor-cycle view can drive multiple holdings at once.",
                "Leverage-adjusted semiconductor exposure is " + exposure(summary, ExposureTag.SEMICONDUCTOR_CYCLE).toPlainString(),
                symbolsWith(summary, ExposureTag.SEMICONDUCTOR_CYCLE)
            ));
        }

        if (exposure(summary, ExposureTag.RISK_APPETITE).compareTo(new BigDecimal("0.60")) >= 0
            && exposure(summary, ExposureTag.CRYPTO_LIQUIDITY).compareTo(BigDecimal.ZERO) > 0
            && exposure(summary, ExposureTag.BROAD_US_MARKET).compareTo(BigDecimal.ZERO) > 0) {
            signals.add(new InsightSignal(
                InsightSignalType.FALSE_DIVERSIFICATION,
                "MEDIUM",
                "The crypto position may not be diversifying the US equity ETF",
                "This portfolio looks diversified by label, but the S&P500 ETF and Bitcoin can both depend on the same risk-taking environment. If they move together, Bitcoin increases the same market-mood bet instead of offsetting it.",
                "Mock correlation profile marks S&P500 ETF and Bitcoin as risk-appetite linked",
                symbolsWith(summary, ExposureTag.RISK_APPETITE)
            ));
        }

        if (exposure(summary, ExposureTag.LEVERAGED_PRODUCT).compareTo(BigDecimal.ZERO) > 0) {
            signals.add(new InsightSignal(
                InsightSignalType.TACTICAL_PRODUCT_MISUSE,
                "HIGH",
                "A leveraged product should not silently become the core holding",
                "SOXL is useful only if the user intentionally wants a short tactical semiconductor bet. If it is treated like a normal ETF, the portfolio can become much more concentrated than the raw weight suggests.",
                "Mock leverage profile uses a 3.0x multiplier for SOXL",
                symbolsWith(summary, ExposureTag.LEVERAGED_PRODUCT)
            ));
        }

        if (summary.hasHighVolatilityAsset() && !summary.hasStabilizer()) {
            signals.add(new InsightSignal(
                InsightSignalType.MISSING_STABILIZER,
                "MEDIUM",
                "The portfolio is missing a role that behaves differently",
                "The selected assets lean toward growth, cycle, liquidity, or high price swings. A complement should be chosen for a different role, not because adding another asset automatically improves diversification.",
                "No selected asset has defensive-consumer or stabilizer mock tags",
                List.of()
            ));
        }

        return List.copyOf(signals);
    }

    private BigDecimal exposure(PortfolioExposureSummary summary, ExposureTag tag) {
        return summary.tagExposure().getOrDefault(tag, BigDecimal.ZERO);
    }

    private List<String> symbolsWith(PortfolioExposureSummary summary, ExposureTag tag) {
        return summary.assets().stream()
            .filter(asset -> asset.exposureTags().contains(tag))
            .map(asset -> asset.symbol())
            .toList();
    }
}
