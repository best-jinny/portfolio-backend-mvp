package com.hyejin.portfolio.evidence.domain;

import java.util.List;
import java.util.UUID;

public record EvidenceDetail(
    UUID evidenceId,
    String claim,
    String basis,
    String limitation,
    String reviewTrigger,
    List<String> sourceSnapshots,
    List<String> affectedSymbols
) {
    public EvidenceDetail {
        sourceSnapshots = List.copyOf(sourceSnapshots == null ? List.of() : sourceSnapshots);
        affectedSymbols = List.copyOf(affectedSymbols == null ? List.of() : affectedSymbols);
    }
}
