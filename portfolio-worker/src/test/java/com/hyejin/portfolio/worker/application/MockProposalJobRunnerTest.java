package com.hyejin.portfolio.worker.application;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MockProposalJobRunnerTest {
    @Test
    void workerCoordinatesWorkflowStepsWithoutDomainDecisions() {
        var result = new MockProposalJobRunner().run(UUID.randomUUID());

        assertThat(result.steps()).isEqualTo(List.of(
            "VALIDATE_INTENT",
            "RESOLVE_ASSETS",
            "GENERATE_ALLOCATION",
            "SIMULATE_CAPITAL_GROWTH",
            "CREATE_EVIDENCE_LINKS",
            "COMPLETE_PROPOSAL"
        ));
    }
}
