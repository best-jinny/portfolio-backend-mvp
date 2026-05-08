package com.hyejin.portfolio.proposal.application.port.in;

import com.hyejin.portfolio.proposal.domain.PortfolioProposal;

import java.util.UUID;

public interface CreatePortfolioProposalUseCase {
    PortfolioProposal create(Command command);

    record Command(UUID intentId) {
    }
}
