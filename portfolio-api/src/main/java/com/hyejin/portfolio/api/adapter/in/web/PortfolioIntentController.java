package com.hyejin.portfolio.api.adapter.in.web;

import com.hyejin.portfolio.api.application.service.PortfolioIntentApiService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portfolio-intents")
public class PortfolioIntentController {
    private final PortfolioIntentApiService portfolioIntentApiService;

    public PortfolioIntentController(PortfolioIntentApiService portfolioIntentApiService) {
        this.portfolioIntentApiService = portfolioIntentApiService;
    }

    @PostMapping
    public CreateIntentResponse createIntent(@RequestBody CreateIntentRequest request) {
        var intent = portfolioIntentApiService.create(new PortfolioIntentApiService.CreateIntentCommand(
            request.availableCash(),
            request.monthlyContribution(),
            request.riskProfile(),
            request.assets().stream()
                .map(asset -> new PortfolioIntentApiService.CreateIntentAssetCommand(asset.assetId(), asset.thesis()))
                .toList()
        ));
        return new CreateIntentResponse(intent.id());
    }

    public record CreateIntentRequest(
        BigDecimal availableCash,
        BigDecimal monthlyContribution,
        String riskProfile,
        List<CreateIntentAssetRequest> assets
    ) {
    }

    public record CreateIntentAssetRequest(UUID assetId, String thesis) {
    }

    public record CreateIntentResponse(UUID intentId) {
    }
}
