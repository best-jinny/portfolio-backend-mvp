package com.hyejin.portfolio.api.application.service;

import java.util.List;
import java.util.UUID;

public record EvidenceDetailResponse(
    UUID evidenceId,
    String claim,
    String claimType,
    String confidence,
    List<String> sources,
    List<String> limitations
) {
}
