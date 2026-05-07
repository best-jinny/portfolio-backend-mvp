package com.hyejin.portfolio.proposal.domain;

import java.util.List;
import java.util.UUID;

public record ProposalAction(
    String actionType,
    String title,
    String userExplanation,
    String reviewTrigger,
    List<String> affectedSymbols,
    List<UUID> evidenceIds
) {
    public ProposalAction {
        affectedSymbols = List.copyOf(affectedSymbols == null ? List.of() : affectedSymbols);
        evidenceIds = List.copyOf(evidenceIds == null ? List.of() : evidenceIds);
    }
}
