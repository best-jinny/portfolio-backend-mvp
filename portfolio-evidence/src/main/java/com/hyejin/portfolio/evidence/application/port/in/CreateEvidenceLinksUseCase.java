package com.hyejin.portfolio.evidence.application.port.in;

import com.hyejin.portfolio.evidence.domain.Claim;
import com.hyejin.portfolio.evidence.domain.EvidenceLink;
import com.hyejin.portfolio.evidence.domain.RelationType;

import java.util.List;
import java.util.UUID;

public interface CreateEvidenceLinksUseCase {
    Result create(Command command);

    record Command(Claim claim, List<TargetReference> targets) {
    }

    record TargetReference(
        String targetModule,
        String targetType,
        String targetId,
        String targetAnchor,
        RelationType relationType
    ) {
    }

    record Result(UUID claimId, List<EvidenceLink> links) {
    }
}
