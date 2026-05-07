package com.hyejin.portfolio.proposal.adapter.out;

import com.hyejin.portfolio.proposal.application.port.out.SavePortfolioIntentPort;
import com.hyejin.portfolio.proposal.domain.PortfolioIntent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPortfolioIntentRepository implements SavePortfolioIntentPort {
    private final ConcurrentHashMap<UUID, PortfolioIntent> intents = new ConcurrentHashMap<>();

    @Override
    public PortfolioIntent save(PortfolioIntent intent) {
        intents.put(intent.id(), intent);
        return intent;
    }
}
