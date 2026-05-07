package com.hyejin.portfolio.evidence.domain;

import java.util.List;
import java.util.UUID;

public record Claim(
    UUID id,
    String text,
    ClaimType type,
    Confidence confidence,
    List<UUID> sourceIds,
    String limitation
) {
    public Claim {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("claim text must not be blank");
        }
        if (type == ClaimType.FACT && (sourceIds == null || sourceIds.isEmpty())) {
            throw new IllegalArgumentException("FACT claim requires at least one source");
        }
        if (type == ClaimType.INTERPRETATION && (limitation == null || limitation.isBlank())) {
            throw new IllegalArgumentException("INTERPRETATION claim requires limitation or counterpoint");
        }
        sourceIds = sourceIds == null ? List.of() : List.copyOf(sourceIds);
    }
}
