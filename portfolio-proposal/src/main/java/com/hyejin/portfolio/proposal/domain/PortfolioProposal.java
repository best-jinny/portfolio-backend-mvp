package com.hyejin.portfolio.proposal.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PortfolioProposal(
    UUID proposalId,
    UUID intentId,
    ProposalStatus status,
    String summary,
    HorizonRationale horizonRationale,
    List<InsightSignal> signals,
    List<ProposalAction> actions,
    Instant createdAt
) {
    public PortfolioProposal {
        signals = List.copyOf(signals == null ? List.of() : signals);
        actions = List.copyOf(actions == null ? List.of() : actions);
    }
}
