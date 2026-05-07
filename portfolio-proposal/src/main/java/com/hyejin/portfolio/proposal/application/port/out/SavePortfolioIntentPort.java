package com.hyejin.portfolio.proposal.application.port.out;

import com.hyejin.portfolio.proposal.domain.PortfolioIntent;

public interface SavePortfolioIntentPort {
    PortfolioIntent save(PortfolioIntent intent);
}
