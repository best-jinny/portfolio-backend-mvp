package com.hyejin.portfolio.api.adapter.in.web;

import com.hyejin.portfolio.api.application.service.ProposalQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/evidence")
public class EvidenceController {
    private final ProposalQueryService proposalQueryService;

    public EvidenceController(ProposalQueryService proposalQueryService) {
        this.proposalQueryService = proposalQueryService;
    }

    @GetMapping("/{evidenceId}")
    public Object getEvidence(@PathVariable UUID evidenceId) {
        return proposalQueryService.getEvidence(evidenceId);
    }
}
