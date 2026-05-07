package com.hyejin.portfolio.worker.application;

import java.util.List;
import java.util.UUID;

public class MockProposalJobRunner {
    public MockProposalJobResult run(UUID intentId) {
        return new MockProposalJobResult(
            intentId,
            List.of(
                "VALIDATE_INTENT",
                "RESOLVE_ASSETS",
                "GENERATE_ALLOCATION",
                "SIMULATE_CAPITAL_GROWTH",
                "CREATE_EVIDENCE_LINKS",
                "COMPLETE_PROPOSAL"
            )
        );
    }
}
