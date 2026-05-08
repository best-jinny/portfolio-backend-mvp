package com.hyejin.portfolio.proposal.application.service;

import com.hyejin.portfolio.common.Money;
import com.hyejin.portfolio.proposal.adapter.out.InMemoryPortfolioIntentRepository;
import com.hyejin.portfolio.proposal.application.port.in.CreatePortfolioIntentUseCase;
import com.hyejin.portfolio.proposal.domain.ProposalAssetSnapshot;
import com.hyejin.portfolio.proposal.domain.ProposalMode;
import com.hyejin.portfolio.proposal.domain.RiskProfile;
import com.hyejin.portfolio.proposal.domain.SelectedAsset;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CreatePortfolioIntentServiceTest {
    @Test
    void createsAndReadsIntentWithSelectedAssetSnapshots() {
        var repository = new InMemoryPortfolioIntentRepository();
        var service = new CreatePortfolioIntentService(repository);
        var selectedAsset = new SelectedAsset(
            UUID.fromString("00000000-0000-0000-0000-000000000104"),
            "TIGER-SP500",
            "TIGER US S&P500 ETF",
            "ETF",
            "KRX",
            Currency.getInstance("KRW"),
            "broad US equity core",
            1,
            new ProposalAssetSnapshot(
                UUID.fromString("00000000-0000-0000-0000-000000000104"),
                "TIGER-SP500",
                "TIGER US S&P500 ETF",
                List.of("BROAD_US_MARKET", "RISK_APPETITE"),
                BigDecimal.ONE,
                6,
                "US large-cap earnings and dollar exposure",
                List.of("US mega-cap breadth narrows")
            )
        );

        var intent = service.create(new CreatePortfolioIntentUseCase.Command(
            new Money(new BigDecimal("10000000"), Currency.getInstance("KRW")),
            new Money(new BigDecimal("1000000"), Currency.getInstance("KRW")),
            RiskProfile.GROWTH,
            Currency.getInstance("KRW"),
            ProposalMode.CYCLE_MOMENTUM,
            List.of(selectedAsset)
        ));

        var loaded = repository.get(intent.id());

        assertThat(loaded.id()).isEqualTo(intent.id());
        assertThat(loaded.selectedAssets()).extracting("symbol").containsExactly("TIGER-SP500");
    }
}
