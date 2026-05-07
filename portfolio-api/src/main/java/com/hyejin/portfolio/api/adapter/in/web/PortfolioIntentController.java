package com.hyejin.portfolio.api.adapter.in.web;

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
    @PostMapping
    public CreateIntentResponse createIntent(@RequestBody CreateIntentRequest request) {
        return new CreateIntentResponse(UUID.randomUUID());
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
