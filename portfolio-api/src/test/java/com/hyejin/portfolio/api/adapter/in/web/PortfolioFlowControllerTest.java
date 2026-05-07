package com.hyejin.portfolio.api.adapter.in.web;

import com.hyejin.portfolio.api.PortfolioApiApplication;
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

    @Test
    void postmanFlowWorksWithMockData() {
        webTestClient.get()
            .uri("/api/assets/search?query=bitcoin")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].symbol").isEqualTo("KRW-BTC");

        webTestClient.post()
            .uri("/api/portfolio-proposals")
            .header("Content-Type", "application/json")
            .bodyValue("""
                {
                  "intentId": "00000000-0000-0000-0000-000000000999",
                  "assetIds": [
                    "00000000-0000-0000-0000-000000000104",
                    "00000000-0000-0000-0000-000000000107"
                  ]
                }
                """)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo("COMPLETED")
            .jsonPath("$.signals[0].type").isEqualTo("FALSE_DIVERSIFICATION");

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
