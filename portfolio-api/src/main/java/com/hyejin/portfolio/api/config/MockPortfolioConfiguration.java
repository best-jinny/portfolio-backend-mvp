package com.hyejin.portfolio.api.config;

import com.hyejin.portfolio.asset.application.service.MockAssetCatalogService;
import com.hyejin.portfolio.evidence.application.service.MockEvidenceDetailService;
import com.hyejin.portfolio.proposal.adapter.out.InMemoryPortfolioIntentRepository;
import com.hyejin.portfolio.proposal.adapter.out.InMemoryPortfolioProposalRepository;
import com.hyejin.portfolio.proposal.application.service.AnalyzePortfolioExposuresService;
import com.hyejin.portfolio.proposal.application.service.CreatePortfolioIntentService;
import com.hyejin.portfolio.proposal.application.service.CreatePortfolioProposalService;
import com.hyejin.portfolio.proposal.application.service.DetectInsightSignalsService;
import com.hyejin.portfolio.proposal.application.service.GenerateProposalActionsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MockPortfolioConfiguration {
    @Bean
    MockAssetCatalogService mockAssetCatalogService() {
        return new MockAssetCatalogService();
    }

    @Bean
    InMemoryPortfolioIntentRepository inMemoryPortfolioIntentRepository() {
        return new InMemoryPortfolioIntentRepository();
    }

    @Bean
    CreatePortfolioIntentService createPortfolioIntentService(InMemoryPortfolioIntentRepository repository) {
        return new CreatePortfolioIntentService(repository);
    }

    @Bean
    InMemoryPortfolioProposalRepository inMemoryPortfolioProposalRepository() {
        return new InMemoryPortfolioProposalRepository();
    }

    @Bean
    CreatePortfolioProposalService createPortfolioProposalService(
        InMemoryPortfolioIntentRepository intentRepository,
        InMemoryPortfolioProposalRepository repository
    ) {
        return new CreatePortfolioProposalService(
            intentRepository,
            repository,
            new AnalyzePortfolioExposuresService(),
            new DetectInsightSignalsService(),
            new GenerateProposalActionsService()
        );
    }

    @Bean
    MockEvidenceDetailService mockEvidenceDetailService() {
        return new MockEvidenceDetailService();
    }
}
