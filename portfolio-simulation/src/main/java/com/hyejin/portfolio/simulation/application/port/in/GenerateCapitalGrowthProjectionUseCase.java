package com.hyejin.portfolio.simulation.application.port.in;

import com.hyejin.portfolio.simulation.domain.CapitalGrowthProjection;
import com.hyejin.portfolio.simulation.domain.Scenario;

import java.math.BigDecimal;
import java.util.UUID;

public interface GenerateCapitalGrowthProjectionUseCase {
    CapitalGrowthProjection generate(Command command);

    record Command(
        UUID proposalId,
        Scenario scenario,
        BigDecimal initialPrincipal,
        BigDecimal monthlyContribution,
        BigDecimal annualReturnRate,
        int horizonMonths,
        int intervalMonths
    ) {
    }
}
