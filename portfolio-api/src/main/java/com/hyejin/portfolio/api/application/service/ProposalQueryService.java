package com.hyejin.portfolio.api.application.service;

import com.hyejin.portfolio.allocation.domain.AllocationPlan;
import com.hyejin.portfolio.allocation.domain.ProposedAllocation;
import com.hyejin.portfolio.common.Percentage;
import com.hyejin.portfolio.proposal.application.port.in.GetPortfolioIntentUseCase;
import com.hyejin.portfolio.proposal.application.port.in.GetPortfolioProposalUseCase;
import com.hyejin.portfolio.proposal.domain.PortfolioProposal;
import com.hyejin.portfolio.proposal.domain.SelectedAsset;
import com.hyejin.portfolio.simulation.application.port.in.GenerateCapitalGrowthProjectionUseCase;
import com.hyejin.portfolio.simulation.application.service.GenerateCapitalGrowthProjectionService;
import com.hyejin.portfolio.simulation.domain.Scenario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProposalQueryService {
    private final GetPortfolioProposalUseCase getPortfolioProposalUseCase;
    private final GetPortfolioIntentUseCase getPortfolioIntentUseCase;
    private final GenerateCapitalGrowthProjectionUseCase generateCapitalGrowthProjectionUseCase;

    @Autowired
    public ProposalQueryService(
        GetPortfolioProposalUseCase getPortfolioProposalUseCase,
        GetPortfolioIntentUseCase getPortfolioIntentUseCase
    ) {
        this(getPortfolioProposalUseCase, getPortfolioIntentUseCase, new GenerateCapitalGrowthProjectionService());
    }

    ProposalQueryService(
        GetPortfolioProposalUseCase getPortfolioProposalUseCase,
        GetPortfolioIntentUseCase getPortfolioIntentUseCase,
        GenerateCapitalGrowthProjectionUseCase generateCapitalGrowthProjectionUseCase
    ) {
        this.getPortfolioProposalUseCase = getPortfolioProposalUseCase;
        this.getPortfolioIntentUseCase = getPortfolioIntentUseCase;
        this.generateCapitalGrowthProjectionUseCase = generateCapitalGrowthProjectionUseCase;
    }

    public ProposalDetailResponse getProposal(UUID proposalId) {
        var proposal = getPortfolioProposalUseCase.get(proposalId);
        var intent = getPortfolioIntentUseCase.get(proposal.intentId());
        var allocationPlan = allocationPlan(proposal, intent.selectedAssets());
        var projection = generateCapitalGrowthProjectionUseCase.generate(new GenerateCapitalGrowthProjectionUseCase.Command(
            proposal.proposalId(),
            Scenario.BASE,
            intent.availableCash().amount(),
            intent.monthlyContribution() == null ? BigDecimal.ZERO : intent.monthlyContribution().amount(),
            new BigDecimal("0.06"),
            proposal.horizonRationale().recommendedHorizonMonths(),
            6
        ));

        return new ProposalDetailResponse(
            proposalId,
            proposal.status().name(),
            "Mock cycle momentum portfolio proposal",
            proposal.summary(),
            proposal.horizonRationale().recommendedHorizonMonths(),
            new ProposalDetailResponse.HorizonRationaleResponse(
                proposal.horizonRationale().recommendedHorizonMonths(),
                proposal.horizonRationale().reviewAfterMonths(),
                proposal.horizonRationale().userExplanation(),
                proposal.horizonRationale().primaryDrivers(),
                proposal.horizonRationale().reviewTriggers()
            ),
            proposal.signals().stream()
                .map(signal -> new ProposalDetailResponse.SignalResponse(
                    signal.type().name(),
                    signal.severity(),
                    signal.title(),
                    signal.userExplanation(),
                    signal.dataBasis(),
                    signal.affectedSymbols()
                ))
                .toList(),
            proposal.actions().stream()
                .map(action -> new ProposalDetailResponse.ActionResponse(
                    action.actionType(),
                    action.title(),
                    action.userExplanation(),
                    action.reviewTrigger(),
                    action.affectedSymbols(),
                    action.evidenceIds()
                ))
                .toList(),
            allocationPlan.allocations().stream()
                .map(allocation -> new ProposalDetailResponse.AllocationResponse(
                    allocation.assetId(),
                    allocation.initialWeight().value(),
                    allocation.role()
                ))
                .toList(),
            projection.points().stream()
                .map(point -> new ProposalDetailResponse.CapitalGrowthPointResponse(
                    point.month(),
                    point.cumulativePrincipal(),
                    point.expectedValue(),
                    point.expectedProfit()
                ))
                .toList()
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

    private AllocationPlan allocationPlan(PortfolioProposal proposal, List<SelectedAsset> assets) {
        var allocations = new ArrayList<ProposedAllocation>();
        var equalWeight = BigDecimal.ONE.divide(BigDecimal.valueOf(assets.size()), MathContext.DECIMAL64);
        var assigned = BigDecimal.ZERO;
        for (int index = 0; index < assets.size(); index++) {
            var asset = assets.get(index);
            var weight = index == assets.size() - 1 ? BigDecimal.ONE.subtract(assigned) : equalWeight;
            assigned = assigned.add(weight);
            allocations.add(new ProposedAllocation(
                asset.assetId(),
                Percentage.of(weight),
                Percentage.of(weight),
                index == 0 ? "core" : "satellite",
                asset.snapshot().catalyst()
            ));
        }
        return new AllocationPlan(
            UUID.randomUUID(),
            proposal.proposalId(),
            proposal.horizonRationale().recommendedHorizonMonths(),
            allocations
        );
    }
}
