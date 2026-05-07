package com.hyejin.portfolio.api.adapter.in.web;

import com.hyejin.portfolio.proposal.application.port.in.CreatePortfolioProposalUseCase;
import com.hyejin.portfolio.proposal.application.port.in.GetPortfolioProposalUseCase;
import com.hyejin.portfolio.proposal.domain.PortfolioProposal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portfolio-proposals")
public class PortfolioProposalController {
    private final CreatePortfolioProposalUseCase createPortfolioProposalUseCase;
    private final GetPortfolioProposalUseCase getPortfolioProposalUseCase;

    public PortfolioProposalController(
        CreatePortfolioProposalUseCase createPortfolioProposalUseCase,
        GetPortfolioProposalUseCase getPortfolioProposalUseCase
    ) {
        this.createPortfolioProposalUseCase = createPortfolioProposalUseCase;
        this.getPortfolioProposalUseCase = getPortfolioProposalUseCase;
    }

    @PostMapping
    public PortfolioProposal createProposal(@RequestBody CreateProposalRequest request) {
        return createPortfolioProposalUseCase.create(new CreatePortfolioProposalUseCase.Command(
            request.intentId(),
            request.assetIds()
        ));
    }

    @GetMapping("/{proposalId}")
    public PortfolioProposal getProposal(@PathVariable UUID proposalId) {
        return getPortfolioProposalUseCase.get(proposalId);
    }

    public record CreateProposalRequest(UUID intentId, List<UUID> assetIds) {
    }
}
