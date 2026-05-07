package com.hyejin.portfolio.evidence.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimTest {
    @Test
    void factClaimRequiresSource() {
        assertThatThrownBy(() -> new Claim(
            UUID.randomUUID(),
            "Samsung Electronics is listed on KRX.",
            ClaimType.FACT,
            Confidence.HIGH,
            List.of(),
            null
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
