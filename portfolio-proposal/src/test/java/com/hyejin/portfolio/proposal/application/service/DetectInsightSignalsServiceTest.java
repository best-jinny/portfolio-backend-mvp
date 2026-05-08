package com.hyejin.portfolio.proposal.application.service;

import com.hyejin.portfolio.proposal.domain.InsightSignalType;
import com.hyejin.portfolio.proposal.domain.ProposalAssetSnapshot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DetectInsightSignalsServiceTest {
    private final AnalyzePortfolioExposuresService analyzer = new AnalyzePortfolioExposuresService();
    private final DetectInsightSignalsService detector = new DetectInsightSignalsService();

    @Test
    void detectsOverlappingSemiconductorExposureForSamsungAndSoxl() {
        var assets = List.of(
            snapshot("005930", "Samsung Electronics", "SEMICONDUCTOR_CYCLE"),
            snapshot("SOXL", "SOXL", "SEMICONDUCTOR_CYCLE", "LEVERAGED_PRODUCT", "HIGH_VOLATILITY")
        );

        var signals = detector.detect(analyzer.analyze(assets));

        assertThat(signals).extracting("type").contains(InsightSignalType.OVERLAPPING_EXPOSURE, InsightSignalType.TACTICAL_PRODUCT_MISUSE);
    }

    @Test
    void detectsFalseDiversificationForSp500AndBitcoin() {
        var assets = List.of(
            snapshot("TIGER-SP500", "TIGER US S&P500 ETF", "BROAD_US_MARKET", "RISK_APPETITE"),
            snapshot("KRW-BTC", "Bitcoin", "CRYPTO_LIQUIDITY", "RISK_APPETITE", "HIGH_VOLATILITY")
        );

        var signals = detector.detect(analyzer.analyze(assets));

        assertThat(signals).extracting("type").contains(InsightSignalType.FALSE_DIVERSIFICATION);
    }

    private ProposalAssetSnapshot snapshot(String symbol, String displayName, String... exposureTags) {
        return new ProposalAssetSnapshot(
            UUID.randomUUID(),
            symbol,
            displayName,
            List.of(exposureTags),
            symbol.equals("SOXL") ? new BigDecimal("3.0") : BigDecimal.ONE,
            6,
            "mock catalyst",
            List.of("mock review trigger")
        );
    }
}
