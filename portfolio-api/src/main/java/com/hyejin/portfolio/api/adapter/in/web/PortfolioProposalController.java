package com.hyejin.portfolio.api.adapter.in.web;

import com.hyejin.portfolio.api.application.service.ProposalDetailResponse;
import com.hyejin.portfolio.api.application.service.ProposalQueryService;
import com.hyejin.portfolio.proposal.application.port.in.CreatePortfolioProposalUseCase;
import com.hyejin.portfolio.proposal.domain.PortfolioProposal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/portfolio-proposals")
public class PortfolioProposalController {
    private final CreatePortfolioProposalUseCase createPortfolioProposalUseCase;
    private final ProposalQueryService proposalQueryService;

    public PortfolioProposalController(
        CreatePortfolioProposalUseCase createPortfolioProposalUseCase,
        ProposalQueryService proposalQueryService
    ) {
        this.createPortfolioProposalUseCase = createPortfolioProposalUseCase;
        this.proposalQueryService = proposalQueryService;
    }

    @PostMapping
    public PortfolioProposal createProposal(@RequestBody CreateProposalRequest request) {
        return createPortfolioProposalUseCase.create(new CreatePortfolioProposalUseCase.Command(
            request.intentId()
        ));
    }

    @GetMapping("/{proposalId}")
    public ProposalDetailResponse getProposal(@PathVariable UUID proposalId) {
        return proposalQueryService.getProposal(proposalId);
    }

    public record CreateProposalRequest(UUID intentId) {
    }
}
