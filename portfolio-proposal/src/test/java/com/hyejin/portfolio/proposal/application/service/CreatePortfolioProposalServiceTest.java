package com.hyejin.portfolio.proposal.application.service;

import com.hyejin.portfolio.common.Money;
import com.hyejin.portfolio.proposal.adapter.out.InMemoryPortfolioIntentRepository;
import com.hyejin.portfolio.proposal.adapter.out.InMemoryPortfolioProposalRepository;
import com.hyejin.portfolio.proposal.application.port.in.CreatePortfolioIntentUseCase;
import com.hyejin.portfolio.proposal.application.port.in.CreatePortfolioProposalUseCase;
import com.hyejin.portfolio.proposal.domain.ProposalAssetSnapshot;
import com.hyejin.portfolio.proposal.domain.ProposalMode;
import com.hyejin.portfolio.proposal.domain.ProposalStatus;
import com.hyejin.portfolio.proposal.domain.RiskProfile;
import com.hyejin.portfolio.proposal.domain.SelectedAsset;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CreatePortfolioProposalServiceTest {
    @Test
    void createsCompletedProposalFromPersistedIntentWithoutDependingOnAssetModule() {
        var intentRepository = new InMemoryPortfolioIntentRepository();
        var proposalRepository = new InMemoryPortfolioProposalRepository();
        var intentService = new CreatePortfolioIntentService(intentRepository);
        var service = new CreatePortfolioProposalService(
            intentRepository,
            proposalRepository,
            new AnalyzePortfolioExposuresService(),
            new DetectInsightSignalsService(),
            new GenerateProposalActionsService()
        );
        var intent = intentService.create(new CreatePortfolioIntentUseCase.Command(
            new Money(new BigDecimal("10000000"), Currency.getInstance("KRW")),
            new Money(new BigDecimal("1000000"), Currency.getInstance("KRW")),
            RiskProfile.GROWTH,
            Currency.getInstance("KRW"),
            ProposalMode.CYCLE_MOMENTUM,
            List.of(
                selected("TIGER-SP500", "TIGER US S&P500 ETF", 1, "BROAD_US_MARKET", "RISK_APPETITE"),
                selected("KRW-BTC", "Bitcoin", 2, "CRYPTO_LIQUIDITY", "RISK_APPETITE", "HIGH_VOLATILITY")
            )
        ));

        var proposal = service.create(new CreatePortfolioProposalUseCase.Command(intent.id()));

        assertThat(proposal.status()).isEqualTo(ProposalStatus.COMPLETED);
        assertThat(proposal.signals()).isNotEmpty();
        assertThat(proposal.actions()).isNotEmpty();
        assertThat(proposal.horizonRationale().recommendedHorizonMonths()).isEqualTo(9);
        assertThat(proposal.summary()).contains("risk-taking environment");
    }

    private SelectedAsset selected(String symbol, String displayName, int displayOrder, String... exposureTags) {
        return new SelectedAsset(
            UUID.randomUUID(),
            symbol,
            displayName,
            "MOCK",
            "MOCK",
            Currency.getInstance("KRW"),
            null,
            displayOrder,
            new ProposalAssetSnapshot(
                UUID.randomUUID(),
                symbol,
                displayName,
                List.of(exposureTags),
                BigDecimal.ONE,
                symbol.equals("KRW-BTC") ? 12 : 6,
                "mock catalyst",
                List.of("mock review trigger")
            )
        );
    }
}
