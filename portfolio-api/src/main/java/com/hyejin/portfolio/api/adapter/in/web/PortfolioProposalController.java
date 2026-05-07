package com.hyejin.portfolio.api.adapter.in.web;

import com.hyejin.portfolio.api.application.service.ProposalQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/portfolio-proposals")
public class PortfolioProposalController {
    private final ProposalQueryService proposalQueryService;

    public PortfolioProposalController(ProposalQueryService proposalQueryService) {
        this.proposalQueryService = proposalQueryService;
    }

    @GetMapping("/{proposalId}")
    public Object getProposal(@PathVariable UUID proposalId) {
        return proposalQueryService.getProposal(proposalId);
    }

    @PostMapping
    public CreateProposalResponse createProposal() {
        return new CreateProposalResponse(UUID.randomUUID(), "QUEUED");
    }

    public record CreateProposalResponse(UUID proposalId, String status) {
    }
}
