package com.hyejin.portfolio.asset.application.port.in;

import com.hyejin.portfolio.asset.domain.Asset;

import java.util.List;

public interface SearchAssetsUseCase {
    List<Asset> search(String query);
}
