package com.hyejin.portfolio.asset.application.service;

import com.hyejin.portfolio.asset.application.port.in.GetAssetFeatureUseCase;
import com.hyejin.portfolio.asset.application.port.in.SearchAssetsUseCase;
import com.hyejin.portfolio.asset.domain.Asset;
import com.hyejin.portfolio.asset.domain.AssetType;
import com.hyejin.portfolio.asset.domain.ExposureTag;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class MockAssetCatalogService implements SearchAssetsUseCase, GetAssetFeatureUseCase {
    private static final Currency KRW = Currency.getInstance("KRW");
    private static final Currency USD = Currency.getInstance("USD");

    private final List<Asset> assets = List.of(
        asset("00000000-0000-0000-0000-000000000101", "005930", "Samsung Electronics", "KRX", AssetType.STOCK, KRW,
            List.of("samsung", "samsung electronics", "삼성전자"),
            List.of(ExposureTag.KOREA_EQUITY, ExposureTag.KRW_LISTING, ExposureTag.SEMICONDUCTOR_CYCLE, ExposureTag.AI_CAPEX),
            "0.04", "0.11", "0.18", "0.24", "0.22", "0.28", "-0.26", "1.0", 12,
            "AI memory and semiconductor-cycle recovery",
            List.of("memory price recovery weakens for two consecutive checks", "AI server demand proxy turns lower")),
        asset("00000000-0000-0000-0000-000000000102", "000660", "SK Hynix", "KRX", AssetType.STOCK, KRW,
            List.of("hynix", "sk hynix", "하이닉스"),
            List.of(ExposureTag.KOREA_EQUITY, ExposureTag.KRW_LISTING, ExposureTag.SEMICONDUCTOR_CYCLE, ExposureTag.AI_CAPEX),
            "0.06", "0.18", "0.30", "0.45", "0.30", "0.36", "-0.33", "1.0", 9,
            "HBM demand and memory-cycle momentum",
            List.of("HBM order momentum slows", "memory-cycle proxy rolls over")),
        asset("00000000-0000-0000-0000-000000000103", "KO", "Coca-Cola", "NYSE", AssetType.STOCK, USD,
            List.of("coca cola", "coca-cola", "ko", "코카콜라"),
            List.of(ExposureTag.US_EQUITY, ExposureTag.USD_EXPOSURE, ExposureTag.DEFENSIVE_CONSUMER),
            "0.01", "0.03", "0.05", "0.08", "0.10", "0.13", "-0.12", "1.0", 18,
            "defensive cash-flow stability",
            List.of("defensive premium becomes stretched", "USD/KRW move dominates KRW return")),
        asset("00000000-0000-0000-0000-000000000104", "TIGER-SP500", "TIGER US S&P500 ETF", "KRX", AssetType.ETF, KRW,
            List.of("tiger", "s&p500", "sp500", "미국 s&p500", "미국 s&p500 tiger etf"),
            List.of(ExposureTag.KRW_LISTING, ExposureTag.US_EQUITY, ExposureTag.USD_EXPOSURE, ExposureTag.BROAD_US_MARKET, ExposureTag.MEGA_CAP_TECH, ExposureTag.RISK_APPETITE),
            "0.03", "0.08", "0.16", "0.27", "0.16", "0.19", "-0.18", "1.0", 12,
            "US large-cap earnings and dollar exposure",
            List.of("US mega-cap breadth narrows", "KRW strengthens enough to dilute US equity gains")),
        asset("00000000-0000-0000-0000-000000000105", "QQQ", "Invesco QQQ ETF", "NASDAQ", AssetType.ETF, USD,
            List.of("qqq", "nasdaq 100", "나스닥"),
            List.of(ExposureTag.US_EQUITY, ExposureTag.USD_EXPOSURE, ExposureTag.MEGA_CAP_TECH, ExposureTag.AI_CAPEX, ExposureTag.RISK_APPETITE),
            "0.04", "0.12", "0.22", "0.34", "0.21", "0.25", "-0.24", "1.0", 9,
            "US mega-cap technology earnings momentum",
            List.of("mega-cap earnings revisions weaken", "AI capex expectations decline")),
        asset("00000000-0000-0000-0000-000000000106", "SOXL", "Direxion Daily Semiconductor Bull 3X Shares", "NYSEARCA", AssetType.ETF, USD,
            List.of("soxl", "semiconductor 3x", "반도체 3배"),
            List.of(ExposureTag.US_EQUITY, ExposureTag.USD_EXPOSURE, ExposureTag.SEMICONDUCTOR_CYCLE, ExposureTag.AI_CAPEX, ExposureTag.HIGH_VOLATILITY, ExposureTag.LEVERAGED_PRODUCT, ExposureTag.RISK_APPETITE),
            "0.10", "0.31", "0.62", "0.95", "0.78", "0.92", "-0.68", "3.0", 3,
            "short tactical semiconductor momentum",
            List.of("semiconductor momentum reverses", "position is held beyond the tactical review window")),
        asset("00000000-0000-0000-0000-000000000107", "KRW-BTC", "Bitcoin", "UPBIT", AssetType.CRYPTO, KRW,
            List.of("bitcoin", "btc", "비트코인"),
            List.of(ExposureTag.CRYPTO_LIQUIDITY, ExposureTag.RISK_APPETITE, ExposureTag.HIGH_VOLATILITY),
            "0.07", "0.21", "0.38", "0.74", "0.56", "0.64", "-0.52", "1.0", 6,
            "crypto liquidity and risk-appetite cycle",
            List.of("Bitcoin falls more than 20 percent from proposal price", "correlation with US equities rises while volatility remains high")),
        asset("00000000-0000-0000-0000-000000000108", "KRW-ETH", "Ethereum", "UPBIT", AssetType.CRYPTO, KRW,
            List.of("ethereum", "eth", "이더리움"),
            List.of(ExposureTag.CRYPTO_LIQUIDITY, ExposureTag.RISK_APPETITE, ExposureTag.HIGH_VOLATILITY),
            "0.05", "0.18", "0.29", "0.58", "0.60", "0.70", "-0.57", "1.0", 6,
            "crypto platform activity and liquidity cycle",
            List.of("Ethereum underperforms Bitcoin during risk-on period", "network activity proxy weakens"))
    );

    @Override
    public List<Asset> search(String query) {
        if (query == null || query.isBlank()) {
            return assets;
        }
        var normalized = query.toLowerCase(Locale.ROOT);
        return assets.stream()
            .filter(asset -> matches(asset, normalized))
            .toList();
    }

    @Override
    public Asset getFeature(UUID assetId) {
        return findFeature(assetId).orElseThrow(() -> new IllegalArgumentException("asset not found: " + assetId));
    }

    @Override
    public Optional<Asset> findFeature(UUID assetId) {
        return assets.stream().filter(asset -> asset.assetId().equals(assetId)).findFirst();
    }

    private boolean matches(Asset asset, String query) {
        return asset.symbol().toLowerCase(Locale.ROOT).contains(query)
            || asset.displayName().toLowerCase(Locale.ROOT).contains(query)
            || asset.aliases().stream().anyMatch(alias -> alias.toLowerCase(Locale.ROOT).contains(query));
    }

    private static Asset asset(
        String assetId,
        String symbol,
        String displayName,
        String market,
        AssetType assetType,
        Currency currency,
        List<String> aliases,
        List<ExposureTag> exposureTags,
        String return1m,
        String return3m,
        String return6m,
        String return1y,
        String volatility30d,
        String volatility90d,
        String maxDrawdown1y,
        String leverageMultiplier,
        int momentumWindowMonths,
        String catalyst,
        List<String> reviewTriggers
    ) {
        return new Asset(
            UUID.fromString(assetId),
            symbol,
            displayName,
            market,
            assetType,
            currency,
            aliases,
            exposureTags,
            new BigDecimal(return1m),
            new BigDecimal(return3m),
            new BigDecimal(return6m),
            new BigDecimal(return1y),
            new BigDecimal(volatility30d),
            new BigDecimal(volatility90d),
            new BigDecimal(maxDrawdown1y),
            new BigDecimal(leverageMultiplier),
            momentumWindowMonths,
            catalyst,
            reviewTriggers
        );
    }
}
