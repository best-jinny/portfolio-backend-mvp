package com.hyejin.portfolio.proposal.application.service;

import com.hyejin.portfolio.proposal.domain.InsightSignal;
import com.hyejin.portfolio.proposal.domain.InsightSignalType;
import com.hyejin.portfolio.proposal.domain.ProposalAction;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class GenerateProposalActionsService {
    public List<ProposalAction> generate(List<InsightSignal> signals) {
        return signals.stream().map(this::toAction).toList();
    }

    private ProposalAction toAction(InsightSignal signal) {
        var evidenceIds = List.of(evidenceIdFor(signal));
        if (signal.type() == InsightSignalType.FALSE_DIVERSIFICATION) {
            return new ProposalAction(
                "CAP_OR_RECLASSIFY",
                "Do not count the crypto position as diversification until the relationship weakens",
                "Keep the broad US equity ETF as the core holding and cap Bitcoin unless the user explicitly wants a stronger risk-appetite bet.",
                "Review if Bitcoin stops moving with US equities in the mock correlation profile.",
                signal.affectedSymbols(),
                evidenceIds
            );
        }
        if (signal.type() == InsightSignalType.TACTICAL_PRODUCT_MISUSE) {
            return new ProposalAction(
                "CLASSIFY_TACTICAL",
                "Treat the leveraged ETF as tactical exposure",
                "Use SOXL only for a short semiconductor view. Do not let it become the main way the portfolio expresses a long-term AI or chip thesis.",
                "Review if the position is still held after the tactical momentum window.",
                signal.affectedSymbols(),
                evidenceIds
            );
        }
        return new ProposalAction(
            "REDUCE_OVERLAP",
            "Reduce one side of the overlapping exposure",
            "Keep the intended thesis, but avoid expressing it through too many assets that depend on the same driver.",
            "Review if the shared driver weakens or one asset no longer tracks it.",
            signal.affectedSymbols(),
            evidenceIds
        );
    }

    private UUID evidenceIdFor(InsightSignal signal) {
        var source = signal.type().name() + ":" + String.join(",", signal.affectedSymbols());
        return UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8));
    }
}
