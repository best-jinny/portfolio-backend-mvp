package com.hyejin.portfolio.evidence.application.service;

import com.hyejin.portfolio.evidence.application.port.in.GetEvidenceDetailUseCase;
import com.hyejin.portfolio.evidence.domain.EvidenceDetail;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class MockEvidenceDetailService implements GetEvidenceDetailUseCase {
    private static final UUID FALSE_DIVERSIFICATION_SP500_BTC =
        UUID.nameUUIDFromBytes("FALSE_DIVERSIFICATION:TIGER-SP500,KRW-BTC".getBytes(StandardCharsets.UTF_8));

    @Override
    public EvidenceDetail get(UUID evidenceId) {
        if (FALSE_DIVERSIFICATION_SP500_BTC.equals(evidenceId)) {
            return new EvidenceDetail(
                evidenceId,
                "Bitcoin is not mainly diversifying the S&P500 ETF in this mock portfolio; it is increasing the same risk-appetite bet.",
                "The mock correlation profile links TIGER-SP500 and KRW-BTC to the same risk-taking environment.",
                "This is mock data. Real price history, ETF data, and crypto market data are not connected yet.",
                "Review if the correlation signal weakens or Bitcoin stops falling with broad US equity risk-off moves.",
                List.of("MOCK_CORRELATION_PROFILE", "MOCK_EXPOSURE_TAGS"),
                List.of("TIGER-SP500", "KRW-BTC")
            );
        }
        return new EvidenceDetail(
            evidenceId,
            "This mock evidence record explains why the proposal flagged a portfolio structure issue.",
            "The proposal generated this id from a deterministic signal type and affected asset list.",
            "Specific external source data is not connected in this phase.",
            "Review when the related signal disappears from a regenerated proposal.",
            List.of("MOCK_EXPOSURE_TAGS"),
            List.of()
        );
    }
}
