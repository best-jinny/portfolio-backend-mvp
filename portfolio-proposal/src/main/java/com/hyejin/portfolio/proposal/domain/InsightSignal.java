package com.hyejin.portfolio.proposal.domain;

import java.util.List;

public record InsightSignal(
    InsightSignalType type,
    String severity,
    String title,
    String userExplanation,
    String dataBasis,
    List<String> affectedSymbols
) {
    public InsightSignal {
        affectedSymbols = List.copyOf(affectedSymbols == null ? List.of() : affectedSymbols);
    }
}
