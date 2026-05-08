package com.hyejin.portfolio.proposal.application.service;

import com.hyejin.portfolio.proposal.application.port.in.CreatePortfolioProposalUseCase;
import com.hyejin.portfolio.proposal.application.port.in.GetPortfolioIntentUseCase;
import com.hyejin.portfolio.proposal.application.port.out.SavePortfolioProposalPort;
import com.hyejin.portfolio.proposal.domain.HorizonRationale;
import com.hyejin.portfolio.proposal.domain.PortfolioProposal;
import com.hyejin.portfolio.proposal.domain.ProposalAssetSnapshot;
import com.hyejin.portfolio.proposal.domain.ProposalStatus;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class CreatePortfolioProposalService implements CreatePortfolioProposalUseCase {
    private final GetPortfolioIntentUseCase getPortfolioIntentUseCase;
    private final SavePortfolioProposalPort savePortfolioProposalPort;
    private final AnalyzePortfolioExposuresService analyzer;
    private final DetectInsightSignalsService detector;
    private final GenerateProposalActionsService actionGenerator;

    public CreatePortfolioProposalService(
        GetPortfolioIntentUseCase getPortfolioIntentUseCase,
        SavePortfolioProposalPort savePortfolioProposalPort,
        AnalyzePortfolioExposuresService analyzer,
        DetectInsightSignalsService detector,
        GenerateProposalActionsService actionGenerator
    ) {
        this.getPortfolioIntentUseCase = getPortfolioIntentUseCase;
        this.savePortfolioProposalPort = savePortfolioProposalPort;
        this.analyzer = analyzer;
        this.detector = detector;
        this.actionGenerator = actionGenerator;
    }

    @Override
    public PortfolioProposal create(Command command) {
        var intent = getPortfolioIntentUseCase.get(command.intentId());
        var assets = intent.selectedAssets().stream()
            .map(selectedAsset -> selectedAsset.snapshot())
            .toList();
        var exposure = analyzer.analyze(assets);
        var signals = detector.detect(exposure);
        var actions = actionGenerator.generate(signals);
        var horizon = buildHorizon(assets);
        var summary = signals.isEmpty()
            ? "The selected assets do not trigger a major mock overlap signal."
            : signals.getFirst().userExplanation();

        return savePortfolioProposalPort.save(new PortfolioProposal(
            UUID.randomUUID(),
            command.intentId(),
            ProposalStatus.COMPLETED,
            summary,
            horizon,
            signals,
            actions,
            Instant.now()
        ));
    }

    private HorizonRationale buildHorizon(List<ProposalAssetSnapshot> assets) {
        var recommended = (int) Math.round(
            assets.stream().mapToInt(ProposalAssetSnapshot::momentumWindowMonths).average().orElse(12)
        );
        var reviewAfter = assets.stream().map(ProposalAssetSnapshot::momentumWindowMonths).min(Comparator.naturalOrder()).orElse(6);
        var drivers = assets.stream().map(asset -> asset.symbol() + ": " + asset.catalyst()).toList();
        var triggers = assets.stream().flatMap(asset -> asset.reviewTriggers().stream()).limit(4).toList();

        return new HorizonRationale(
            recommended,
            reviewAfter,
            "The horizon comes from the selected assets' mock momentum windows and the earliest point where the main thesis should be checked again. Risk profile changes position limits, not this horizon.",
            drivers,
            triggers
        );
    }
}
