package com.hyejin.portfolio.proposal.application.port.in;

import com.hyejin.portfolio.proposal.domain.PortfolioIntent;

import java.util.UUID;

public interface GetPortfolioIntentUseCase {
    PortfolioIntent get(UUID intentId);
}
