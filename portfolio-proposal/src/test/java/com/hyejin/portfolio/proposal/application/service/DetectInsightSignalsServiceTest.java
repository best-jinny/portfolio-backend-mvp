package com.hyejin.portfolio.proposal.application.service;

import com.hyejin.portfolio.asset.application.service.MockAssetCatalogService;
import com.hyejin.portfolio.proposal.domain.InsightSignalType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DetectInsightSignalsServiceTest {
    private final MockAssetCatalogService catalog = new MockAssetCatalogService();
    private final AnalyzePortfolioExposuresService analyzer = new AnalyzePortfolioExposuresService();
    private final DetectInsightSignalsService detector = new DetectInsightSignalsService();

    @Test
    void detectsOverlappingSemiconductorExposureForSamsungAndSoxl() {
        var assets = List.of(
            catalog.search("samsung").getFirst(),
            catalog.search("soxl").getFirst()
        );

        var signals = detector.detect(analyzer.analyze(assets));

        assertThat(signals).extracting("type").contains(InsightSignalType.OVERLAPPING_EXPOSURE, InsightSignalType.TACTICAL_PRODUCT_MISUSE);
    }

    @Test
    void detectsFalseDiversificationForSp500AndBitcoin() {
        var assets = List.of(
            catalog.search("s&p500").getFirst(),
            catalog.search("bitcoin").getFirst()
        );

        var signals = detector.detect(analyzer.analyze(assets));

        assertThat(signals).extracting("type").contains(InsightSignalType.FALSE_DIVERSIFICATION);
    }
}
