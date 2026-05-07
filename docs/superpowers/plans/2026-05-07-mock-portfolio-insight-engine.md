# Mock Portfolio Insight Engine Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a deterministic mock portfolio insight flow that runs from asset search through intent creation, proposal generation, proposal detail, and evidence detail.

**Architecture:** Keep business rules in feature modules and let `portfolio-api` expose screen-oriented request/response models. Mock asset features, exposure analysis, signal detection, action generation, and evidence assembly are deterministic so Postman can exercise the full flow before external APIs are connected.

**Tech Stack:** Java 21, Spring Boot WebFlux, Gradle multi-module, JUnit 5, AssertJ, ArchUnit.

---

## File Structure

### `portfolio-asset`

- `domain/Asset.java`: seed asset identity and mock feature data.
- `domain/AssetType.java`: `STOCK`, `ETF`, `CRYPTO`.
- `domain/ExposureTag.java`: economic exposure labels used for portfolio analysis.
- `application/port/in/SearchAssetsUseCase.java`: asset search API boundary.
- `application/port/in/GetAssetFeatureUseCase.java`: lookup selected asset feature snapshots.
- `application/service/MockAssetCatalogService.java`: deterministic in-memory asset catalog.
- `src/test/.../MockAssetCatalogServiceTest.java`: seed search and feature lookup tests.

### `portfolio-proposal`

- Extend existing intent use case to include asset snapshots.
- Add `GetPortfolioIntentUseCase`.
- Add proposal lifecycle and result records:
  - `domain/PortfolioProposal.java`
  - `domain/ProposalStatus.java`
  - `domain/InsightSignal.java`
  - `domain/ProposalAction.java`
  - `domain/HorizonRationale.java`
- Add ports and services:
  - `CreatePortfolioProposalUseCase`
  - `GetPortfolioProposalUseCase`
  - `AnalyzePortfolioExposuresService`
  - `DetectInsightSignalsService`
  - `GenerateProposalActionsService`
  - `CreatePortfolioProposalService`
- Add in-memory proposal repository.

### `portfolio-evidence`

- `application/port/in/GetEvidenceDetailUseCase.java`: evidence detail read boundary.
- `application/service/MockEvidenceDetailService.java`: deterministic evidence detail lookup by id.
- `domain/EvidenceDetail.java`: claim, basis, limitation, review trigger, and affected assets.

### `portfolio-api`

- Add asset controller.
- Wire intent controller to real use case.
- Wire proposal controller to real proposal use case.
- Extend proposal detail response with summary, horizon rationale, signals, actions, allocations, simulation, and evidence ids.
- Wire evidence controller to evidence read use case.

---

## Task 1: Mock Asset Catalog

**Files:**
- Modify: `portfolio-asset/build.gradle`
- Create: `portfolio-asset/src/main/java/com/hyejin/portfolio/asset/domain/AssetType.java`
- Create: `portfolio-asset/src/main/java/com/hyejin/portfolio/asset/domain/ExposureTag.java`
- Create: `portfolio-asset/src/main/java/com/hyejin/portfolio/asset/domain/Asset.java`
- Create: `portfolio-asset/src/main/java/com/hyejin/portfolio/asset/application/port/in/SearchAssetsUseCase.java`
- Create: `portfolio-asset/src/main/java/com/hyejin/portfolio/asset/application/port/in/GetAssetFeatureUseCase.java`
- Create: `portfolio-asset/src/main/java/com/hyejin/portfolio/asset/application/service/MockAssetCatalogService.java`
- Test: `portfolio-asset/src/test/java/com/hyejin/portfolio/asset/application/service/MockAssetCatalogServiceTest.java`

- [ ] **Step 1: Add AssertJ dependency if missing**

Use this dependency block in `portfolio-asset/build.gradle`:

```groovy
dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.3'
    testImplementation 'org.assertj:assertj-core:3.26.3'
}
```

- [ ] **Step 2: Write failing asset catalog tests**

Create `portfolio-asset/src/test/java/com/hyejin/portfolio/asset/application/service/MockAssetCatalogServiceTest.java`:

```java
package com.hyejin.portfolio.asset.application.service;

import com.hyejin.portfolio.asset.domain.ExposureTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MockAssetCatalogServiceTest {
    @Test
    void searchesAssetsByAliasSymbolAndDisplayName() {
        var service = new MockAssetCatalogService();

        var samsung = service.search("samsung");
        var tiger = service.search("s&p500");
        var bitcoin = service.search("bitcoin");

        assertThat(samsung).extracting("symbol").contains("005930");
        assertThat(tiger).extracting("symbol").contains("TIGER-SP500");
        assertThat(bitcoin).extracting("symbol").contains("KRW-BTC");
    }

    @Test
    void returnsMockFeatureTagsForSelectedAsset() {
        var service = new MockAssetCatalogService();
        var soxlId = service.search("soxl").getFirst().assetId();

        var feature = service.getFeature(soxlId);

        assertThat(feature.symbol()).isEqualTo("SOXL");
        assertThat(feature.exposureTags()).contains(ExposureTag.SEMICONDUCTOR_CYCLE, ExposureTag.LEVERAGED_PRODUCT);
        assertThat(feature.leverageMultiplier()).isEqualByComparingTo("3.0");
    }

    @Test
    void unknownAssetReturnsEmptyOptional() {
        var service = new MockAssetCatalogService();

        assertThat(service.findFeature(UUID.randomUUID())).isEmpty();
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run:

```bash
./gradlew :portfolio-asset:test
```

Expected: FAIL because asset catalog classes do not exist.

- [ ] **Step 4: Implement asset domain records**

Create `AssetType.java`:

```java
package com.hyejin.portfolio.asset.domain;

public enum AssetType {
    STOCK,
    ETF,
    CRYPTO
}
```

Create `ExposureTag.java`:

```java
package com.hyejin.portfolio.asset.domain;

public enum ExposureTag {
    KOREA_EQUITY,
    US_EQUITY,
    USD_EXPOSURE,
    KRW_LISTING,
    SEMICONDUCTOR_CYCLE,
    AI_CAPEX,
    AUTO_CYCLE,
    EXPORT_FX_SENSITIVITY,
    DEFENSIVE_CONSUMER,
    MEGA_CAP_TECH,
    BROAD_US_MARKET,
    CRYPTO_LIQUIDITY,
    RISK_APPETITE,
    HIGH_VOLATILITY,
    LEVERAGED_PRODUCT
}
```

Create `Asset.java`:

```java
package com.hyejin.portfolio.asset.domain;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record Asset(
    UUID assetId,
    String symbol,
    String displayName,
    String market,
    AssetType assetType,
    Currency currency,
    List<String> aliases,
    List<ExposureTag> exposureTags,
    BigDecimal return1m,
    BigDecimal return3m,
    BigDecimal return6m,
    BigDecimal return1y,
    BigDecimal volatility30d,
    BigDecimal volatility90d,
    BigDecimal maxDrawdown1y,
    BigDecimal leverageMultiplier,
    int momentumWindowMonths,
    String catalyst,
    List<String> reviewTriggers
) {
    public Asset {
        Objects.requireNonNull(assetId, "assetId must not be null");
        Objects.requireNonNull(symbol, "symbol must not be null");
        Objects.requireNonNull(displayName, "displayName must not be null");
        Objects.requireNonNull(market, "market must not be null");
        Objects.requireNonNull(assetType, "assetType must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        aliases = List.copyOf(aliases == null ? List.of() : aliases);
        exposureTags = List.copyOf(exposureTags == null ? List.of() : exposureTags);
        reviewTriggers = List.copyOf(reviewTriggers == null ? List.of() : reviewTriggers);
    }
}
```

- [ ] **Step 5: Implement asset ports**

Create `SearchAssetsUseCase.java`:

```java
package com.hyejin.portfolio.asset.application.port.in;

import com.hyejin.portfolio.asset.domain.Asset;

import java.util.List;

public interface SearchAssetsUseCase {
    List<Asset> search(String query);
}
```

Create `GetAssetFeatureUseCase.java`:

```java
package com.hyejin.portfolio.asset.application.port.in;

import com.hyejin.portfolio.asset.domain.Asset;

import java.util.Optional;
import java.util.UUID;

public interface GetAssetFeatureUseCase {
    Asset getFeature(UUID assetId);

    Optional<Asset> findFeature(UUID assetId);
}
```

- [ ] **Step 6: Implement deterministic mock asset catalog**

Create `MockAssetCatalogService.java` with eight seed assets. Use stable UUID constants so Postman examples remain repeatable.

```java
package com.hyejin.portfolio.asset.application.service;

import com.hyejin.portfolio.asset.application.port.in.GetAssetFeatureUseCase;
import com.hyejin.portfolio.asset.application.port.in.SearchAssetsUseCase;
import com.hyejin.portfolio.asset.domain.Asset;
import com.hyejin.portfolio.asset.domain.AssetType;
import com.hyejin.portfolio.asset.domain.ExposureTag;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class MockAssetCatalogService implements SearchAssetsUseCase, GetAssetFeatureUseCase {
    private static final Currency KRW = Currency.getInstance("KRW");
    private static final Currency USD = Currency.getInstance("USD");

    private final List<Asset> assets = List.of(
        asset("00000000-0000-0000-0000-000000000101", "005930", "Samsung Electronics", "KRX", AssetType.STOCK, KRW,
            List.of("samsung", "samsung electronics", "삼성전자"),
            List.of(ExposureTag.KOREA_EQUITY, ExposureTag.KRW_LISTING, ExposureTag.SEMICONDUCTOR_CYCLE, ExposureTag.AI_CAPEX),
            "0.04", "0.11", "0.18", "0.24", "0.22", "0.28", "-0.26", "1.0", 12,
            "AI memory and semiconductor-cycle recovery",
            List.of("memory price recovery weakens for two consecutive checks", "AI server demand proxy turns lower")),
        asset("00000000-0000-0000-0000-000000000102", "000660", "SK Hynix", "KRX", AssetType.STOCK, KRW,
            List.of("hynix", "sk hynix", "하이닉스"),
            List.of(ExposureTag.KOREA_EQUITY, ExposureTag.KRW_LISTING, ExposureTag.SEMICONDUCTOR_CYCLE, ExposureTag.AI_CAPEX),
            "0.06", "0.18", "0.30", "0.45", "0.30", "0.36", "-0.33", "1.0", 9,
            "HBM demand and memory-cycle momentum",
            List.of("HBM order momentum slows", "memory-cycle proxy rolls over")),
        asset("00000000-0000-0000-0000-000000000103", "KO", "Coca-Cola", "NYSE", AssetType.STOCK, USD,
            List.of("coca cola", "coca-cola", "ko", "코카콜라"),
            List.of(ExposureTag.US_EQUITY, ExposureTag.USD_EXPOSURE, ExposureTag.DEFENSIVE_CONSUMER),
            "0.01", "0.03", "0.05", "0.08", "0.10", "0.13", "-0.12", "1.0", 18,
            "defensive cash-flow stability",
            List.of("defensive premium becomes stretched", "USD/KRW move dominates KRW return")),
        asset("00000000-0000-0000-0000-000000000104", "TIGER-SP500", "TIGER US S&P500 ETF", "KRX", AssetType.ETF, KRW,
            List.of("tiger", "s&p500", "sp500", "미국 s&p500", "미국 s&p500 tiger etf"),
            List.of(ExposureTag.KRW_LISTING, ExposureTag.US_EQUITY, ExposureTag.USD_EXPOSURE, ExposureTag.BROAD_US_MARKET, ExposureTag.MEGA_CAP_TECH, ExposureTag.RISK_APPETITE),
            "0.03", "0.08", "0.16", "0.27", "0.16", "0.19", "-0.18", "1.0", 12,
            "US large-cap earnings and dollar exposure",
            List.of("US mega-cap breadth narrows", "KRW strengthens enough to dilute US equity gains")),
        asset("00000000-0000-0000-0000-000000000105", "QQQ", "Invesco QQQ ETF", "NASDAQ", AssetType.ETF, USD,
            List.of("qqq", "nasdaq 100", "나스닥"),
            List.of(ExposureTag.US_EQUITY, ExposureTag.USD_EXPOSURE, ExposureTag.MEGA_CAP_TECH, ExposureTag.AI_CAPEX, ExposureTag.RISK_APPETITE),
            "0.04", "0.12", "0.22", "0.34", "0.21", "0.25", "-0.24", "1.0", 9,
            "US mega-cap technology earnings momentum",
            List.of("mega-cap earnings revisions weaken", "AI capex expectations decline")),
        asset("00000000-0000-0000-0000-000000000106", "SOXL", "Direxion Daily Semiconductor Bull 3X Shares", "NYSEARCA", AssetType.ETF, USD,
            List.of("soxl", "semiconductor 3x", "반도체 3배"),
            List.of(ExposureTag.US_EQUITY, ExposureTag.USD_EXPOSURE, ExposureTag.SEMICONDUCTOR_CYCLE, ExposureTag.AI_CAPEX, ExposureTag.HIGH_VOLATILITY, ExposureTag.LEVERAGED_PRODUCT, ExposureTag.RISK_APPETITE),
            "0.10", "0.31", "0.62", "0.95", "0.78", "0.92", "-0.68", "3.0", 3,
            "short tactical semiconductor momentum",
            List.of("semiconductor momentum reverses", "position is held beyond the tactical review window")),
        asset("00000000-0000-0000-0000-000000000107", "KRW-BTC", "Bitcoin", "UPBIT", AssetType.CRYPTO, KRW,
            List.of("bitcoin", "btc", "비트코인"),
            List.of(ExposureTag.CRYPTO_LIQUIDITY, ExposureTag.RISK_APPETITE, ExposureTag.HIGH_VOLATILITY),
            "0.07", "0.21", "0.38", "0.74", "0.56", "0.64", "-0.52", "1.0", 6,
            "crypto liquidity and risk-appetite cycle",
            List.of("Bitcoin falls more than 20 percent from proposal price", "correlation with US equities rises while volatility remains high")),
        asset("00000000-0000-0000-0000-000000000108", "KRW-ETH", "Ethereum", "UPBIT", AssetType.CRYPTO, KRW,
            List.of("ethereum", "eth", "이더리움"),
            List.of(ExposureTag.CRYPTO_LIQUIDITY, ExposureTag.RISK_APPETITE, ExposureTag.HIGH_VOLATILITY),
            "0.05", "0.18", "0.29", "0.58", "0.60", "0.70", "-0.57", "1.0", 6,
            "crypto platform activity and liquidity cycle",
            List.of("Ethereum underperforms Bitcoin during risk-on period", "network activity proxy weakens"))
    );

    @Override
    public List<Asset> search(String query) {
        if (query == null || query.isBlank()) {
            return assets;
        }
        var normalized = query.toLowerCase(Locale.ROOT);
        return assets.stream()
            .filter(asset -> matches(asset, normalized))
            .toList();
    }

    @Override
    public Asset getFeature(UUID assetId) {
        return findFeature(assetId).orElseThrow(() -> new IllegalArgumentException("asset not found: " + assetId));
    }

    @Override
    public Optional<Asset> findFeature(UUID assetId) {
        return assets.stream().filter(asset -> asset.assetId().equals(assetId)).findFirst();
    }

    private boolean matches(Asset asset, String query) {
        return asset.symbol().toLowerCase(Locale.ROOT).contains(query)
            || asset.displayName().toLowerCase(Locale.ROOT).contains(query)
            || asset.aliases().stream().anyMatch(alias -> alias.toLowerCase(Locale.ROOT).contains(query));
    }

    private static Asset asset(
        String assetId,
        String symbol,
        String displayName,
        String market,
        AssetType assetType,
        Currency currency,
        List<String> aliases,
        List<ExposureTag> exposureTags,
        String return1m,
        String return3m,
        String return6m,
        String return1y,
        String volatility30d,
        String volatility90d,
        String maxDrawdown1y,
        String leverageMultiplier,
        int momentumWindowMonths,
        String catalyst,
        List<String> reviewTriggers
    ) {
        return new Asset(
            UUID.fromString(assetId),
            symbol,
            displayName,
            market,
            assetType,
            currency,
            aliases,
            exposureTags,
            new BigDecimal(return1m),
            new BigDecimal(return3m),
            new BigDecimal(return6m),
            new BigDecimal(return1y),
            new BigDecimal(volatility30d),
            new BigDecimal(volatility90d),
            new BigDecimal(maxDrawdown1y),
            new BigDecimal(leverageMultiplier),
            momentumWindowMonths,
            catalyst,
            reviewTriggers
        );
    }
}
```

- [ ] **Step 7: Run asset tests**

Run:

```bash
./gradlew :portfolio-asset:test
```

Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add portfolio-asset
git commit -m "feat: add mock asset catalog"
```

---

## Task 2: Intent Storage With Selected Asset Snapshots

**Files:**
- Modify: `portfolio-proposal/build.gradle`
- Modify: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/domain/SelectedAsset.java`
- Modify: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/adapter/out/InMemoryPortfolioIntentRepository.java`
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/application/port/in/GetPortfolioIntentUseCase.java`
- Test: `portfolio-proposal/src/test/java/com/hyejin/portfolio/proposal/application/service/CreatePortfolioIntentServiceTest.java`

- [ ] **Step 1: Add asset dependency to `portfolio-proposal`**

In `portfolio-proposal/build.gradle`, include:

```groovy
dependencies {
    implementation project(':portfolio-common')
    implementation project(':portfolio-asset')
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.3'
    testImplementation 'org.assertj:assertj-core:3.26.3'
}
```

- [ ] **Step 2: Write failing intent persistence test**

Create `CreatePortfolioIntentServiceTest.java`:

```java
package com.hyejin.portfolio.proposal.application.service;

import com.hyejin.portfolio.common.Money;
import com.hyejin.portfolio.proposal.adapter.out.InMemoryPortfolioIntentRepository;
import com.hyejin.portfolio.proposal.application.port.in.CreatePortfolioIntentUseCase;
import com.hyejin.portfolio.proposal.domain.ProposalMode;
import com.hyejin.portfolio.proposal.domain.RiskProfile;
import com.hyejin.portfolio.proposal.domain.SelectedAsset;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CreatePortfolioIntentServiceTest {
    @Test
    void createsAndReadsIntentWithSelectedAssetSnapshots() {
        var repository = new InMemoryPortfolioIntentRepository();
        var service = new CreatePortfolioIntentService(repository);
        var selectedAsset = new SelectedAsset(
            UUID.fromString("00000000-0000-0000-0000-000000000104"),
            "TIGER-SP500",
            "TIGER US S&P500 ETF",
            "ETF",
            "KRX",
            Currency.getInstance("KRW"),
            "broad US equity core",
            1
        );

        var intent = service.create(new CreatePortfolioIntentUseCase.Command(
            new Money(new BigDecimal("10000000"), Currency.getInstance("KRW")),
            new Money(new BigDecimal("1000000"), Currency.getInstance("KRW")),
            RiskProfile.GROWTH,
            Currency.getInstance("KRW"),
            ProposalMode.CYCLE_MOMENTUM,
            List.of(selectedAsset)
        ));

        var loaded = repository.get(intent.id());

        assertThat(loaded.id()).isEqualTo(intent.id());
        assertThat(loaded.selectedAssets()).extracting("symbol").containsExactly("TIGER-SP500");
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run:

```bash
./gradlew :portfolio-proposal:test --tests '*CreatePortfolioIntentServiceTest'
```

Expected: FAIL because `get(UUID)` does not exist.

- [ ] **Step 4: Add read use case and repository read method**

Create `GetPortfolioIntentUseCase.java`:

```java
package com.hyejin.portfolio.proposal.application.port.in;

import com.hyejin.portfolio.proposal.domain.PortfolioIntent;

import java.util.UUID;

public interface GetPortfolioIntentUseCase {
    PortfolioIntent get(UUID intentId);
}
```

Modify `InMemoryPortfolioIntentRepository.java`:

```java
package com.hyejin.portfolio.proposal.adapter.out;

import com.hyejin.portfolio.proposal.application.port.in.GetPortfolioIntentUseCase;
import com.hyejin.portfolio.proposal.application.port.out.SavePortfolioIntentPort;
import com.hyejin.portfolio.proposal.domain.PortfolioIntent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPortfolioIntentRepository implements SavePortfolioIntentPort, GetPortfolioIntentUseCase {
    private final ConcurrentHashMap<UUID, PortfolioIntent> intents = new ConcurrentHashMap<>();

    @Override
    public PortfolioIntent save(PortfolioIntent intent) {
        intents.put(intent.id(), intent);
        return intent;
    }

    @Override
    public PortfolioIntent get(UUID intentId) {
        var intent = intents.get(intentId);
        if (intent == null) {
            throw new IllegalArgumentException("portfolio intent not found: " + intentId);
        }
        return intent;
    }
}
```

- [ ] **Step 5: Run proposal tests**

Run:

```bash
./gradlew :portfolio-proposal:test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add portfolio-proposal
git commit -m "feat: persist portfolio intents in memory"
```

---

## Task 3: Exposure Analysis And Signal Detection

**Files:**
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/domain/InsightSignal.java`
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/domain/InsightSignalType.java`
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/application/service/AnalyzePortfolioExposuresService.java`
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/application/service/PortfolioExposureSummary.java`
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/application/service/DetectInsightSignalsService.java`
- Test: `portfolio-proposal/src/test/java/com/hyejin/portfolio/proposal/application/service/DetectInsightSignalsServiceTest.java`

- [ ] **Step 1: Write failing signal detector tests**

Create `DetectInsightSignalsServiceTest.java`:

```java
package com.hyejin.portfolio.proposal.application.service;

import com.hyejin.portfolio.asset.application.service.MockAssetCatalogService;
import com.hyejin.portfolio.proposal.domain.InsightSignalType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DetectInsightSignalsServiceTest {
    private final MockAssetCatalogService catalog = new MockAssetCatalogService();
    private final AnalyzePortfolioExposuresService analyzer = new AnalyzePortfolioExposuresService();
    private final DetectInsightSignalsService detector = new DetectInsightSignalsService();

    @Test
    void detectsOverlappingSemiconductorExposureForSamsungAndSoxl() {
        var assets = List.of(
            catalog.search("samsung").getFirst(),
            catalog.search("soxl").getFirst()
        );

        var signals = detector.detect(analyzer.analyze(assets));

        assertThat(signals).extracting("type").contains(InsightSignalType.OVERLAPPING_EXPOSURE, InsightSignalType.TACTICAL_PRODUCT_MISUSE);
    }

    @Test
    void detectsFalseDiversificationForSp500AndBitcoin() {
        var assets = List.of(
            catalog.search("s&p500").getFirst(),
            catalog.search("bitcoin").getFirst()
        );

        var signals = detector.detect(analyzer.analyze(assets));

        assertThat(signals).extracting("type").contains(InsightSignalType.FALSE_DIVERSIFICATION);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
./gradlew :portfolio-proposal:test --tests '*DetectInsightSignalsServiceTest'
```

Expected: FAIL because exposure and signal classes do not exist.

- [ ] **Step 3: Implement signal records**

Create `InsightSignalType.java`:

```java
package com.hyejin.portfolio.proposal.domain;

public enum InsightSignalType {
    OVERLAPPING_EXPOSURE,
    FALSE_DIVERSIFICATION,
    TACTICAL_PRODUCT_MISUSE,
    MISSING_STABILIZER,
    ALLOCATION_CONVICTION_MISMATCH
}
```

Create `InsightSignal.java`:

```java
package com.hyejin.portfolio.proposal.domain;

import java.util.List;

public record InsightSignal(
    InsightSignalType type,
    String severity,
    String title,
    String userExplanation,
    String dataBasis,
    List<String> affectedSymbols
) {
    public InsightSignal {
        affectedSymbols = List.copyOf(affectedSymbols == null ? List.of() : affectedSymbols);
    }
}
```

- [ ] **Step 4: Implement exposure summary and analyzer**

Create `PortfolioExposureSummary.java`:

```java
package com.hyejin.portfolio.proposal.application.service;

import com.hyejin.portfolio.asset.domain.Asset;
import com.hyejin.portfolio.asset.domain.ExposureTag;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record PortfolioExposureSummary(
    List<Asset> assets,
    Map<ExposureTag, BigDecimal> tagExposure,
    BigDecimal leverageAdjustedExposure,
    boolean hasHighVolatilityAsset,
    boolean hasStabilizer
) {
}
```

Create `AnalyzePortfolioExposuresService.java`:

```java
package com.hyejin.portfolio.proposal.application.service;

import com.hyejin.portfolio.asset.domain.Asset;
import com.hyejin.portfolio.asset.domain.ExposureTag;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.EnumMap;
import java.util.List;

public class AnalyzePortfolioExposuresService {
    public PortfolioExposureSummary analyze(List<Asset> assets) {
        if (assets == null || assets.isEmpty()) {
            throw new IllegalArgumentException("assets must not be empty");
        }
        var equalWeight = BigDecimal.ONE.divide(BigDecimal.valueOf(assets.size()), MathContext.DECIMAL64);
        var tagExposure = new EnumMap<ExposureTag, BigDecimal>(ExposureTag.class);
        var leverageAdjustedExposure = BigDecimal.ZERO;
        var hasHighVolatility = false;
        var hasStabilizer = false;

        for (var asset : assets) {
            var adjustedWeight = equalWeight.multiply(asset.leverageMultiplier(), MathContext.DECIMAL64);
            leverageAdjustedExposure = leverageAdjustedExposure.add(adjustedWeight);
            for (var tag : asset.exposureTags()) {
                tagExposure.merge(tag, adjustedWeight, BigDecimal::add);
            }
            hasHighVolatility = hasHighVolatility || asset.exposureTags().contains(ExposureTag.HIGH_VOLATILITY);
            hasStabilizer = hasStabilizer || asset.exposureTags().contains(ExposureTag.DEFENSIVE_CONSUMER);
        }

        return new PortfolioExposureSummary(List.copyOf(assets), tagExposure, leverageAdjustedExposure, hasHighVolatility, hasStabilizer);
    }
}
```

- [ ] **Step 5: Implement signal detector**

Create `DetectInsightSignalsService.java`:

```java
package com.hyejin.portfolio.proposal.application.service;

import com.hyejin.portfolio.asset.domain.ExposureTag;
import com.hyejin.portfolio.proposal.domain.InsightSignal;
import com.hyejin.portfolio.proposal.domain.InsightSignalType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DetectInsightSignalsService {
    public List<InsightSignal> detect(PortfolioExposureSummary summary) {
        var signals = new ArrayList<InsightSignal>();

        if (exposure(summary, ExposureTag.SEMICONDUCTOR_CYCLE).compareTo(new BigDecimal("0.50")) >= 0) {
            signals.add(new InsightSignal(
                InsightSignalType.OVERLAPPING_EXPOSURE,
                "HIGH",
                "Semiconductor cycle is doing more work than it first appears",
                "Several selected assets depend on semiconductor momentum. The issue is not Korea versus US exposure; it is that the same semiconductor-cycle view can drive multiple holdings at once.",
                "Leverage-adjusted semiconductor exposure is " + exposure(summary, ExposureTag.SEMICONDUCTOR_CYCLE).toPlainString(),
                symbolsWith(summary, ExposureTag.SEMICONDUCTOR_CYCLE)
            ));
        }

        if (exposure(summary, ExposureTag.RISK_APPETITE).compareTo(new BigDecimal("0.60")) >= 0
            && exposure(summary, ExposureTag.CRYPTO_LIQUIDITY).compareTo(BigDecimal.ZERO) > 0
            && exposure(summary, ExposureTag.BROAD_US_MARKET).compareTo(BigDecimal.ZERO) > 0) {
            signals.add(new InsightSignal(
                InsightSignalType.FALSE_DIVERSIFICATION,
                "MEDIUM",
                "The crypto position may not be diversifying the US equity ETF",
                "This portfolio looks diversified by label, but the S&P500 ETF and Bitcoin can both depend on the same risk-taking environment. If they move together, Bitcoin increases the same market-mood bet instead of offsetting it.",
                "Mock correlation profile marks S&P500 ETF and Bitcoin as risk-appetite linked",
                symbolsWith(summary, ExposureTag.RISK_APPETITE)
            ));
        }

        if (exposure(summary, ExposureTag.LEVERAGED_PRODUCT).compareTo(BigDecimal.ZERO) > 0) {
            signals.add(new InsightSignal(
                InsightSignalType.TACTICAL_PRODUCT_MISUSE,
                "HIGH",
                "A leveraged product should not silently become the core holding",
                "SOXL is useful only if the user intentionally wants a short tactical semiconductor bet. If it is treated like a normal ETF, the portfolio can become much more concentrated than the raw weight suggests.",
                "Mock leverage profile uses a 3.0x multiplier for SOXL",
                symbolsWith(summary, ExposureTag.LEVERAGED_PRODUCT)
            ));
        }

        if (summary.hasHighVolatilityAsset() && !summary.hasStabilizer()) {
            signals.add(new InsightSignal(
                InsightSignalType.MISSING_STABILIZER,
                "MEDIUM",
                "The portfolio is missing a role that behaves differently",
                "The selected assets lean toward growth, cycle, liquidity, or high price swings. A complement should be chosen for a different role, not because adding another asset automatically improves diversification.",
                "No selected asset has defensive-consumer or stabilizer mock tags",
                List.of()
            ));
        }

        return List.copyOf(signals);
    }

    private BigDecimal exposure(PortfolioExposureSummary summary, ExposureTag tag) {
        return summary.tagExposure().getOrDefault(tag, BigDecimal.ZERO);
    }

    private List<String> symbolsWith(PortfolioExposureSummary summary, ExposureTag tag) {
        return summary.assets().stream()
            .filter(asset -> asset.exposureTags().contains(tag))
            .map(asset -> asset.symbol())
            .toList();
    }
}
```

- [ ] **Step 6: Run signal tests**

Run:

```bash
./gradlew :portfolio-proposal:test --tests '*DetectInsightSignalsServiceTest'
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add portfolio-proposal
git commit -m "feat: detect portfolio insight signals"
```

---

## Task 4: Proposal Generation, Actions, Horizon, And Evidence

**Files:**
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/domain/ProposalStatus.java`
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/domain/ProposalAction.java`
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/domain/HorizonRationale.java`
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/domain/PortfolioProposal.java`
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/application/port/in/CreatePortfolioProposalUseCase.java`
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/application/port/in/GetPortfolioProposalUseCase.java`
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/application/service/GenerateProposalActionsService.java`
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/application/service/CreatePortfolioProposalService.java`
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/adapter/out/InMemoryPortfolioProposalRepository.java`
- Test: `portfolio-proposal/src/test/java/com/hyejin/portfolio/proposal/application/service/CreatePortfolioProposalServiceTest.java`

- [ ] **Step 1: Write failing proposal generation test**

Create `CreatePortfolioProposalServiceTest.java`:

```java
package com.hyejin.portfolio.proposal.application.service;

import com.hyejin.portfolio.asset.application.service.MockAssetCatalogService;
import com.hyejin.portfolio.proposal.adapter.out.InMemoryPortfolioProposalRepository;
import com.hyejin.portfolio.proposal.application.port.in.CreatePortfolioProposalUseCase;
import com.hyejin.portfolio.proposal.domain.ProposalStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CreatePortfolioProposalServiceTest {
    @Test
    void createsCompletedProposalWithNonGenericSignalsActionsAndHorizon() {
        var catalog = new MockAssetCatalogService();
        var repository = new InMemoryPortfolioProposalRepository();
        var service = new CreatePortfolioProposalService(
            catalog,
            repository,
            new AnalyzePortfolioExposuresService(),
            new DetectInsightSignalsService(),
            new GenerateProposalActionsService()
        );

        var proposal = service.create(new CreatePortfolioProposalUseCase.Command(
            UUID.randomUUID(),
            List.of(
                catalog.search("s&p500").getFirst().assetId(),
                catalog.search("bitcoin").getFirst().assetId()
            )
        ));

        assertThat(proposal.status()).isEqualTo(ProposalStatus.COMPLETED);
        assertThat(proposal.signals()).isNotEmpty();
        assertThat(proposal.actions()).isNotEmpty();
        assertThat(proposal.horizonRationale().recommendedHorizonMonths()).isEqualTo(9);
        assertThat(proposal.summary()).contains("risk-taking environment");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
./gradlew :portfolio-proposal:test --tests '*CreatePortfolioProposalServiceTest'
```

Expected: FAIL because proposal classes do not exist.

- [ ] **Step 3: Implement proposal domain records**

Create `ProposalStatus.java`:

```java
package com.hyejin.portfolio.proposal.domain;

public enum ProposalStatus {
    QUEUED,
    COLLECTING_DATA,
    ANALYZING_ASSETS,
    BUILDING_SCENARIOS,
    GENERATING_EVIDENCE,
    COMPLETED,
    FAILED
}
```

Create `ProposalAction.java`:

```java
package com.hyejin.portfolio.proposal.domain;

import java.util.List;
import java.util.UUID;

public record ProposalAction(
    String actionType,
    String title,
    String userExplanation,
    String reviewTrigger,
    List<String> affectedSymbols,
    List<UUID> evidenceIds
) {
    public ProposalAction {
        affectedSymbols = List.copyOf(affectedSymbols == null ? List.of() : affectedSymbols);
        evidenceIds = List.copyOf(evidenceIds == null ? List.of() : evidenceIds);
    }
}
```

Create `HorizonRationale.java`:

```java
package com.hyejin.portfolio.proposal.domain;

import java.util.List;

public record HorizonRationale(
    int recommendedHorizonMonths,
    int reviewAfterMonths,
    String userExplanation,
    List<String> primaryDrivers,
    List<String> reviewTriggers
) {
    public HorizonRationale {
        primaryDrivers = List.copyOf(primaryDrivers == null ? List.of() : primaryDrivers);
        reviewTriggers = List.copyOf(reviewTriggers == null ? List.of() : reviewTriggers);
    }
}
```

Create `PortfolioProposal.java`:

```java
package com.hyejin.portfolio.proposal.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PortfolioProposal(
    UUID proposalId,
    UUID intentId,
    ProposalStatus status,
    String summary,
    HorizonRationale horizonRationale,
    List<InsightSignal> signals,
    List<ProposalAction> actions,
    Instant createdAt
) {
    public PortfolioProposal {
        signals = List.copyOf(signals == null ? List.of() : signals);
        actions = List.copyOf(actions == null ? List.of() : actions);
    }
}
```

- [ ] **Step 4: Implement proposal use case interfaces and repository**

Create `CreatePortfolioProposalUseCase.java`:

```java
package com.hyejin.portfolio.proposal.application.port.in;

import com.hyejin.portfolio.proposal.domain.PortfolioProposal;

import java.util.List;
import java.util.UUID;

public interface CreatePortfolioProposalUseCase {
    PortfolioProposal create(Command command);

    record Command(UUID intentId, List<UUID> assetIds) {
    }
}
```

Create `GetPortfolioProposalUseCase.java`:

```java
package com.hyejin.portfolio.proposal.application.port.in;

import com.hyejin.portfolio.proposal.domain.PortfolioProposal;

import java.util.UUID;

public interface GetPortfolioProposalUseCase {
    PortfolioProposal get(UUID proposalId);
}
```

Create `InMemoryPortfolioProposalRepository.java`:

```java
package com.hyejin.portfolio.proposal.adapter.out;

import com.hyejin.portfolio.proposal.application.port.in.GetPortfolioProposalUseCase;
import com.hyejin.portfolio.proposal.domain.PortfolioProposal;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPortfolioProposalRepository implements GetPortfolioProposalUseCase {
    private final ConcurrentHashMap<UUID, PortfolioProposal> proposals = new ConcurrentHashMap<>();

    public PortfolioProposal save(PortfolioProposal proposal) {
        proposals.put(proposal.proposalId(), proposal);
        return proposal;
    }

    @Override
    public PortfolioProposal get(UUID proposalId) {
        var proposal = proposals.get(proposalId);
        if (proposal == null) {
            throw new IllegalArgumentException("portfolio proposal not found: " + proposalId);
        }
        return proposal;
    }
}
```

- [ ] **Step 5: Implement action and proposal services**

Create `GenerateProposalActionsService.java`:

```java
package com.hyejin.portfolio.proposal.application.service;

import com.hyejin.portfolio.proposal.domain.InsightSignal;
import com.hyejin.portfolio.proposal.domain.InsightSignalType;
import com.hyejin.portfolio.proposal.domain.ProposalAction;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class GenerateProposalActionsService {
    public List<ProposalAction> generate(List<InsightSignal> signals) {
        return signals.stream().map(this::toAction).toList();
    }

    private ProposalAction toAction(InsightSignal signal) {
        var evidenceIds = List.of(evidenceIdFor(signal));
        if (signal.type() == InsightSignalType.FALSE_DIVERSIFICATION) {
            return new ProposalAction(
                "CAP_OR_RECLASSIFY",
                "Do not count the crypto position as diversification until the relationship weakens",
                "Keep the broad US equity ETF as the core holding and cap Bitcoin unless the user explicitly wants a stronger risk-appetite bet.",
                "Review if Bitcoin stops moving with US equities in the mock correlation profile.",
                signal.affectedSymbols(),
                evidenceIds
            );
        }
        if (signal.type() == InsightSignalType.TACTICAL_PRODUCT_MISUSE) {
            return new ProposalAction(
                "CLASSIFY_TACTICAL",
                "Treat the leveraged ETF as tactical exposure",
                "Use SOXL only for a short semiconductor view. Do not let it become the main way the portfolio expresses a long-term AI or chip thesis.",
                "Review if the position is still held after the tactical momentum window.",
                signal.affectedSymbols(),
                evidenceIds
            );
        }
        return new ProposalAction(
            "REDUCE_OVERLAP",
            "Reduce one side of the overlapping exposure",
            "Keep the intended thesis, but avoid expressing it through too many assets that depend on the same driver.",
            "Review if the shared driver weakens or one asset no longer tracks it.",
            signal.affectedSymbols(),
            evidenceIds
        );
    }

    private UUID evidenceIdFor(InsightSignal signal) {
        var source = signal.type().name() + ":" + String.join(",", signal.affectedSymbols());
        return UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8));
    }
}
```

Create `CreatePortfolioProposalService.java`:

```java
package com.hyejin.portfolio.proposal.application.service;

import com.hyejin.portfolio.asset.application.port.in.GetAssetFeatureUseCase;
import com.hyejin.portfolio.asset.domain.Asset;
import com.hyejin.portfolio.proposal.adapter.out.InMemoryPortfolioProposalRepository;
import com.hyejin.portfolio.proposal.application.port.in.CreatePortfolioProposalUseCase;
import com.hyejin.portfolio.proposal.domain.HorizonRationale;
import com.hyejin.portfolio.proposal.domain.PortfolioProposal;
import com.hyejin.portfolio.proposal.domain.ProposalStatus;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class CreatePortfolioProposalService implements CreatePortfolioProposalUseCase {
    private final GetAssetFeatureUseCase getAssetFeatureUseCase;
    private final InMemoryPortfolioProposalRepository repository;
    private final AnalyzePortfolioExposuresService analyzer;
    private final DetectInsightSignalsService detector;
    private final GenerateProposalActionsService actionGenerator;

    public CreatePortfolioProposalService(
        GetAssetFeatureUseCase getAssetFeatureUseCase,
        InMemoryPortfolioProposalRepository repository,
        AnalyzePortfolioExposuresService analyzer,
        DetectInsightSignalsService detector,
        GenerateProposalActionsService actionGenerator
    ) {
        this.getAssetFeatureUseCase = getAssetFeatureUseCase;
        this.repository = repository;
        this.analyzer = analyzer;
        this.detector = detector;
        this.actionGenerator = actionGenerator;
    }

    @Override
    public PortfolioProposal create(Command command) {
        var assets = command.assetIds().stream().map(getAssetFeatureUseCase::getFeature).toList();
        var exposure = analyzer.analyze(assets);
        var signals = detector.detect(exposure);
        var actions = actionGenerator.generate(signals);
        var horizon = buildHorizon(assets);
        var summary = signals.isEmpty()
            ? "The selected assets do not trigger a major mock overlap signal."
            : signals.getFirst().userExplanation();

        return repository.save(new PortfolioProposal(
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

    private HorizonRationale buildHorizon(List<Asset> assets) {
        var recommended = assets.stream()
            .map(Asset::momentumWindowMonths)
            .sorted()
            .skip(Math.max(0, assets.size() - 1L))
            .findFirst()
            .orElse(12);
        var reviewAfter = assets.stream().map(Asset::momentumWindowMonths).min(Comparator.naturalOrder()).orElse(6);
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
```

- [ ] **Step 6: Run proposal service tests**

Run:

```bash
./gradlew :portfolio-proposal:test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add portfolio-proposal
git commit -m "feat: generate mock portfolio proposals"
```

---

## Task 5: Mock Evidence Detail Read Model

**Files:**
- Create: `portfolio-evidence/src/main/java/com/hyejin/portfolio/evidence/domain/EvidenceDetail.java`
- Create: `portfolio-evidence/src/main/java/com/hyejin/portfolio/evidence/application/port/in/GetEvidenceDetailUseCase.java`
- Create: `portfolio-evidence/src/main/java/com/hyejin/portfolio/evidence/application/service/MockEvidenceDetailService.java`
- Test: `portfolio-evidence/src/test/java/com/hyejin/portfolio/evidence/application/service/MockEvidenceDetailServiceTest.java`

- [ ] **Step 1: Write failing evidence detail test**

Create `MockEvidenceDetailServiceTest.java`:

```java
package com.hyejin.portfolio.evidence.application.service;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MockEvidenceDetailServiceTest {
    @Test
    void returnsFalseDiversificationEvidenceFromDeterministicId() {
        var evidenceId = UUID.nameUUIDFromBytes("FALSE_DIVERSIFICATION:TIGER-SP500,KRW-BTC".getBytes(StandardCharsets.UTF_8));
        var service = new MockEvidenceDetailService();

        var detail = service.get(evidenceId);

        assertThat(detail.claim()).contains("not mainly diversifying");
        assertThat(detail.basis()).contains("mock correlation");
        assertThat(detail.reviewTrigger()).contains("correlation");
        assertThat(detail.affectedSymbols()).contains("TIGER-SP500", "KRW-BTC");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
./gradlew :portfolio-evidence:test --tests '*MockEvidenceDetailServiceTest'
```

Expected: FAIL because evidence detail classes do not exist.

- [ ] **Step 3: Implement evidence detail domain and port**

Create `EvidenceDetail.java`:

```java
package com.hyejin.portfolio.evidence.domain;

import java.util.List;
import java.util.UUID;

public record EvidenceDetail(
    UUID evidenceId,
    String claim,
    String basis,
    String limitation,
    String reviewTrigger,
    List<String> sourceSnapshots,
    List<String> affectedSymbols
) {
    public EvidenceDetail {
        sourceSnapshots = List.copyOf(sourceSnapshots == null ? List.of() : sourceSnapshots);
        affectedSymbols = List.copyOf(affectedSymbols == null ? List.of() : affectedSymbols);
    }
}
```

Create `GetEvidenceDetailUseCase.java`:

```java
package com.hyejin.portfolio.evidence.application.port.in;

import com.hyejin.portfolio.evidence.domain.EvidenceDetail;

import java.util.UUID;

public interface GetEvidenceDetailUseCase {
    EvidenceDetail get(UUID evidenceId);
}
```

- [ ] **Step 4: Implement deterministic mock evidence service**

Create `MockEvidenceDetailService.java`:

```java
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
```

- [ ] **Step 5: Run evidence tests**

Run:

```bash
./gradlew :portfolio-evidence:test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add portfolio-evidence
git commit -m "feat: add mock evidence detail read model"
```

---

## Task 6: API Wiring For Postman Flow

**Files:**
- Modify: `portfolio-api/build.gradle`
- Create: `portfolio-api/src/main/java/com/hyejin/portfolio/api/config/MockPortfolioConfiguration.java`
- Create: `portfolio-api/src/main/java/com/hyejin/portfolio/api/adapter/in/web/AssetController.java`
- Modify: `portfolio-api/src/main/java/com/hyejin/portfolio/api/adapter/in/web/PortfolioIntentController.java`
- Modify: `portfolio-api/src/main/java/com/hyejin/portfolio/api/adapter/in/web/PortfolioProposalController.java`
- Modify: `portfolio-api/src/main/java/com/hyejin/portfolio/api/adapter/in/web/EvidenceController.java`
- Modify: `portfolio-api/src/main/java/com/hyejin/portfolio/api/application/service/ProposalDetailResponse.java`
- Test: `portfolio-api/src/test/java/com/hyejin/portfolio/api/adapter/in/web/PortfolioFlowControllerTest.java`

- [ ] **Step 1: Add WebFlux controller test**

Create `PortfolioFlowControllerTest.java`:

```java
package com.hyejin.portfolio.api.adapter.in.web;

import com.hyejin.portfolio.api.PortfolioApiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

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
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
./gradlew :portfolio-api:test --tests '*PortfolioFlowControllerTest'
```

Expected: FAIL because controllers and beans are not wired.

- [ ] **Step 3: Add API configuration**

Create `MockPortfolioConfiguration.java`:

```java
package com.hyejin.portfolio.api.config;

import com.hyejin.portfolio.asset.application.service.MockAssetCatalogService;
import com.hyejin.portfolio.proposal.adapter.out.InMemoryPortfolioIntentRepository;
import com.hyejin.portfolio.proposal.adapter.out.InMemoryPortfolioProposalRepository;
import com.hyejin.portfolio.proposal.application.service.AnalyzePortfolioExposuresService;
import com.hyejin.portfolio.proposal.application.service.CreatePortfolioIntentService;
import com.hyejin.portfolio.proposal.application.service.CreatePortfolioProposalService;
import com.hyejin.portfolio.proposal.application.service.DetectInsightSignalsService;
import com.hyejin.portfolio.proposal.application.service.GenerateProposalActionsService;
import com.hyejin.portfolio.evidence.application.service.MockEvidenceDetailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MockPortfolioConfiguration {
    @Bean
    MockAssetCatalogService mockAssetCatalogService() {
        return new MockAssetCatalogService();
    }

    @Bean
    InMemoryPortfolioIntentRepository inMemoryPortfolioIntentRepository() {
        return new InMemoryPortfolioIntentRepository();
    }

    @Bean
    CreatePortfolioIntentService createPortfolioIntentService(InMemoryPortfolioIntentRepository repository) {
        return new CreatePortfolioIntentService(repository);
    }

    @Bean
    InMemoryPortfolioProposalRepository inMemoryPortfolioProposalRepository() {
        return new InMemoryPortfolioProposalRepository();
    }

    @Bean
    CreatePortfolioProposalService createPortfolioProposalService(
        MockAssetCatalogService catalog,
        InMemoryPortfolioProposalRepository repository
    ) {
        return new CreatePortfolioProposalService(
            catalog,
            repository,
            new AnalyzePortfolioExposuresService(),
            new DetectInsightSignalsService(),
            new GenerateProposalActionsService()
        );
    }

    @Bean
    MockEvidenceDetailService mockEvidenceDetailService() {
        return new MockEvidenceDetailService();
    }
}
```

- [ ] **Step 4: Add asset search controller**

Create `AssetController.java`:

```java
package com.hyejin.portfolio.api.adapter.in.web;

import com.hyejin.portfolio.asset.application.port.in.SearchAssetsUseCase;
import com.hyejin.portfolio.asset.domain.Asset;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    private final SearchAssetsUseCase searchAssetsUseCase;

    public AssetController(SearchAssetsUseCase searchAssetsUseCase) {
        this.searchAssetsUseCase = searchAssetsUseCase;
    }

    @GetMapping("/search")
    public List<AssetSearchResponse> search(@RequestParam String query) {
        return searchAssetsUseCase.search(query).stream()
            .map(AssetSearchResponse::from)
            .toList();
    }

    public record AssetSearchResponse(
        UUID assetId,
        String symbol,
        String displayName,
        String market,
        String assetType,
        String currency
    ) {
        static AssetSearchResponse from(Asset asset) {
            return new AssetSearchResponse(
                asset.assetId(),
                asset.symbol(),
                asset.displayName(),
                asset.market(),
                asset.assetType().name(),
                asset.currency().getCurrencyCode()
            );
        }
    }
}
```

- [ ] **Step 5: Replace proposal controller with real use case call**

Modify `PortfolioProposalController.java`:

```java
package com.hyejin.portfolio.api.adapter.in.web;

import com.hyejin.portfolio.proposal.adapter.out.InMemoryPortfolioProposalRepository;
import com.hyejin.portfolio.proposal.application.port.in.CreatePortfolioProposalUseCase;
import com.hyejin.portfolio.proposal.application.service.CreatePortfolioProposalService;
import com.hyejin.portfolio.proposal.domain.PortfolioProposal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portfolio-proposals")
public class PortfolioProposalController {
    private final CreatePortfolioProposalService createPortfolioProposalService;
    private final InMemoryPortfolioProposalRepository proposalRepository;

    public PortfolioProposalController(
        CreatePortfolioProposalService createPortfolioProposalService,
        InMemoryPortfolioProposalRepository proposalRepository
    ) {
        this.createPortfolioProposalService = createPortfolioProposalService;
        this.proposalRepository = proposalRepository;
    }

    @GetMapping("/{proposalId}")
    public PortfolioProposal getProposal(@PathVariable UUID proposalId) {
        return proposalRepository.get(proposalId);
    }

    @PostMapping
    public PortfolioProposal createProposal(@RequestBody CreateProposalRequest request) {
        return createPortfolioProposalService.create(new CreatePortfolioProposalUseCase.Command(
            request.intentId(),
            request.assetIds()
        ));
    }

    public record CreateProposalRequest(UUID intentId, List<UUID> assetIds) {
    }
}
```

- [ ] **Step 6: Replace evidence controller with evidence detail use case**

Modify `EvidenceController.java`:

```java
package com.hyejin.portfolio.api.adapter.in.web;

import com.hyejin.portfolio.evidence.application.port.in.GetEvidenceDetailUseCase;
import com.hyejin.portfolio.evidence.domain.EvidenceDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/evidence")
public class EvidenceController {
    private final GetEvidenceDetailUseCase getEvidenceDetailUseCase;

    public EvidenceController(GetEvidenceDetailUseCase getEvidenceDetailUseCase) {
        this.getEvidenceDetailUseCase = getEvidenceDetailUseCase;
    }

    @GetMapping("/{evidenceId}")
    public EvidenceDetail getEvidence(@PathVariable UUID evidenceId) {
        return getEvidenceDetailUseCase.get(evidenceId);
    }
}
```

- [ ] **Step 7: Extend controller test to verify evidence detail**

Add a WebTestClient call to `postmanFlowWorksWithMockData()` after proposal creation:

```java
webTestClient.get()
    .uri("/api/evidence/21417d4d-6b12-32f1-a7cc-02cf7da439bd")
    .exchange()
    .expectStatus().isOk()
    .expectBody()
    .jsonPath("$.claim").value(org.hamcrest.Matchers.containsString("not mainly diversifying"));
```

- [ ] **Step 8: Run API tests**

Run:

```bash
./gradlew :portfolio-api:test
```

Expected: PASS.

- [ ] **Step 9: Commit**

```bash
git add portfolio-api
git commit -m "feat: expose mock portfolio insight flow"
```

---

## Task 7: Verification And Postman Examples

**Files:**
- Create: `docs/postman/mock-portfolio-insight-flow.md`

- [ ] **Step 1: Add Postman test script documentation**

Create `docs/postman/mock-portfolio-insight-flow.md`:

```markdown
# Mock Portfolio Insight Flow

Run the API server:

```bash
./gradlew :portfolio-api:bootRun --args='--server.port=18080'
```

Base URL:

```text
http://localhost:18080
```

## Search Assets

```http
GET /api/assets/search?query=bitcoin
```

## Create Proposal Directly From Mock Asset IDs

```http
POST /api/portfolio-proposals
Content-Type: application/json
```

```json
{
  "intentId": "00000000-0000-0000-0000-000000000999",
  "assetIds": [
    "00000000-0000-0000-0000-000000000104",
    "00000000-0000-0000-0000-000000000107"
  ]
}
```

Expected: `status` is `COMPLETED`, and `signals` includes `FALSE_DIVERSIFICATION`.
```

- [ ] **Step 2: Run full clean test**

Run:

```bash
./gradlew clean test
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Start server and verify endpoint manually**

Run:

```bash
./gradlew :portfolio-api:bootRun --args='--server.port=18080'
```

In another terminal:

```bash
curl -s -X POST http://localhost:18080/api/portfolio-proposals \
  -H 'Content-Type: application/json' \
  -d '{"intentId":"00000000-0000-0000-0000-000000000999","assetIds":["00000000-0000-0000-0000-000000000104","00000000-0000-0000-0000-000000000107"]}'
```

Expected response includes:

```json
{
  "status": "COMPLETED"
}
```

and a signal with:

```json
{
  "type": "FALSE_DIVERSIFICATION"
}
```

- [ ] **Step 4: Commit docs**

```bash
git add docs/postman/mock-portfolio-insight-flow.md
git commit -m "docs: add mock insight postman flow"
```

---

## Self-Review

Spec coverage:

- Full Postman flow is covered by Tasks 1, 2, 4, 5, and 6.
- Mock asset feature data is covered by Task 1.
- Portfolio exposure and relationship diagnosis are covered by Task 3.
- Action generation and horizon rules are covered by Task 4.
- User-facing non-generic insight text is covered by Tasks 3 and 4.
- Evidence detail is covered by Task 5 and wired through API in Task 6.

Placeholder scan:

- No `TBD`, `TODO`, or unspecified implementation steps are used.

Type consistency:

- `Asset`, `ExposureTag`, `InsightSignal`, `ProposalAction`, `HorizonRationale`, and `PortfolioProposal` are defined before use.
- `MockAssetCatalogService` implements both search and feature lookup ports.
- `CreatePortfolioProposalService` receives asset ids directly for the first Postman flow.
