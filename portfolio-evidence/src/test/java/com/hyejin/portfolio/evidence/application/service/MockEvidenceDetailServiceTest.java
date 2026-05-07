package com.hyejin.portfolio.evidence.application.service;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MockEvidenceDetailServiceTest {
    @Test
    void returnsFalseDiversificationEvidenceFromDeterministicId() {
        var evidenceId = UUID.nameUUIDFromBytes("FALSE_DIVERSIFICATION:TIGER-SP500,KRW-BTC".getBytes(StandardCharsets.UTF_8));
        var service = new MockEvidenceDetailService();

        var detail = service.get(evidenceId);

        assertThat(detail.claim()).contains("not mainly diversifying");
        assertThat(detail.basis()).contains("mock correlation");
        assertThat(detail.reviewTrigger()).contains("correlation");
        assertThat(detail.affectedSymbols()).contains("TIGER-SP500", "KRW-BTC");
    }
}
