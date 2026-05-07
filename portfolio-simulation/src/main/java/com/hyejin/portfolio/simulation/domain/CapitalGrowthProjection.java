package com.hyejin.portfolio.simulation.domain;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record CapitalGrowthProjection(
    UUID id,
    UUID proposalId,
    Scenario scenario,
    List<CapitalGrowthPoint> points
) {
    public CapitalGrowthProjection {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("projection points must not be empty");
        }
        var sorted = points.stream().sorted(Comparator.comparingInt(CapitalGrowthPoint::month)).toList();
        if (!points.equals(sorted)) {
            throw new IllegalArgumentException("projection points must be sorted by month");
        }
        points = List.copyOf(points);
    }
}
