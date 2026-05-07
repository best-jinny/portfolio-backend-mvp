package com.hyejin.portfolio.simulation.application.service;

import com.hyejin.portfolio.simulation.application.port.in.GenerateCapitalGrowthProjectionUseCase;
import com.hyejin.portfolio.simulation.domain.CapitalGrowthPoint;
import com.hyejin.portfolio.simulation.domain.CapitalGrowthProjection;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.UUID;

public class GenerateCapitalGrowthProjectionService implements GenerateCapitalGrowthProjectionUseCase {
    @Override
    public CapitalGrowthProjection generate(Command command) {
        if (command.horizonMonths() <= 0) {
            throw new IllegalArgumentException("horizon must be positive");
        }
        if (command.intervalMonths() <= 0) {
            throw new IllegalArgumentException("interval must be positive");
        }

        var monthlyRate = command.annualReturnRate().divide(new BigDecimal("12"), MathContext.DECIMAL64);
        var points = new ArrayList<CapitalGrowthPoint>();
        for (int month = 0; month <= command.horizonMonths(); month += command.intervalMonths()) {
            var principal = command.initialPrincipal().add(command.monthlyContribution().multiply(BigDecimal.valueOf(month)));
            var value = compound(command.initialPrincipal(), monthlyRate, month)
                .add(monthlyContributionFutureValue(command.monthlyContribution(), monthlyRate, month));
            points.add(new CapitalGrowthPoint(month, principal, value, value.subtract(principal)));
        }

        return new CapitalGrowthProjection(UUID.randomUUID(), command.proposalId(), command.scenario(), points);
    }

    private BigDecimal compound(BigDecimal principal, BigDecimal monthlyRate, int months) {
        var value = principal;
        for (int i = 0; i < months; i++) {
            value = value.multiply(BigDecimal.ONE.add(monthlyRate), MathContext.DECIMAL64);
        }
        return value;
    }

    private BigDecimal monthlyContributionFutureValue(BigDecimal contribution, BigDecimal monthlyRate, int months) {
        var value = BigDecimal.ZERO;
        for (int i = 0; i < months; i++) {
            value = value.add(contribution).multiply(BigDecimal.ONE.add(monthlyRate), MathContext.DECIMAL64);
        }
        return value;
    }
}
