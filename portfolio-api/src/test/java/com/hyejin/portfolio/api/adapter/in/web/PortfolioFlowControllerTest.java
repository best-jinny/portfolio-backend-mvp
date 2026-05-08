package com.hyejin.portfolio.api.adapter.in.web;

import com.hyejin.portfolio.api.PortfolioApiApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@SpringBootTest(classes = PortfolioApiApplication.class)
@AutoConfigureWebTestClient
class PortfolioFlowControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void postmanFlowWorksWithMockData() throws Exception {
        webTestClient.get()
            .uri("/api/assets/search?query=bitcoin")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].symbol").isEqualTo("KRW-BTC");

        var intentResult = webTestClient.post()
            .uri("/api/portfolio-intents")
            .header("Content-Type", "application/json")
            .bodyValue("""
                {
                  "availableCash": 10000000,
                  "monthlyContribution": 1000000,
                  "riskProfile": "GROWTH",
                  "assets": [
                    {"assetId": "00000000-0000-0000-0000-000000000104", "thesis": "US core"},
                    {"assetId": "00000000-0000-0000-0000-000000000107", "thesis": "Crypto upside"}
                  ]
                }
                """)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.intentId").exists()
            .returnResult();
        var intentId = objectMapper.readTree(intentResult.getResponseBody()).get("intentId").asText();

        var proposalResult = webTestClient.post()
            .uri("/api/portfolio-proposals")
            .header("Content-Type", "application/json")
            .bodyValue("""
                {
                  "intentId": "%s"
                }
                """.formatted(intentId))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo("COMPLETED")
            .jsonPath("$.signals[0].type").isEqualTo("FALSE_DIVERSIFICATION")
            .returnResult();
        JsonNode proposal = objectMapper.readTree(proposalResult.getResponseBody());
        var proposalId = proposal.get("proposalId").asText();

        webTestClient.get()
            .uri("/api/portfolio-proposals/" + proposalId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.proposalId").isEqualTo(proposalId)
            .jsonPath("$.summary").value(org.hamcrest.Matchers.containsString("risk-taking environment"))
            .jsonPath("$.allocations[0].role").exists()
            .jsonPath("$.capitalGrowth[0].month").isEqualTo(0);

        var evidenceId = UUID.nameUUIDFromBytes(
            "FALSE_DIVERSIFICATION:TIGER-SP500,KRW-BTC".getBytes(StandardCharsets.UTF_8)
        );

        webTestClient.get()
            .uri("/api/evidence/" + evidenceId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.claim").value(org.hamcrest.Matchers.containsString("not mainly diversifying"));
    }
}
