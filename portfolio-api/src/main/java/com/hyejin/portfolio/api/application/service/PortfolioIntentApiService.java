package com.hyejin.portfolio.api.application.service;

import com.hyejin.portfolio.asset.application.port.in.GetAssetFeatureUseCase;
import com.hyejin.portfolio.asset.domain.Asset;
import com.hyejin.portfolio.common.Money;
import com.hyejin.portfolio.proposal.application.port.in.CreatePortfolioIntentUseCase;
import com.hyejin.portfolio.proposal.domain.PortfolioIntent;
import com.hyejin.portfolio.proposal.domain.ProposalAssetSnapshot;
import com.hyejin.portfolio.proposal.domain.ProposalMode;
import com.hyejin.portfolio.proposal.domain.RiskProfile;
import com.hyejin.portfolio.proposal.domain.SelectedAsset;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class PortfolioIntentApiService {
    private final CreatePortfolioIntentUseCase createPortfolioIntentUseCase;
    private final GetAssetFeatureUseCase getAssetFeatureUseCase;

    public PortfolioIntentApiService(
        CreatePortfolioIntentUseCase createPortfolioIntentUseCase,
        GetAssetFeatureUseCase getAssetFeatureUseCase
    ) {
        this.createPortfolioIntentUseCase = createPortfolioIntentUseCase;
        this.getAssetFeatureUseCase = getAssetFeatureUseCase;
    }

    public PortfolioIntent create(CreateIntentCommand command) {
        var baseCurrency = Currency.getInstance("KRW");
        var selectedAssets = selectedAssets(command.assets());
        return createPortfolioIntentUseCase.create(new CreatePortfolioIntentUseCase.Command(
            new Money(command.availableCash(), baseCurrency),
            new Money(command.monthlyContribution(), baseCurrency),
            RiskProfile.valueOf(command.riskProfile()),
            baseCurrency,
            ProposalMode.CYCLE_MOMENTUM,
            selectedAssets
        ));
    }

    private List<SelectedAsset> selectedAssets(List<CreateIntentAssetCommand> assets) {
        if (assets == null || assets.isEmpty()) {
            throw new IllegalArgumentException("assets must not be empty");
        }
        for (int index = 0; index < assets.size(); index++) {
            if (assets.get(index).assetId() == null) {
                throw new IllegalArgumentException("assetId must not be null");
            }
        }
        return IntStream.range(0, assets.size())
            .mapToObj(index -> {
                var asset = assets.get(index);
                return toSelectedAsset(getAssetFeatureUseCase.getFeature(asset.assetId()), asset.thesis(), index + 1);
            })
            .toList();
    }

    private SelectedAsset toSelectedAsset(Asset asset, String thesis, int displayOrder) {
        return new SelectedAsset(
            asset.assetId(),
            asset.symbol(),
            asset.displayName(),
            asset.assetType().name(),
            asset.market(),
            asset.currency(),
            thesis,
            displayOrder,
            new ProposalAssetSnapshot(
                asset.assetId(),
                asset.symbol(),
                asset.displayName(),
                asset.exposureTags().stream().map(Enum::name).toList(),
                asset.leverageMultiplier(),
                asset.momentumWindowMonths(),
                asset.catalyst(),
                asset.reviewTriggers()
            )
        );
    }

    public record CreateIntentCommand(
        BigDecimal availableCash,
        BigDecimal monthlyContribution,
        String riskProfile,
        List<CreateIntentAssetCommand> assets
    ) {
    }

    public record CreateIntentAssetCommand(UUID assetId, String thesis) {
    }
}
