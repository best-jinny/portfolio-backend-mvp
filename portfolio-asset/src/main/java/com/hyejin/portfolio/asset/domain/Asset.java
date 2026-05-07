package com.hyejin.portfolio.asset.domain;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record Asset(
    UUID assetId,
    String symbol,
    String displayName,
    String market,
    AssetType assetType,
    Currency currency,
    List<String> aliases,
    List<ExposureTag> exposureTags,
    BigDecimal return1m,
    BigDecimal return3m,
    BigDecimal return6m,
    BigDecimal return1y,
    BigDecimal volatility30d,
    BigDecimal volatility90d,
    BigDecimal maxDrawdown1y,
    BigDecimal leverageMultiplier,
    int momentumWindowMonths,
    String catalyst,
    List<String> reviewTriggers
) {
    public Asset {
        Objects.requireNonNull(assetId, "assetId must not be null");
        Objects.requireNonNull(symbol, "symbol must not be null");
        Objects.requireNonNull(displayName, "displayName must not be null");
        Objects.requireNonNull(market, "market must not be null");
        Objects.requireNonNull(assetType, "assetType must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        aliases = List.copyOf(aliases == null ? List.of() : aliases);
        exposureTags = List.copyOf(exposureTags == null ? List.of() : exposureTags);
        reviewTriggers = List.copyOf(reviewTriggers == null ? List.of() : reviewTriggers);
    }
}
