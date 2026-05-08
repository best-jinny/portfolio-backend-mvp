package com.hyejin.portfolio.api.application.service;

import com.hyejin.portfolio.common.Money;
import com.hyejin.portfolio.proposal.adapter.out.InMemoryPortfolioIntentRepository;
import com.hyejin.portfolio.proposal.adapter.out.InMemoryPortfolioProposalRepository;
import com.hyejin.portfolio.proposal.domain.HorizonRationale;
import com.hyejin.portfolio.proposal.domain.PortfolioIntent;
import com.hyejin.portfolio.proposal.domain.PortfolioProposal;
import com.hyejin.portfolio.proposal.domain.ProposalAssetSnapshot;
import com.hyejin.portfolio.proposal.domain.ProposalMode;
import com.hyejin.portfolio.proposal.domain.ProposalStatus;
import com.hyejin.portfolio.proposal.domain.RiskProfile;
import com.hyejin.portfolio.proposal.domain.SelectedAsset;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProposalQueryServiceTest {
    @Test
    void returnsCompletedMockProposalDetail() {
        var intentRepository = new InMemoryPortfolioIntentRepository();
        var proposalRepository = new InMemoryPortfolioProposalRepository();
        var intent = intentRepository.save(intent());
        var proposal = proposalRepository.save(new PortfolioProposal(
            UUID.randomUUID(),
            intent.id(),
            ProposalStatus.COMPLETED,
            "Mock summary",
            new HorizonRationale(12, 6, "review", List.of("driver"), List.of("trigger")),
            List.of(),
            List.of(),
            Instant.now()
        ));
        var response = new ProposalQueryService(proposalRepository, intentRepository).getProposal(proposal.proposalId());

        assertThat(response.proposalId()).isEqualTo(proposal.proposalId());
        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.allocations()).hasSize(2);
        assertThat(response.capitalGrowth()).extracting("month").contains(0);
    }

    @Test
    void returnsMockEvidenceDetail() {
        var evidenceId = UUID.randomUUID();
        var response = new ProposalQueryService(null, null).getEvidence(evidenceId);

        assertThat(response.evidenceId()).isEqualTo(evidenceId);
        assertThat(response.claimType()).isEqualTo("INTERPRETATION");
    }

    private PortfolioIntent intent() {
        return PortfolioIntent.create(
            new Money(new BigDecimal("10000000"), Currency.getInstance("KRW")),
            new Money(new BigDecimal("1000000"), Currency.getInstance("KRW")),
            RiskProfile.GROWTH,
            Currency.getInstance("KRW"),
            ProposalMode.CYCLE_MOMENTUM,
            List.of(selected("TIGER-SP500", 1), selected("KRW-BTC", 2))
        );
    }

    private SelectedAsset selected(String symbol, int displayOrder) {
        var id = UUID.randomUUID();
        return new SelectedAsset(
            id,
            symbol,
            symbol,
            "MOCK",
            "MOCK",
            Currency.getInstance("KRW"),
            null,
            displayOrder,
            new ProposalAssetSnapshot(
                id,
                symbol,
                symbol,
                List.of("RISK_APPETITE"),
                BigDecimal.ONE,
                12,
                "mock catalyst",
                List.of("mock trigger")
            )
        );
    }
}
