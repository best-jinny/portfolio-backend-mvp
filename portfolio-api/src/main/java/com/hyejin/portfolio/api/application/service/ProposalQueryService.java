package com.hyejin.portfolio.api.application.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProposalQueryService {
    public ProposalDetailResponse getProposal(UUID proposalId) {
        return new ProposalDetailResponse(
            proposalId,
            "COMPLETED",
            "Mock cycle momentum portfolio proposal",
            12,
            List.of(),
            List.of()
        );
    }

    public EvidenceDetailResponse getEvidence(UUID evidenceId) {
        return new EvidenceDetailResponse(
            evidenceId,
            "Mock evidence-backed claim",
            "INTERPRETATION",
            "MEDIUM",
            List.of("mock-source"),
            List.of("mock data only")
        );
    }
}
