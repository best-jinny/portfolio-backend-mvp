# Portfolio Backend MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the AI-free Java backend MVP skeleton for the portfolio insight service.

**Architecture:** Use a Gradle Java multi-module modular monolith with business-feature modules. `portfolio-api` and `portfolio-worker` are executable app modules; business modules expose use cases through `application/port/in`; business modules do not depend on each other.

**Tech Stack:** Java 21, Spring Boot WebFlux, Spring Data JPA-ready boundaries, PostgreSQL-ready persistence boundaries, Flyway-ready structure, JUnit 5, ArchUnit.

---

## Scope

This plan implements the first backend slice only:

- Gradle multi-module skeleton.
- Architecture dependency rules.
- Core domain model stubs.
- Intent creation API.
- Proposal job creation API.
- Worker execution path for deterministic mock proposal generation.
- Proposal result query composition through `ProposalQueryService`.
- Evidence detail API with mock/in-memory persistence.
- Capital growth projection calculation.
- Domain invariant and API-flow tests.

Real external market data, real LLM research, brokerage integration, authentication, and full database migrations are outside this plan.

## Target Module Structure

```text
portfolio-insight-server
├── settings.gradle
├── build.gradle
├── portfolio-api
├── portfolio-worker
├── portfolio-proposal
├── portfolio-allocation
├── portfolio-simulation
├── portfolio-asset
├── portfolio-evidence
├── portfolio-recommendation
├── portfolio-common
├── portfolio-error
└── portfolio-infrastructure
```

Each business module uses:

```text
src/main/java/com/hyejin/portfolio/<module>/
├── adapter/out
├── application/port/in
├── application/port/out
├── application/service
└── domain
```

---

### Task 1: Gradle Java Multi-Module Skeleton

**Files:**
- Create: `settings.gradle`
- Create: `build.gradle`
- Create: `portfolio-common/build.gradle`
- Create: `portfolio-error/build.gradle`
- Create: `portfolio-proposal/build.gradle`
- Create: `portfolio-allocation/build.gradle`
- Create: `portfolio-simulation/build.gradle`
- Create: `portfolio-asset/build.gradle`
- Create: `portfolio-evidence/build.gradle`
- Create: `portfolio-recommendation/build.gradle`
- Create: `portfolio-infrastructure/build.gradle`
- Create: `portfolio-api/build.gradle`
- Create: `portfolio-worker/build.gradle`

- [ ] **Step 1: Create `settings.gradle`**

```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = 'portfolio-insight-server'

include(
    'portfolio-common',
    'portfolio-error',
    'portfolio-proposal',
    'portfolio-allocation',
    'portfolio-simulation',
    'portfolio-asset',
    'portfolio-evidence',
    'portfolio-recommendation',
    'portfolio-infrastructure',
    'portfolio-api',
    'portfolio-worker'
)
```

- [ ] **Step 2: Create root `build.gradle`**

```groovy
plugins {
    id 'org.springframework.boot' version '3.3.5' apply false
    id 'io.spring.dependency-management' version '1.1.6' apply false
}

subprojects {
    group = 'com.hyejin.portfolio'
    version = '0.0.1-SNAPSHOT'

    repositories {
        mavenCentral()
    }
}
```

- [ ] **Step 3: Create library module build files**

Use this exact file for `portfolio-common/build.gradle`, `portfolio-error/build.gradle`, `portfolio-proposal/build.gradle`, `portfolio-allocation/build.gradle`, `portfolio-simulation/build.gradle`, `portfolio-asset/build.gradle`, `portfolio-evidence/build.gradle`, and `portfolio-recommendation/build.gradle`.

```groovy
plugins {
    id 'java-library'
    id 'io.spring.dependency-management'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.3'
    testImplementation 'org.assertj:assertj-core:3.26.3'
}

dependencyManagement {
    imports {
        mavenBom 'org.springframework.boot:spring-boot-dependencies:3.3.5'
    }
}

tasks.named('test') {
    useJUnitPlatform()
}
```

- [ ] **Step 4: Create `portfolio-infrastructure/build.gradle`**

```groovy
plugins {
    id 'java-library'
    id 'io.spring.dependency-management'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    api 'org.springframework:spring-webflux'
    implementation 'com.fasterxml.jackson.core:jackson-databind'
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.3'
    testImplementation 'org.assertj:assertj-core:3.26.3'
}

dependencyManagement {
    imports {
        mavenBom 'org.springframework.boot:spring-boot-dependencies:3.3.5'
    }
}

tasks.named('test') {
    useJUnitPlatform()
}
```

- [ ] **Step 5: Create executable module build files**

Use this exact file for `portfolio-api/build.gradle` and `portfolio-worker/build.gradle`.

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot'
    id 'io.spring.dependency-management'
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation project(':portfolio-common')
    implementation project(':portfolio-error')
    implementation project(':portfolio-proposal')
    implementation project(':portfolio-allocation')
    implementation project(':portfolio-simulation')
    implementation project(':portfolio-asset')
    implementation project(':portfolio-evidence')
    implementation project(':portfolio-recommendation')
    implementation project(':portfolio-infrastructure')

    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'io.projectreactor:reactor-test'
    testImplementation 'org.assertj:assertj-core:3.26.3'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

- [ ] **Step 6: Run Gradle project discovery**

Run:

```bash
./gradlew projects
```

Expected: all eleven modules appear. If there is no Gradle wrapper yet, run system Gradle:

```bash
gradle projects
```

- [ ] **Step 7: Commit**

```bash
git add settings.gradle build.gradle portfolio-*/build.gradle
git commit -m "chore: create java multi-module project skeleton"
```

---

### Task 2: Common Value Types And Errors

**Files:**
- Create: `portfolio-common/src/main/java/com/hyejin/portfolio/common/Money.java`
- Create: `portfolio-common/src/main/java/com/hyejin/portfolio/common/Percentage.java`
- Create: `portfolio-error/src/main/java/com/hyejin/portfolio/error/ErrorCode.java`
- Create: `portfolio-error/src/main/java/com/hyejin/portfolio/error/PortfolioException.java`
- Test: `portfolio-common/src/test/java/com/hyejin/portfolio/common/PercentageTest.java`

- [ ] **Step 1: Write failing percentage bounds test**

```java
package com.hyejin.portfolio.common;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PercentageTest {
    @Test
    void percentageMustBeBetweenZeroAndOne() {
        assertThat(Percentage.of(new BigDecimal("0.25")).value()).isEqualByComparingTo("0.25");

        assertThatThrownBy(() -> Percentage.of(new BigDecimal("-0.01")))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Percentage.of(new BigDecimal("1.01")))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
```

- [ ] **Step 2: Implement `Percentage`**

```java
package com.hyejin.portfolio.common;

import java.math.BigDecimal;
import java.util.Objects;

public record Percentage(BigDecimal value) {
    public Percentage {
        Objects.requireNonNull(value, "value must not be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("percentage must be greater than or equal to 0");
        }
        if (value.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("percentage must be less than or equal to 1");
        }
    }

    public static Percentage of(BigDecimal value) {
        return new Percentage(value);
    }
}
```

- [ ] **Step 3: Implement `Money`**

```java
package com.hyejin.portfolio.common;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) {
    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("money amount must not be negative");
        }
    }
}
```

- [ ] **Step 4: Implement shared error types**

```java
package com.hyejin.portfolio.error;

public enum ErrorCode {
    INVALID_REQUEST,
    NOT_FOUND,
    PROPOSAL_NOT_READY,
    INTERNAL_ERROR
}
```

```java
package com.hyejin.portfolio.error;

public class PortfolioException extends RuntimeException {
    private final ErrorCode code;

    public PortfolioException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}
```

- [ ] **Step 5: Run tests**

```bash
./gradlew :portfolio-common:test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add portfolio-common portfolio-error
git commit -m "feat: add common value and error types"
```

---

### Task 3: Proposal Domain And Intent Use Case

**Files:**
- Modify: `portfolio-proposal/build.gradle`
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/domain/RiskProfile.java`
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/domain/ProposalMode.java`
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/domain/SelectedAsset.java`
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/domain/PortfolioIntent.java`
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/application/port/in/CreatePortfolioIntentUseCase.java`
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/application/port/out/SavePortfolioIntentPort.java`
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/application/service/CreatePortfolioIntentService.java`
- Create: `portfolio-proposal/src/main/java/com/hyejin/portfolio/proposal/adapter/out/InMemoryPortfolioIntentRepository.java`
- Test: `portfolio-proposal/src/test/java/com/hyejin/portfolio/proposal/domain/PortfolioIntentTest.java`

- [ ] **Step 1: Add common dependency to `portfolio-proposal`**

```groovy
dependencies {
    implementation project(':portfolio-common')
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.3'
    testImplementation 'org.assertj:assertj-core:3.26.3'
}
```

- [ ] **Step 2: Write failing intent invariant test**

```java
package com.hyejin.portfolio.proposal.domain;

import com.hyejin.portfolio.common.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PortfolioIntentTest {
    @Test
    void intentRequiresAtLeastOneSelectedAsset() {
        assertThatThrownBy(() -> PortfolioIntent.create(
            new Money(new BigDecimal("10000000"), Currency.getInstance("KRW")),
            null,
            RiskProfile.GROWTH,
            Currency.getInstance("KRW"),
            ProposalMode.CYCLE_MOMENTUM,
            List.of()
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
```

- [ ] **Step 3: Implement proposal domain**

```java
package com.hyejin.portfolio.proposal.domain;

public enum RiskProfile {
    VERY_CONSERVATIVE,
    CONSERVATIVE,
    BALANCED,
    GROWTH,
    AGGRESSIVE
}
```

```java
package com.hyejin.portfolio.proposal.domain;

public enum ProposalMode {
    CYCLE_MOMENTUM
}
```

```java
package com.hyejin.portfolio.proposal.domain;

import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

public record SelectedAsset(
    UUID assetId,
    String symbol,
    String displayName,
    String assetType,
    String market,
    Currency currency,
    String userThesis,
    int displayOrder
) {
    public SelectedAsset {
        Objects.requireNonNull(assetId, "assetId must not be null");
        Objects.requireNonNull(symbol, "symbol must not be null");
        Objects.requireNonNull(displayName, "displayName must not be null");
        Objects.requireNonNull(assetType, "assetType must not be null");
        Objects.requireNonNull(market, "market must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
    }
}
```

```java
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
```

- [ ] **Step 4: Implement intent use case and in-memory adapter**

```java
package com.hyejin.portfolio.proposal.application.port.in;

import com.hyejin.portfolio.common.Money;
import com.hyejin.portfolio.proposal.domain.PortfolioIntent;
import com.hyejin.portfolio.proposal.domain.ProposalMode;
import com.hyejin.portfolio.proposal.domain.RiskProfile;
import com.hyejin.portfolio.proposal.domain.SelectedAsset;

import java.util.Currency;
import java.util.List;

public interface CreatePortfolioIntentUseCase {
    PortfolioIntent create(Command command);

    record Command(
        Money availableCash,
        Money monthlyContribution,
        RiskProfile riskProfile,
        Currency baseCurrency,
        ProposalMode proposalMode,
        List<SelectedAsset> selectedAssets
    ) {
    }
}
```

```java
package com.hyejin.portfolio.proposal.application.port.out;

import com.hyejin.portfolio.proposal.domain.PortfolioIntent;

public interface SavePortfolioIntentPort {
    PortfolioIntent save(PortfolioIntent intent);
}
```

```java
package com.hyejin.portfolio.proposal.application.service;

import com.hyejin.portfolio.proposal.application.port.in.CreatePortfolioIntentUseCase;
import com.hyejin.portfolio.proposal.application.port.out.SavePortfolioIntentPort;
import com.hyejin.portfolio.proposal.domain.PortfolioIntent;

public class CreatePortfolioIntentService implements CreatePortfolioIntentUseCase {
    private final SavePortfolioIntentPort savePortfolioIntentPort;

    public CreatePortfolioIntentService(SavePortfolioIntentPort savePortfolioIntentPort) {
        this.savePortfolioIntentPort = savePortfolioIntentPort;
    }

    @Override
    public PortfolioIntent create(Command command) {
        var intent = PortfolioIntent.create(
            command.availableCash(),
            command.monthlyContribution(),
            command.riskProfile(),
            command.baseCurrency(),
            command.proposalMode(),
            command.selectedAssets()
        );
        return savePortfolioIntentPort.save(intent);
    }
}
```

```java
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
```

- [ ] **Step 5: Run tests**

```bash
./gradlew :portfolio-proposal:test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add portfolio-proposal
git commit -m "feat: add portfolio intent domain"
```

---

### Task 4: Allocation And Simulation Domains

**Files:**
- Modify: `portfolio-allocation/build.gradle`
- Modify: `portfolio-simulation/build.gradle`
- Create: `portfolio-allocation/src/main/java/com/hyejin/portfolio/allocation/domain/AllocationPlan.java`
- Create: `portfolio-allocation/src/main/java/com/hyejin/portfolio/allocation/domain/ProposedAllocation.java`
- Create: `portfolio-simulation/src/main/java/com/hyejin/portfolio/simulation/domain/CapitalGrowthProjection.java`
- Create: `portfolio-simulation/src/main/java/com/hyejin/portfolio/simulation/domain/CapitalGrowthPoint.java`
- Create: `portfolio-simulation/src/main/java/com/hyejin/portfolio/simulation/domain/Scenario.java`
- Create: `portfolio-simulation/src/main/java/com/hyejin/portfolio/simulation/application/port/in/GenerateCapitalGrowthProjectionUseCase.java`
- Create: `portfolio-simulation/src/main/java/com/hyejin/portfolio/simulation/application/service/GenerateCapitalGrowthProjectionService.java`
- Test: `portfolio-allocation/src/test/java/com/hyejin/portfolio/allocation/domain/AllocationPlanTest.java`
- Test: `portfolio-simulation/src/test/java/com/hyejin/portfolio/simulation/application/service/GenerateCapitalGrowthProjectionServiceTest.java`

- [ ] **Step 1: Add common dependency to allocation and simulation**

Add to both module Gradle files:

```groovy
dependencies {
    implementation project(':portfolio-common')
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.3'
    testImplementation 'org.assertj:assertj-core:3.26.3'
}
```

- [ ] **Step 2: Implement allocation domain**

```java
package com.hyejin.portfolio.allocation.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AllocationPlan(
    UUID id,
    UUID proposalId,
    int recommendedHorizonMonths,
    List<ProposedAllocation> allocations
) {
    public AllocationPlan {
        if (recommendedHorizonMonths <= 0) {
            throw new IllegalArgumentException("recommended horizon must be positive");
        }
        if (allocations == null || allocations.isEmpty()) {
            throw new IllegalArgumentException("allocations must not be empty");
        }
        var sum = allocations.stream()
            .map(allocation -> allocation.initialWeight().value())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(BigDecimal.ONE) != 0) {
            throw new IllegalArgumentException("initial weights must sum to 1");
        }
        allocations = List.copyOf(allocations);
    }
}
```

```java
package com.hyejin.portfolio.allocation.domain;

import com.hyejin.portfolio.common.Percentage;

import java.util.UUID;

public record ProposedAllocation(
    UUID assetId,
    Percentage initialWeight,
    Percentage monthlyWeight,
    String role,
    String rationaleAnchor
) {
}
```

- [ ] **Step 3: Write allocation invariant test**

```java
package com.hyejin.portfolio.allocation.domain;

import com.hyejin.portfolio.common.Percentage;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AllocationPlanTest {
    @Test
    void initialWeightsMustSumToOne() {
        assertThatThrownBy(() -> new AllocationPlan(
            UUID.randomUUID(),
            UUID.randomUUID(),
            12,
            List.of(
                new ProposedAllocation(UUID.randomUUID(), Percentage.of(new BigDecimal("0.40")), null, "core", "r1"),
                new ProposedAllocation(UUID.randomUUID(), Percentage.of(new BigDecimal("0.40")), null, "satellite", "r2")
            )
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
```

- [ ] **Step 4: Implement simulation domain and service**

```java
package com.hyejin.portfolio.simulation.domain;

public enum Scenario {
    BEAR,
    BASE,
    BULL
}
```

```java
package com.hyejin.portfolio.simulation.domain;

import java.math.BigDecimal;

public record CapitalGrowthPoint(
    int month,
    BigDecimal cumulativePrincipal,
    BigDecimal expectedValue,
    BigDecimal expectedProfit
) {
    public CapitalGrowthPoint {
        if (month < 0) {
            throw new IllegalArgumentException("month must not be negative");
        }
        if (cumulativePrincipal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("principal must not be negative");
        }
    }
}
```

```java
package com.hyejin.portfolio.simulation.domain;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record CapitalGrowthProjection(
    UUID id,
    UUID proposalId,
    Scenario scenario,
    List<CapitalGrowthPoint> points
) {
    public CapitalGrowthProjection {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("projection points must not be empty");
        }
        var sorted = points.stream().sorted(Comparator.comparingInt(CapitalGrowthPoint::month)).toList();
        if (!points.equals(sorted)) {
            throw new IllegalArgumentException("projection points must be sorted by month");
        }
        points = List.copyOf(points);
    }
}
```

```java
package com.hyejin.portfolio.simulation.application.port.in;

import com.hyejin.portfolio.simulation.domain.CapitalGrowthProjection;
import com.hyejin.portfolio.simulation.domain.Scenario;

import java.math.BigDecimal;
import java.util.UUID;

public interface GenerateCapitalGrowthProjectionUseCase {
    CapitalGrowthProjection generate(Command command);

    record Command(
        UUID proposalId,
        Scenario scenario,
        BigDecimal initialPrincipal,
        BigDecimal monthlyContribution,
        BigDecimal annualReturnRate,
        int horizonMonths,
        int intervalMonths
    ) {
    }
}
```

```java
package com.hyejin.portfolio.simulation.application.service;

import com.hyejin.portfolio.simulation.application.port.in.GenerateCapitalGrowthProjectionUseCase;
import com.hyejin.portfolio.simulation.domain.CapitalGrowthPoint;
import com.hyejin.portfolio.simulation.domain.CapitalGrowthProjection;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.UUID;

public class GenerateCapitalGrowthProjectionService implements GenerateCapitalGrowthProjectionUseCase {
    @Override
    public CapitalGrowthProjection generate(Command command) {
        if (command.horizonMonths() <= 0) {
            throw new IllegalArgumentException("horizon must be positive");
        }
        if (command.intervalMonths() <= 0) {
            throw new IllegalArgumentException("interval must be positive");
        }

        var monthlyRate = command.annualReturnRate().divide(new BigDecimal("12"), MathContext.DECIMAL64);
        var points = new ArrayList<CapitalGrowthPoint>();
        for (int month = 0; month <= command.horizonMonths(); month += command.intervalMonths()) {
            var principal = command.initialPrincipal().add(command.monthlyContribution().multiply(BigDecimal.valueOf(month)));
            var value = compound(command.initialPrincipal(), monthlyRate, month)
                .add(monthlyContributionFutureValue(command.monthlyContribution(), monthlyRate, month));
            points.add(new CapitalGrowthPoint(month, principal, value, value.subtract(principal)));
        }

        return new CapitalGrowthProjection(UUID.randomUUID(), command.proposalId(), command.scenario(), points);
    }

    private BigDecimal compound(BigDecimal principal, BigDecimal monthlyRate, int months) {
        var value = principal;
        for (int i = 0; i < months; i++) {
            value = value.multiply(BigDecimal.ONE.add(monthlyRate), MathContext.DECIMAL64);
        }
        return value;
    }

    private BigDecimal monthlyContributionFutureValue(BigDecimal contribution, BigDecimal monthlyRate, int months) {
        var value = BigDecimal.ZERO;
        for (int i = 0; i < months; i++) {
            value = value.add(contribution).multiply(BigDecimal.ONE.add(monthlyRate), MathContext.DECIMAL64);
        }
        return value;
    }
}
```

- [ ] **Step 5: Write simulation test**

```java
package com.hyejin.portfolio.simulation.application.service;

import com.hyejin.portfolio.simulation.application.port.in.GenerateCapitalGrowthProjectionUseCase;
import com.hyejin.portfolio.simulation.domain.Scenario;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateCapitalGrowthProjectionServiceTest {
    @Test
    void createsSixMonthIntervalCapitalGrowthPoints() {
        var service = new GenerateCapitalGrowthProjectionService();
        var projection = service.generate(new GenerateCapitalGrowthProjectionUseCase.Command(
            UUID.randomUUID(),
            Scenario.BASE,
            new BigDecimal("10000000"),
            new BigDecimal("1000000"),
            new BigDecimal("0.06"),
            12,
            6
        ));

        assertThat(projection.points()).extracting("month").containsExactly(0, 6, 12);
    }
}
```

- [ ] **Step 6: Run tests**

```bash
./gradlew :portfolio-allocation:test :portfolio-simulation:test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add portfolio-allocation portfolio-simulation
git commit -m "feat: add allocation and simulation domains"
```

---

### Task 5: Evidence Domain And Evidence Links

**Files:**
- Create: `portfolio-evidence/src/main/java/com/hyejin/portfolio/evidence/domain/Claim.java`
- Create: `portfolio-evidence/src/main/java/com/hyejin/portfolio/evidence/domain/ClaimType.java`
- Create: `portfolio-evidence/src/main/java/com/hyejin/portfolio/evidence/domain/Confidence.java`
- Create: `portfolio-evidence/src/main/java/com/hyejin/portfolio/evidence/domain/EvidenceLink.java`
- Create: `portfolio-evidence/src/main/java/com/hyejin/portfolio/evidence/domain/RelationType.java`
- Create: `portfolio-evidence/src/main/java/com/hyejin/portfolio/evidence/application/port/in/CreateEvidenceLinksUseCase.java`
- Create: `portfolio-evidence/src/main/java/com/hyejin/portfolio/evidence/application/service/CreateEvidenceLinksService.java`
- Test: `portfolio-evidence/src/test/java/com/hyejin/portfolio/evidence/domain/ClaimTest.java`

- [ ] **Step 1: Implement evidence domain**

```java
package com.hyejin.portfolio.evidence.domain;

public enum ClaimType {
    FACT,
    ESTIMATE,
    INTERPRETATION
}
```

```java
package com.hyejin.portfolio.evidence.domain;

public enum Confidence {
    LOW,
    MEDIUM,
    HIGH
}
```

```java
package com.hyejin.portfolio.evidence.domain;

import java.util.List;
import java.util.UUID;

public record Claim(
    UUID id,
    String text,
    ClaimType type,
    Confidence confidence,
    List<UUID> sourceIds,
    String limitation
) {
    public Claim {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("claim text must not be blank");
        }
        if (type == ClaimType.FACT && (sourceIds == null || sourceIds.isEmpty())) {
            throw new IllegalArgumentException("FACT claim requires at least one source");
        }
        if (type == ClaimType.INTERPRETATION && (limitation == null || limitation.isBlank())) {
            throw new IllegalArgumentException("INTERPRETATION claim requires limitation or counterpoint");
        }
        sourceIds = sourceIds == null ? List.of() : List.copyOf(sourceIds);
    }
}
```

```java
package com.hyejin.portfolio.evidence.domain;

public enum RelationType {
    SUPPORTS,
    CONTRADICTS,
    CONTEXTUALIZES
}
```

```java
package com.hyejin.portfolio.evidence.domain;

import java.util.UUID;

public record EvidenceLink(
    UUID id,
    UUID claimId,
    String targetModule,
    String targetType,
    String targetId,
    String targetAnchor,
    RelationType relationType
) {
}
```

- [ ] **Step 2: Write claim invariant test**

```java
package com.hyejin.portfolio.evidence.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimTest {
    @Test
    void factClaimRequiresSource() {
        assertThatThrownBy(() -> new Claim(
            UUID.randomUUID(),
            "Samsung Electronics is listed on KRX.",
            ClaimType.FACT,
            Confidence.HIGH,
            List.of(),
            null
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
```

- [ ] **Step 3: Implement evidence use case**

```java
package com.hyejin.portfolio.evidence.application.port.in;

import com.hyejin.portfolio.evidence.domain.Claim;
import com.hyejin.portfolio.evidence.domain.EvidenceLink;
import com.hyejin.portfolio.evidence.domain.RelationType;

import java.util.List;
import java.util.UUID;

public interface CreateEvidenceLinksUseCase {
    Result create(Command command);

    record Command(Claim claim, List<TargetReference> targets) {
    }

    record TargetReference(
        String targetModule,
        String targetType,
        String targetId,
        String targetAnchor,
        RelationType relationType
    ) {
    }

    record Result(UUID claimId, List<EvidenceLink> links) {
    }
}
```

```java
package com.hyejin.portfolio.evidence.application.service;

import com.hyejin.portfolio.evidence.application.port.in.CreateEvidenceLinksUseCase;
import com.hyejin.portfolio.evidence.domain.EvidenceLink;

import java.util.UUID;

public class CreateEvidenceLinksService implements CreateEvidenceLinksUseCase {
    @Override
    public Result create(Command command) {
        var links = command.targets().stream()
            .map(target -> new EvidenceLink(
                UUID.randomUUID(),
                command.claim().id(),
                target.targetModule(),
                target.targetType(),
                target.targetId(),
                target.targetAnchor(),
                target.relationType()
            ))
            .toList();
        return new Result(command.claim().id(), links);
    }
}
```

- [ ] **Step 4: Run tests**

```bash
./gradlew :portfolio-evidence:test
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add portfolio-evidence
git commit -m "feat: add evidence claim and link domain"
```

---

### Task 6: API Query Service And Controllers

**Files:**
- Create: `portfolio-api/src/main/java/com/hyejin/portfolio/api/PortfolioApiApplication.java`
- Create: `portfolio-api/src/main/java/com/hyejin/portfolio/api/adapter/in/web/PortfolioIntentController.java`
- Create: `portfolio-api/src/main/java/com/hyejin/portfolio/api/adapter/in/web/PortfolioProposalController.java`
- Create: `portfolio-api/src/main/java/com/hyejin/portfolio/api/adapter/in/web/EvidenceController.java`
- Create: `portfolio-api/src/main/java/com/hyejin/portfolio/api/application/service/ProposalQueryService.java`
- Create: `portfolio-api/src/main/java/com/hyejin/portfolio/api/application/service/ProposalDetailResponse.java`
- Create: `portfolio-api/src/main/java/com/hyejin/portfolio/api/application/service/EvidenceDetailResponse.java`
- Test: `portfolio-api/src/test/java/com/hyejin/portfolio/api/application/service/ProposalQueryServiceTest.java`

- [ ] **Step 1: Create Spring Boot app**

```java
package com.hyejin.portfolio.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PortfolioApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(PortfolioApiApplication.class, args);
    }
}
```

- [ ] **Step 2: Create response DTOs**

```java
package com.hyejin.portfolio.api.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProposalDetailResponse(
    UUID proposalId,
    String status,
    String title,
    int recommendedHorizonMonths,
    List<AllocationResponse> allocations,
    List<CapitalGrowthPointResponse> capitalGrowth
) {
    public record AllocationResponse(UUID assetId, BigDecimal initialWeight, String role) {
    }

    public record CapitalGrowthPointResponse(
        int month,
        BigDecimal cumulativePrincipal,
        BigDecimal expectedValue,
        BigDecimal expectedProfit
    ) {
    }
}
```

```java
package com.hyejin.portfolio.api.application.service;

import java.util.List;
import java.util.UUID;

public record EvidenceDetailResponse(
    UUID evidenceId,
    String claim,
    String claimType,
    String confidence,
    List<String> sources,
    List<String> limitations
) {
}
```

- [ ] **Step 3: Create `ProposalQueryService`**

```java
package com.hyejin.portfolio.api.application.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProposalQueryService {
    public ProposalDetailResponse getProposal(UUID proposalId) {
        return new ProposalDetailResponse(
            proposalId,
            "COMPLETED",
            "Mock cycle momentum portfolio proposal",
            12,
            List.of(),
            List.of()
        );
    }

    public EvidenceDetailResponse getEvidence(UUID evidenceId) {
        return new EvidenceDetailResponse(
            evidenceId,
            "Mock evidence-backed claim",
            "INTERPRETATION",
            "MEDIUM",
            List.of("mock-source"),
            List.of("mock data only")
        );
    }
}
```

- [ ] **Step 4: Create controllers that do not compose directly**

```java
package com.hyejin.portfolio.api.adapter.in.web;

import com.hyejin.portfolio.api.application.service.ProposalQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/portfolio-proposals")
public class PortfolioProposalController {
    private final ProposalQueryService proposalQueryService;

    public PortfolioProposalController(ProposalQueryService proposalQueryService) {
        this.proposalQueryService = proposalQueryService;
    }

    @GetMapping("/{proposalId}")
    public Object getProposal(@PathVariable UUID proposalId) {
        return proposalQueryService.getProposal(proposalId);
    }

    @PostMapping
    public CreateProposalResponse createProposal() {
        return new CreateProposalResponse(UUID.randomUUID(), "QUEUED");
    }

    public record CreateProposalResponse(UUID proposalId, String status) {
    }
}
```

```java
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
```

```java
package com.hyejin.portfolio.api.adapter.in.web;

import com.hyejin.portfolio.api.application.service.ProposalQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/evidence")
public class EvidenceController {
    private final ProposalQueryService proposalQueryService;

    public EvidenceController(ProposalQueryService proposalQueryService) {
        this.proposalQueryService = proposalQueryService;
    }

    @GetMapping("/{evidenceId}")
    public Object getEvidence(@PathVariable UUID evidenceId) {
        return proposalQueryService.getEvidence(evidenceId);
    }
}
```

- [ ] **Step 5: Write query service test**

```java
package com.hyejin.portfolio.api.application.service;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProposalQueryServiceTest {
    @Test
    void returnsCompletedMockProposalDetail() {
        var proposalId = UUID.randomUUID();
        var response = new ProposalQueryService().getProposal(proposalId);

        assertThat(response.proposalId()).isEqualTo(proposalId);
        assertThat(response.status()).isEqualTo("COMPLETED");
    }

    @Test
    void returnsMockEvidenceDetail() {
        var evidenceId = UUID.randomUUID();
        var response = new ProposalQueryService().getEvidence(evidenceId);

        assertThat(response.evidenceId()).isEqualTo(evidenceId);
        assertThat(response.claimType()).isEqualTo("INTERPRETATION");
    }
}
```

- [ ] **Step 6: Run API tests/build**

```bash
./gradlew :portfolio-api:test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add portfolio-api
git commit -m "feat: add proposal query service and controllers"
```

---

### Task 7: Worker Mock Proposal Flow

**Files:**
- Create: `portfolio-worker/src/main/java/com/hyejin/portfolio/worker/PortfolioWorkerApplication.java`
- Create: `portfolio-worker/src/main/java/com/hyejin/portfolio/worker/application/MockProposalJobRunner.java`
- Create: `portfolio-worker/src/main/java/com/hyejin/portfolio/worker/application/MockProposalJobResult.java`
- Test: `portfolio-worker/src/test/java/com/hyejin/portfolio/worker/application/MockProposalJobRunnerTest.java`

- [ ] **Step 1: Create worker app**

```java
package com.hyejin.portfolio.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PortfolioWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(PortfolioWorkerApplication.class, args);
    }
}
```

- [ ] **Step 2: Create mock process manager**

```java
package com.hyejin.portfolio.worker.application;

import java.util.List;
import java.util.UUID;

public class MockProposalJobRunner {
    public MockProposalJobResult run(UUID intentId) {
        return new MockProposalJobResult(
            intentId,
            List.of(
                "VALIDATE_INTENT",
                "RESOLVE_ASSETS",
                "GENERATE_ALLOCATION",
                "SIMULATE_CAPITAL_GROWTH",
                "CREATE_EVIDENCE_LINKS",
                "COMPLETE_PROPOSAL"
            )
        );
    }
}
```

```java
package com.hyejin.portfolio.worker.application;

import java.util.List;
import java.util.UUID;

public record MockProposalJobResult(UUID intentId, List<String> steps) {
}
```

- [ ] **Step 3: Write worker flow test**

```java
package com.hyejin.portfolio.worker.application;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MockProposalJobRunnerTest {
    @Test
    void workerCoordinatesWorkflowStepsWithoutDomainDecisions() {
        var result = new MockProposalJobRunner().run(UUID.randomUUID());

        assertThat(result.steps()).isEqualTo(List.of(
            "VALIDATE_INTENT",
            "RESOLVE_ASSETS",
            "GENERATE_ALLOCATION",
            "SIMULATE_CAPITAL_GROWTH",
            "CREATE_EVIDENCE_LINKS",
            "COMPLETE_PROPOSAL"
        ));
    }
}
```

- [ ] **Step 4: Run worker tests**

```bash
./gradlew :portfolio-worker:test
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add portfolio-worker
git commit -m "feat: add mock proposal worker flow"
```

---

### Task 8: Architecture Tests

**Files:**
- Modify: `portfolio-api/build.gradle`
- Create: `portfolio-api/src/test/java/com/hyejin/portfolio/api/ArchitectureTest.java`

- [ ] **Step 1: Add ArchUnit dependency**

Add to `portfolio-api/build.gradle`:

```groovy
testImplementation 'com.tngtech.archunit:archunit-junit5:1.3.0'
```

- [ ] **Step 2: Create architecture tests**

```java
package com.hyejin.portfolio.api;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {
    private final com.tngtech.archunit.core.domain.JavaClasses classes =
        new ClassFileImporter().importPackages("com.hyejin.portfolio");

    @Test
    void domainPackagesDoNotDependOnSpring() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
            .check(classes);
    }

    @Test
    void apiControllersDoNotDependOnAdapterOutPackages() {
        noClasses()
            .that().resideInAPackage("..api.adapter.in.web..")
            .should().dependOnClassesThat().resideInAPackage("..adapter.out..")
            .check(classes);
    }

    @Test
    void allocationModuleDoesNotDependOnOtherBusinessModules() {
        noClasses()
            .that().resideInAPackage("..allocation..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..asset..",
                "..evidence..",
                "..simulation..",
                "..recommendation..",
                "..proposal.."
            )
            .check(classes);
    }
}
```

- [ ] **Step 3: Run architecture tests**

```bash
./gradlew :portfolio-api:test --tests '*ArchitectureTest'
```

Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add portfolio-api/build.gradle portfolio-api/src/test/java/com/hyejin/portfolio/api/ArchitectureTest.java
git commit -m "test: enforce architecture boundaries"
```

---

## Self-Review

Spec coverage:

- Proposal-first backend: covered by Tasks 3, 6, and 7.
- Business-feature modules: covered by Task 1.
- Small aggregate boundaries: covered by Tasks 3, 4, and 5.
- Claim ownership: covered by Task 5.
- `ProposalQueryService`: covered by Task 6.
- Capital growth projection: covered by Task 4.
- Architecture rules: covered by Task 8.

Known gaps intentionally left for later plans:

- Real external market data ingestion.
- Real LLM evidence generation.
- PostgreSQL/JPA entity mapping and Flyway migrations.
- Authentication.
- Frontend charts.
- User-adjusted allocation simulation UI.

Placeholder scan:

- No `TBD`, `TODO`, or unspecified implementation steps are used.

Type consistency:

- `Percentage`, `Money`, `PortfolioIntent`, `AllocationPlan`, `CapitalGrowthProjection`, `Claim`, and `ProposalQueryService` are introduced before use.
- API DTOs intentionally stay in `portfolio-api`.
- Business modules do not directly reference each other in the planned code.
