package com.hyejin.portfolio.asset.application.service;

import com.hyejin.portfolio.asset.domain.ExposureTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MockAssetCatalogServiceTest {
    @Test
    void searchesAssetsByAliasSymbolAndDisplayName() {
        var service = new MockAssetCatalogService();

        var samsung = service.search("samsung");
        var tiger = service.search("s&p500");
        var bitcoin = service.search("bitcoin");

        assertThat(samsung).extracting("symbol").contains("005930");
        assertThat(tiger).extracting("symbol").contains("TIGER-SP500");
        assertThat(bitcoin).extracting("symbol").contains("KRW-BTC");
    }

    @Test
    void returnsMockFeatureTagsForSelectedAsset() {
        var service = new MockAssetCatalogService();
        var soxlId = service.search("soxl").getFirst().assetId();

        var feature = service.getFeature(soxlId);

        assertThat(feature.symbol()).isEqualTo("SOXL");
        assertThat(feature.exposureTags()).contains(ExposureTag.SEMICONDUCTOR_CYCLE, ExposureTag.LEVERAGED_PRODUCT);
        assertThat(feature.leverageMultiplier()).isEqualByComparingTo("3.0");
    }

    @Test
    void unknownAssetReturnsEmptyOptional() {
        var service = new MockAssetCatalogService();

        assertThat(service.findFeature(UUID.randomUUID())).isEmpty();
    }
}
