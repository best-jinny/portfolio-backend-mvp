package com.hyejin.portfolio.simulation.application.service;

import com.hyejin.portfolio.simulation.application.port.in.GenerateCapitalGrowthProjectionUseCase;
import com.hyejin.portfolio.simulation.domain.Scenario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateCapitalGrowthProjectionServiceTest {
    @Test
    void createsSixMonthIntervalCapitalGrowthPoints() {
        var service = new GenerateCapitalGrowthProjectionService();
        var projection = service.generate(new GenerateCapitalGrowthProjectionUseCase.Command(
            UUID.randomUUID(),
            Scenario.BASE,
            new BigDecimal("10000000"),
            new BigDecimal("1000000"),
            new BigDecimal("0.06"),
            12,
            6
        ));

        assertThat(projection.points()).extracting("month").containsExactly(0, 6, 12);
    }
}
