package com.hyejin.portfolio.proposal.application.port.in;

import com.hyejin.portfolio.proposal.domain.PortfolioProposal;

import java.util.UUID;

public interface GetPortfolioProposalUseCase {
    PortfolioProposal get(UUID proposalId);
}
