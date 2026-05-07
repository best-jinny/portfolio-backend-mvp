package com.hyejin.portfolio.proposal.domain;

import java.util.List;

public record HorizonRationale(
    int recommendedHorizonMonths,
    int reviewAfterMonths,
    String userExplanation,
    List<String> primaryDrivers,
    List<String> reviewTriggers
) {
    public HorizonRationale {
        primaryDrivers = List.copyOf(primaryDrivers == null ? List.of() : primaryDrivers);
        reviewTriggers = List.copyOf(reviewTriggers == null ? List.of() : reviewTriggers);
    }
}
