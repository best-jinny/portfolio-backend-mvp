package com.hyejin.portfolio.api.application.service;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProposalQueryServiceTest {
    @Test
    void returnsCompletedMockProposalDetail() {
        var proposalId = UUID.randomUUID();
        var response = new ProposalQueryService().getProposal(proposalId);

        assertThat(response.proposalId()).isEqualTo(proposalId);
        assertThat(response.status()).isEqualTo("COMPLETED");
    }

    @Test
    void returnsMockEvidenceDetail() {
        var evidenceId = UUID.randomUUID();
        var response = new ProposalQueryService().getEvidence(evidenceId);

        assertThat(response.evidenceId()).isEqualTo(evidenceId);
        assertThat(response.claimType()).isEqualTo("INTERPRETATION");
    }
}
