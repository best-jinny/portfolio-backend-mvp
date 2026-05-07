package com.hyejin.portfolio.asset.application.port.in;

import com.hyejin.portfolio.asset.domain.Asset;

import java.util.Optional;
import java.util.UUID;

public interface GetAssetFeatureUseCase {
    Asset getFeature(UUID assetId);

    Optional<Asset> findFeature(UUID assetId);
}
