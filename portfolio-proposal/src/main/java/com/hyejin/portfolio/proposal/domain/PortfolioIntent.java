package com.hyejin.portfolio.proposal.domain;

import com.hyejin.portfolio.common.Money;

import java.time.Instant;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record PortfolioIntent(
    UUID id,
    Money availableCash,
    Money monthlyContribution,
    RiskProfile riskProfile,
    Currency baseCurrency,
    ProposalMode proposalMode,
    List<SelectedAsset> selectedAssets,
    Instant createdAt
) {
    public PortfolioIntent {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(availableCash, "availableCash must not be null");
        Objects.requireNonNull(riskProfile, "riskProfile must not be null");
        Objects.requireNonNull(baseCurrency, "baseCurrency must not be null");
        Objects.requireNonNull(proposalMode, "proposalMode must not be null");
        if (selectedAssets == null || selectedAssets.isEmpty()) {
            throw new IllegalArgumentException("selected assets must not be empty");
        }
        selectedAssets = List.copyOf(selectedAssets);
    }

    public static PortfolioIntent create(
        Money availableCash,
        Money monthlyContribution,
        RiskProfile riskProfile,
        Currency baseCurrency,
        ProposalMode proposalMode,
        List<SelectedAsset> selectedAssets
    ) {
        var sortedAssets = selectedAssets.stream()
            .sorted(Comparator.comparingInt(SelectedAsset::displayOrder))
            .toList();
        return new PortfolioIntent(
            UUID.randomUUID(),
            availableCash,
            monthlyContribution,
            riskProfile,
            baseCurrency,
            proposalMode,
            sortedAssets,
            Instant.now()
        );
    }
}
