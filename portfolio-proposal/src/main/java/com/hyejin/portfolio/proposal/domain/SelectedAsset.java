package com.hyejin.portfolio.proposal.domain;

import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

public record SelectedAsset(
    UUID assetId,
    String symbol,
    String displayName,
    String assetType,
    String market,
    Currency currency,
    String userThesis,
    int displayOrder
) {
    public SelectedAsset {
        Objects.requireNonNull(assetId, "assetId must not be null");
        Objects.requireNonNull(symbol, "symbol must not be null");
        Objects.requireNonNull(displayName, "displayName must not be null");
        Objects.requireNonNull(assetType, "assetType must not be null");
        Objects.requireNonNull(market, "market must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
    }
}
