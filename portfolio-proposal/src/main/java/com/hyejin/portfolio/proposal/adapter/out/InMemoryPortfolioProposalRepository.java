package com.hyejin.portfolio.proposal.adapter.out;

import com.hyejin.portfolio.proposal.application.port.in.GetPortfolioProposalUseCase;
import com.hyejin.portfolio.proposal.application.port.out.SavePortfolioProposalPort;
import com.hyejin.portfolio.proposal.domain.PortfolioProposal;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPortfolioProposalRepository implements SavePortfolioProposalPort, GetPortfolioProposalUseCase {
    private final ConcurrentHashMap<UUID, PortfolioProposal> proposals = new ConcurrentHashMap<>();

    @Override
    public PortfolioProposal save(PortfolioProposal proposal) {
        proposals.put(proposal.proposalId(), proposal);
        return proposal;
    }

    @Override
    public PortfolioProposal get(UUID proposalId) {
        var proposal = proposals.get(proposalId);
        if (proposal == null) {
            throw new IllegalArgumentException("portfolio proposal not found: " + proposalId);
        }
        return proposal;
    }
}
