package com.hyejin.portfolio.api.adapter.in.web;

import com.hyejin.portfolio.asset.application.port.in.SearchAssetsUseCase;
import com.hyejin.portfolio.asset.domain.Asset;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    private final SearchAssetsUseCase searchAssetsUseCase;

    public AssetController(SearchAssetsUseCase searchAssetsUseCase) {
        this.searchAssetsUseCase = searchAssetsUseCase;
    }

    @GetMapping("/search")
    public List<AssetSearchResponse> search(@RequestParam String query) {
        return searchAssetsUseCase.search(query).stream()
            .map(AssetSearchResponse::from)
            .toList();
    }

    public record AssetSearchResponse(
        UUID assetId,
        String symbol,
        String displayName,
        String market,
        String assetType,
        String currency
    ) {
        static AssetSearchResponse from(Asset asset) {
            return new AssetSearchResponse(
                asset.assetId(),
                asset.symbol(),
                asset.displayName(),
                asset.market(),
                asset.assetType().name(),
                asset.currency().getCurrencyCode()
            );
        }
    }
}
