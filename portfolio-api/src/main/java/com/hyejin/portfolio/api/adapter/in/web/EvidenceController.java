package com.hyejin.portfolio.api.adapter.in.web;

import com.hyejin.portfolio.evidence.application.port.in.GetEvidenceDetailUseCase;
import com.hyejin.portfolio.evidence.domain.EvidenceDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/evidence")
public class EvidenceController {
    private final GetEvidenceDetailUseCase getEvidenceDetailUseCase;

    public EvidenceController(GetEvidenceDetailUseCase getEvidenceDetailUseCase) {
        this.getEvidenceDetailUseCase = getEvidenceDetailUseCase;
    }

    @GetMapping("/{evidenceId}")
    public EvidenceDetail getEvidence(@PathVariable UUID evidenceId) {
        return getEvidenceDetailUseCase.get(evidenceId);
    }
}
