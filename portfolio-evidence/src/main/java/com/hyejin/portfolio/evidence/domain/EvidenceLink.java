package com.hyejin.portfolio.evidence.domain;

import java.util.UUID;

public record EvidenceLink(
    UUID id,
    UUID claimId,
    String targetModule,
    String targetType,
    String targetId,
    String targetAnchor,
    RelationType relationType
) {
}
