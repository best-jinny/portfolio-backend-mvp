package com.hyejin.portfolio.proposal.domain;

import com.hyejin.portfolio.common.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PortfolioIntentTest {
    @Test
    void intentRequiresAtLeastOneSelectedAsset() {
        assertThatThrownBy(() -> PortfolioIntent.create(
            new Money(new BigDecimal("10000000"), Currency.getInstance("KRW")),
            null,
            RiskProfile.GROWTH,
            Currency.getInstance("KRW"),
            ProposalMode.CYCLE_MOMENTUM,
            List.of()
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
