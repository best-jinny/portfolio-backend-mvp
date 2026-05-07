package com.hyejin.portfolio.proposal.application.port.in;

import com.hyejin.portfolio.common.Money;
import com.hyejin.portfolio.proposal.domain.PortfolioIntent;
import com.hyejin.portfolio.proposal.domain.ProposalMode;
import com.hyejin.portfolio.proposal.domain.RiskProfile;
import com.hyejin.portfolio.proposal.domain.SelectedAsset;

import java.util.Currency;
import java.util.List;

public interface CreatePortfolioIntentUseCase {
    PortfolioIntent create(Command command);

    record Command(
        Money availableCash,
        Money monthlyContribution,
        RiskProfile riskProfile,
        Currency baseCurrency,
        ProposalMode proposalMode,
        List<SelectedAsset> selectedAssets
    ) {
    }
}
