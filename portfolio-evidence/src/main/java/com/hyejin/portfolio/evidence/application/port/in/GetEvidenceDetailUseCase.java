package com.hyejin.portfolio.evidence.application.port.in;

import com.hyejin.portfolio.evidence.domain.EvidenceDetail;

import java.util.UUID;

public interface GetEvidenceDetailUseCase {
    EvidenceDetail get(UUID evidenceId);
}
