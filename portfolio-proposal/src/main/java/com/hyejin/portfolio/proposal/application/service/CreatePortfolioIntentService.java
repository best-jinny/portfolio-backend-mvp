package com.hyejin.portfolio.proposal.application.service;

import com.hyejin.portfolio.proposal.application.port.in.CreatePortfolioIntentUseCase;
import com.hyejin.portfolio.proposal.application.port.out.SavePortfolioIntentPort;
import com.hyejin.portfolio.proposal.domain.PortfolioIntent;

public class CreatePortfolioIntentService implements CreatePortfolioIntentUseCase {
    private final SavePortfolioIntentPort savePortfolioIntentPort;

    public CreatePortfolioIntentService(SavePortfolioIntentPort savePortfolioIntentPort) {
        this.savePortfolioIntentPort = savePortfolioIntentPort;
    }

    @Override
    public PortfolioIntent create(Command command) {
        var intent = PortfolioIntent.create(
            command.availableCash(),
            command.monthlyContribution(),
            command.riskProfile(),
            command.baseCurrency(),
            command.proposalMode(),
            command.selectedAssets()
        );
        return savePortfolioIntentPort.save(intent);
    }
}
