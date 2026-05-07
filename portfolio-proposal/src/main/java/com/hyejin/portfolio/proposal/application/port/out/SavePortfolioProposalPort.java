package com.hyejin.portfolio.proposal.application.port.out;

import com.hyejin.portfolio.proposal.domain.PortfolioProposal;

public interface SavePortfolioProposalPort {
    PortfolioProposal save(PortfolioProposal proposal);
}
