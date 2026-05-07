package com.hyejin.portfolio.proposal.application.service;

import com.hyejin.portfolio.asset.application.service.MockAssetCatalogService;
import com.hyejin.portfolio.proposal.adapter.out.InMemoryPortfolioProposalRepository;
import com.hyejin.portfolio.proposal.application.port.in.CreatePortfolioProposalUseCase;
import com.hyejin.portfolio.proposal.domain.ProposalStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CreatePortfolioProposalServiceTest {
    @Test
    void createsCompletedProposalWithNonGenericSignalsActionsAndHorizon() {
        var catalog = new MockAssetCatalogService();
        var repository = new InMemoryPortfolioProposalRepository();
        var service = new CreatePortfolioProposalService(
            catalog,
            repository,
            new AnalyzePortfolioExposuresService(),
            new DetectInsightSignalsService(),
            new GenerateProposalActionsService()
        );

        var proposal = service.create(new CreatePortfolioProposalUseCase.Command(
            UUID.randomUUID(),
            List.of(
                catalog.search("s&p500").getFirst().assetId(),
                catalog.search("bitcoin").getFirst().assetId()
            )
        ));

        assertThat(proposal.status()).isEqualTo(ProposalStatus.COMPLETED);
        assertThat(proposal.signals()).isNotEmpty();
        assertThat(proposal.actions()).isNotEmpty();
        assertThat(proposal.horizonRationale().recommendedHorizonMonths()).isEqualTo(9);
        assertThat(proposal.summary()).contains("risk-taking environment");
    }
}
