# Portfolio Backend MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the AI-free backend MVP skeleton for the portfolio insight service.

**Architecture:** Use a Gradle Kotlin multi-module modular monolith with business-feature modules. `portfolio-api` and `portfolio-worker` are executable app modules; business modules expose use cases through `application/port/in`; business modules do not depend on each other.

**Tech Stack:** Kotlin, Spring Boot WebFlux, Spring Data JPA, PostgreSQL-ready persistence boundaries, Flyway-ready structure, JUnit 5, ArchUnit.

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
├── settings.gradle.kts
├── build.gradle.kts
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
src/main/kotlin/com/hyejin/portfolio/<module>/
├── adapter/out
├── application/port/in
├── application/port/out
├── application/service
└── domain
```

---

### Task 1: Gradle Multi-Module Skeleton

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `portfolio-common/build.gradle.kts`
- Create: `portfolio-error/build.gradle.kts`
- Create: `portfolio-proposal/build.gradle.kts`
- Create: `portfolio-allocation/build.gradle.kts`
- Create: `portfolio-simulation/build.gradle.kts`
- Create: `portfolio-asset/build.gradle.kts`
- Create: `portfolio-evidence/build.gradle.kts`
- Create: `portfolio-recommendation/build.gradle.kts`
- Create: `portfolio-infrastructure/build.gradle.kts`
- Create: `portfolio-api/build.gradle.kts`
- Create: `portfolio-worker/build.gradle.kts`

- [ ] **Step 1: Create `settings.gradle.kts`**

```kotlin
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

rootProject.name = "portfolio-insight-server"

include(
    "portfolio-common",
    "portfolio-error",
    "portfolio-proposal",
    "portfolio-allocation",
    "portfolio-simulation",
    "portfolio-asset",
    "portfolio-evidence",
    "portfolio-recommendation",
    "portfolio-infrastructure",
    "portfolio-api",
    "portfolio-worker",
)
```

- [ ] **Step 2: Create root `build.gradle.kts`**

```kotlin
plugins {
    kotlin("jvm") version "1.9.25" apply false
    kotlin("plugin.spring") version "1.9.25" apply false
    id("org.springframework.boot") version "3.3.5" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
}

subprojects {
    group = "com.hyejin.portfolio"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}
```

- [ ] **Step 3: Create library module Gradle files**

Use this exact file for `portfolio-common/build.gradle.kts`, `portfolio-error/build.gradle.kts`, `portfolio-proposal/build.gradle.kts`, `portfolio-allocation/build.gradle.kts`, `portfolio-simulation/build.gradle.kts`, `portfolio-asset/build.gradle.kts`, `portfolio-evidence/build.gradle.kts`, and `portfolio-recommendation/build.gradle.kts`.

```kotlin
plugins {
    kotlin("jvm")
    id("io.spring.dependency-management")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

- [ ] **Step 4: Create `portfolio-infrastructure/build.gradle.kts`**

```kotlin
plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation("org.springframework:spring-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

- [ ] **Step 5: Create executable module Gradle files**

Use this exact file for `portfolio-api/build.gradle.kts` and `portfolio-worker/build.gradle.kts`.

```kotlin
plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(project(":portfolio-common"))
    implementation(project(":portfolio-error"))
    implementation(project(":portfolio-proposal"))
    implementation(project(":portfolio-allocation"))
    implementation(project(":portfolio-simulation"))
    implementation(project(":portfolio-asset"))
    implementation(project(":portfolio-evidence"))
    implementation(project(":portfolio-recommendation"))
    implementation(project(":portfolio-infrastructure"))

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<Test> {
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
git add settings.gradle.kts build.gradle.kts portfolio-*/build.gradle.kts
git commit -m "chore: create multi-module project skeleton"
```

---

### Task 2: Common Value Types And Errors

**Files:**
- Create: `portfolio-common/src/main/kotlin/com/hyejin/portfolio/common/Money.kt`
- Create: `portfolio-common/src/main/kotlin/com/hyejin/portfolio/common/Percentage.kt`
- Create: `portfolio-error/src/main/kotlin/com/hyejin/portfolio/error/ErrorCode.kt`
- Create: `portfolio-error/src/main/kotlin/com/hyejin/portfolio/error/PortfolioException.kt`
- Test: `portfolio-common/src/test/kotlin/com/hyejin/portfolio/common/PercentageTest.kt`

- [ ] **Step 1: Write failing test for percentage bounds**

```kotlin
package com.hyejin.portfolio.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PercentageTest {
    @Test
    fun `percentage must be between zero and one`() {
        assertEquals(0.25.toBigDecimal(), Percentage.of(0.25.toBigDecimal()).value)
        assertFailsWith<IllegalArgumentException> { Percentage.of((-0.01).toBigDecimal()) }
        assertFailsWith<IllegalArgumentException> { Percentage.of(1.01.toBigDecimal()) }
    }
}
```

- [ ] **Step 2: Implement `Percentage`**

```kotlin
package com.hyejin.portfolio.common

import java.math.BigDecimal

@JvmInline
value class Percentage private constructor(val value: BigDecimal) {
    companion object {
        fun of(value: BigDecimal): Percentage {
            require(value >= BigDecimal.ZERO) { "percentage must be greater than or equal to 0" }
            require(value <= BigDecimal.ONE) { "percentage must be less than or equal to 1" }
            return Percentage(value)
        }
    }
}
```

- [ ] **Step 3: Implement `Money`**

```kotlin
package com.hyejin.portfolio.common

import java.math.BigDecimal
import java.util.Currency

data class Money(
    val amount: BigDecimal,
    val currency: Currency,
) {
    init {
        require(amount >= BigDecimal.ZERO) { "money amount must not be negative" }
    }
}
```

- [ ] **Step 4: Implement shared error types**

```kotlin
package com.hyejin.portfolio.error

enum class ErrorCode {
    INVALID_REQUEST,
    NOT_FOUND,
    PROPOSAL_NOT_READY,
    INTERNAL_ERROR,
}
```

```kotlin
package com.hyejin.portfolio.error

class PortfolioException(
    val code: ErrorCode,
    override val message: String,
) : RuntimeException(message)
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
- Modify: `portfolio-proposal/build.gradle.kts`
- Create: `portfolio-proposal/src/main/kotlin/com/hyejin/portfolio/proposal/domain/RiskProfile.kt`
- Create: `portfolio-proposal/src/main/kotlin/com/hyejin/portfolio/proposal/domain/ProposalMode.kt`
- Create: `portfolio-proposal/src/main/kotlin/com/hyejin/portfolio/proposal/domain/SelectedAsset.kt`
- Create: `portfolio-proposal/src/main/kotlin/com/hyejin/portfolio/proposal/domain/PortfolioIntent.kt`
- Create: `portfolio-proposal/src/main/kotlin/com/hyejin/portfolio/proposal/application/port/in/CreatePortfolioIntentUseCase.kt`
- Create: `portfolio-proposal/src/main/kotlin/com/hyejin/portfolio/proposal/application/port/out/SavePortfolioIntentPort.kt`
- Create: `portfolio-proposal/src/main/kotlin/com/hyejin/portfolio/proposal/application/service/CreatePortfolioIntentService.kt`
- Create: `portfolio-proposal/src/main/kotlin/com/hyejin/portfolio/proposal/adapter/out/InMemoryPortfolioIntentRepository.kt`
- Test: `portfolio-proposal/src/test/kotlin/com/hyejin/portfolio/proposal/domain/PortfolioIntentTest.kt`

- [ ] **Step 1: Add common dependency to `portfolio-proposal`**

```kotlin
dependencies {
    implementation(project(":portfolio-common"))
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
}
```

- [ ] **Step 2: Write failing intent invariant test**

```kotlin
package com.hyejin.portfolio.proposal.domain

import com.hyejin.portfolio.common.Money
import java.math.BigDecimal
import java.util.Currency
import kotlin.test.Test
import kotlin.test.assertFailsWith

class PortfolioIntentTest {
    @Test
    fun `intent requires at least one selected asset`() {
        assertFailsWith<IllegalArgumentException> {
            PortfolioIntent.create(
                availableCash = Money(BigDecimal("10000000"), Currency.getInstance("KRW")),
                monthlyContribution = null,
                riskProfile = RiskProfile.GROWTH,
                baseCurrency = Currency.getInstance("KRW"),
                proposalMode = ProposalMode.CYCLE_MOMENTUM,
                selectedAssets = emptyList(),
            )
        }
    }
}
```

- [ ] **Step 3: Implement proposal domain**

```kotlin
package com.hyejin.portfolio.proposal.domain

enum class RiskProfile {
    VERY_CONSERVATIVE,
    CONSERVATIVE,
    BALANCED,
    GROWTH,
    AGGRESSIVE,
}
```

```kotlin
package com.hyejin.portfolio.proposal.domain

enum class ProposalMode {
    CYCLE_MOMENTUM,
}
```

```kotlin
package com.hyejin.portfolio.proposal.domain

import java.util.Currency
import java.util.UUID

data class SelectedAsset(
    val assetId: UUID,
    val symbol: String,
    val displayName: String,
    val assetType: String,
    val market: String,
    val currency: Currency,
    val userThesis: String?,
    val displayOrder: Int,
)
```

```kotlin
package com.hyejin.portfolio.proposal.domain

import com.hyejin.portfolio.common.Money
import java.time.Instant
import java.util.Currency
import java.util.UUID

data class PortfolioIntent(
    val id: UUID,
    val availableCash: Money,
    val monthlyContribution: Money?,
    val riskProfile: RiskProfile,
    val baseCurrency: Currency,
    val proposalMode: ProposalMode,
    val selectedAssets: List<SelectedAsset>,
    val createdAt: Instant,
) {
    companion object {
        fun create(
            availableCash: Money,
            monthlyContribution: Money?,
            riskProfile: RiskProfile,
            baseCurrency: Currency,
            proposalMode: ProposalMode,
            selectedAssets: List<SelectedAsset>,
        ): PortfolioIntent {
            require(selectedAssets.isNotEmpty()) { "selected assets must not be empty" }
            return PortfolioIntent(
                id = UUID.randomUUID(),
                availableCash = availableCash,
                monthlyContribution = monthlyContribution,
                riskProfile = riskProfile,
                baseCurrency = baseCurrency,
                proposalMode = proposalMode,
                selectedAssets = selectedAssets.sortedBy { it.displayOrder },
                createdAt = Instant.now(),
            )
        }
    }
}
```

- [ ] **Step 4: Implement intent use case and in-memory adapter**

```kotlin
package com.hyejin.portfolio.proposal.application.port.`in`

import com.hyejin.portfolio.common.Money
import com.hyejin.portfolio.proposal.domain.PortfolioIntent
import com.hyejin.portfolio.proposal.domain.ProposalMode
import com.hyejin.portfolio.proposal.domain.RiskProfile
import com.hyejin.portfolio.proposal.domain.SelectedAsset
import java.util.Currency

interface CreatePortfolioIntentUseCase {
    fun create(command: Command): PortfolioIntent

    data class Command(
        val availableCash: Money,
        val monthlyContribution: Money?,
        val riskProfile: RiskProfile,
        val baseCurrency: Currency,
        val proposalMode: ProposalMode,
        val selectedAssets: List<SelectedAsset>,
    )
}
```

```kotlin
package com.hyejin.portfolio.proposal.application.port.out

import com.hyejin.portfolio.proposal.domain.PortfolioIntent

interface SavePortfolioIntentPort {
    fun save(intent: PortfolioIntent): PortfolioIntent
}
```

```kotlin
package com.hyejin.portfolio.proposal.application.service

import com.hyejin.portfolio.proposal.application.port.`in`.CreatePortfolioIntentUseCase
import com.hyejin.portfolio.proposal.application.port.out.SavePortfolioIntentPort
import com.hyejin.portfolio.proposal.domain.PortfolioIntent

class CreatePortfolioIntentService(
    private val savePortfolioIntentPort: SavePortfolioIntentPort,
) : CreatePortfolioIntentUseCase {
    override fun create(command: CreatePortfolioIntentUseCase.Command): PortfolioIntent {
        val intent = PortfolioIntent.create(
            availableCash = command.availableCash,
            monthlyContribution = command.monthlyContribution,
            riskProfile = command.riskProfile,
            baseCurrency = command.baseCurrency,
            proposalMode = command.proposalMode,
            selectedAssets = command.selectedAssets,
        )
        return savePortfolioIntentPort.save(intent)
    }
}
```

```kotlin
package com.hyejin.portfolio.proposal.adapter.out

import com.hyejin.portfolio.proposal.application.port.out.SavePortfolioIntentPort
import com.hyejin.portfolio.proposal.domain.PortfolioIntent
import java.util.concurrent.ConcurrentHashMap

class InMemoryPortfolioIntentRepository : SavePortfolioIntentPort {
    private val intents = ConcurrentHashMap<java.util.UUID, PortfolioIntent>()

    override fun save(intent: PortfolioIntent): PortfolioIntent {
        intents[intent.id] = intent
        return intent
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
- Modify: `portfolio-allocation/build.gradle.kts`
- Modify: `portfolio-simulation/build.gradle.kts`
- Create: `portfolio-allocation/src/main/kotlin/com/hyejin/portfolio/allocation/domain/AllocationPlan.kt`
- Create: `portfolio-simulation/src/main/kotlin/com/hyejin/portfolio/simulation/domain/CapitalGrowthProjection.kt`
- Create: `portfolio-simulation/src/main/kotlin/com/hyejin/portfolio/simulation/application/port/in/GenerateCapitalGrowthProjectionUseCase.kt`
- Create: `portfolio-simulation/src/main/kotlin/com/hyejin/portfolio/simulation/application/service/GenerateCapitalGrowthProjectionService.kt`
- Test: `portfolio-allocation/src/test/kotlin/com/hyejin/portfolio/allocation/domain/AllocationPlanTest.kt`
- Test: `portfolio-simulation/src/test/kotlin/com/hyejin/portfolio/simulation/application/service/GenerateCapitalGrowthProjectionServiceTest.kt`

- [ ] **Step 1: Add common dependency to allocation and simulation**

Add to both module Gradle files:

```kotlin
dependencies {
    implementation(project(":portfolio-common"))
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
}
```

- [ ] **Step 2: Implement `AllocationPlan`**

```kotlin
package com.hyejin.portfolio.allocation.domain

import com.hyejin.portfolio.common.Percentage
import java.util.UUID

data class AllocationPlan(
    val id: UUID,
    val proposalId: UUID,
    val recommendedHorizonMonths: Int,
    val allocations: List<ProposedAllocation>,
) {
    init {
        require(recommendedHorizonMonths > 0) { "recommended horizon must be positive" }
        require(allocations.isNotEmpty()) { "allocations must not be empty" }
        require(allocations.map { it.initialWeight.value }.reduce { acc, value -> acc + value }.compareTo(java.math.BigDecimal.ONE) == 0) {
            "initial weights must sum to 1"
        }
    }
}

data class ProposedAllocation(
    val assetId: UUID,
    val initialWeight: Percentage,
    val monthlyWeight: Percentage?,
    val role: String,
    val rationaleAnchor: String,
)
```

- [ ] **Step 3: Write allocation invariant test**

```kotlin
package com.hyejin.portfolio.allocation.domain

import com.hyejin.portfolio.common.Percentage
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertFailsWith

class AllocationPlanTest {
    @Test
    fun `initial weights must sum to one`() {
        assertFailsWith<IllegalArgumentException> {
            AllocationPlan(
                id = UUID.randomUUID(),
                proposalId = UUID.randomUUID(),
                recommendedHorizonMonths = 12,
                allocations = listOf(
                    ProposedAllocation(UUID.randomUUID(), Percentage.of("0.40".toBigDecimal()), null, "core", "r1"),
                    ProposedAllocation(UUID.randomUUID(), Percentage.of("0.40".toBigDecimal()), null, "satellite", "r2"),
                ),
            )
        }
    }
}
```

- [ ] **Step 4: Implement capital growth projection**

```kotlin
package com.hyejin.portfolio.simulation.domain

import java.math.BigDecimal
import java.util.UUID

data class CapitalGrowthProjection(
    val id: UUID,
    val proposalId: UUID,
    val scenario: Scenario,
    val points: List<CapitalGrowthPoint>,
) {
    init {
        require(points.isNotEmpty()) { "projection points must not be empty" }
        require(points == points.sortedBy { it.month }) { "projection points must be sorted by month" }
    }
}

data class CapitalGrowthPoint(
    val month: Int,
    val cumulativePrincipal: BigDecimal,
    val expectedValue: BigDecimal,
    val expectedProfit: BigDecimal,
) {
    init {
        require(month >= 0) { "month must not be negative" }
        require(cumulativePrincipal >= BigDecimal.ZERO) { "principal must not be negative" }
    }
}

enum class Scenario {
    BEAR,
    BASE,
    BULL,
}
```

```kotlin
package com.hyejin.portfolio.simulation.application.port.`in`

import com.hyejin.portfolio.simulation.domain.CapitalGrowthProjection
import com.hyejin.portfolio.simulation.domain.Scenario
import java.math.BigDecimal
import java.util.UUID

interface GenerateCapitalGrowthProjectionUseCase {
    fun generate(command: Command): CapitalGrowthProjection

    data class Command(
        val proposalId: UUID,
        val scenario: Scenario,
        val initialPrincipal: BigDecimal,
        val monthlyContribution: BigDecimal,
        val annualReturnRate: BigDecimal,
        val horizonMonths: Int,
        val intervalMonths: Int = 6,
    )
}
```

```kotlin
package com.hyejin.portfolio.simulation.application.service

import com.hyejin.portfolio.simulation.application.port.`in`.GenerateCapitalGrowthProjectionUseCase
import com.hyejin.portfolio.simulation.domain.CapitalGrowthPoint
import com.hyejin.portfolio.simulation.domain.CapitalGrowthProjection
import java.math.BigDecimal
import java.math.MathContext
import java.util.UUID

class GenerateCapitalGrowthProjectionService : GenerateCapitalGrowthProjectionUseCase {
    override fun generate(command: GenerateCapitalGrowthProjectionUseCase.Command): CapitalGrowthProjection {
        require(command.horizonMonths > 0) { "horizon must be positive" }
        require(command.intervalMonths > 0) { "interval must be positive" }

        val monthlyRate = command.annualReturnRate.divide(BigDecimal("12"), MathContext.DECIMAL64)
        val points = (0..command.horizonMonths step command.intervalMonths).map { month ->
            val principal = command.initialPrincipal + command.monthlyContribution.multiply(BigDecimal(month))
            val value = compound(command.initialPrincipal, monthlyRate, month) +
                monthlyContributionFutureValue(command.monthlyContribution, monthlyRate, month)
            CapitalGrowthPoint(
                month = month,
                cumulativePrincipal = principal,
                expectedValue = value,
                expectedProfit = value - principal,
            )
        }

        return CapitalGrowthProjection(
            id = UUID.randomUUID(),
            proposalId = command.proposalId,
            scenario = command.scenario,
            points = points,
        )
    }

    private fun compound(principal: BigDecimal, monthlyRate: BigDecimal, months: Int): BigDecimal {
        var value = principal
        repeat(months) {
            value = value.multiply(BigDecimal.ONE + monthlyRate, MathContext.DECIMAL64)
        }
        return value
    }

    private fun monthlyContributionFutureValue(contribution: BigDecimal, monthlyRate: BigDecimal, months: Int): BigDecimal {
        var value = BigDecimal.ZERO
        repeat(months) {
            value = (value + contribution).multiply(BigDecimal.ONE + monthlyRate, MathContext.DECIMAL64)
        }
        return value
    }
}
```

- [ ] **Step 5: Write simulation test**

```kotlin
package com.hyejin.portfolio.simulation.application.service

import com.hyejin.portfolio.simulation.application.port.`in`.GenerateCapitalGrowthProjectionUseCase
import com.hyejin.portfolio.simulation.domain.Scenario
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class GenerateCapitalGrowthProjectionServiceTest {
    @Test
    fun `creates six month interval capital growth points`() {
        val service = GenerateCapitalGrowthProjectionService()
        val projection = service.generate(
            GenerateCapitalGrowthProjectionUseCase.Command(
                proposalId = UUID.randomUUID(),
                scenario = Scenario.BASE,
                initialPrincipal = BigDecimal("10000000"),
                monthlyContribution = BigDecimal("1000000"),
                annualReturnRate = BigDecimal("0.06"),
                horizonMonths = 12,
            ),
        )

        assertEquals(listOf(0, 6, 12), projection.points.map { it.month })
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
- Create: `portfolio-evidence/src/main/kotlin/com/hyejin/portfolio/evidence/domain/Claim.kt`
- Create: `portfolio-evidence/src/main/kotlin/com/hyejin/portfolio/evidence/domain/EvidenceLink.kt`
- Create: `portfolio-evidence/src/main/kotlin/com/hyejin/portfolio/evidence/application/port/in/CreateEvidenceLinksUseCase.kt`
- Create: `portfolio-evidence/src/main/kotlin/com/hyejin/portfolio/evidence/application/service/CreateEvidenceLinksService.kt`
- Test: `portfolio-evidence/src/test/kotlin/com/hyejin/portfolio/evidence/domain/ClaimTest.kt`

- [ ] **Step 1: Implement evidence domain**

```kotlin
package com.hyejin.portfolio.evidence.domain

import java.util.UUID

data class Claim(
    val id: UUID,
    val text: String,
    val type: ClaimType,
    val confidence: Confidence,
    val sourceIds: List<UUID>,
    val limitation: String?,
) {
    init {
        require(text.isNotBlank()) { "claim text must not be blank" }
        if (type == ClaimType.FACT) {
            require(sourceIds.isNotEmpty()) { "FACT claim requires at least one source" }
        }
        if (type == ClaimType.INTERPRETATION) {
            require(!limitation.isNullOrBlank()) { "INTERPRETATION claim requires limitation or counterpoint" }
        }
    }
}

enum class ClaimType {
    FACT,
    ESTIMATE,
    INTERPRETATION,
}

enum class Confidence {
    LOW,
    MEDIUM,
    HIGH,
}
```

```kotlin
package com.hyejin.portfolio.evidence.domain

import java.util.UUID

data class EvidenceLink(
    val id: UUID,
    val claimId: UUID,
    val targetModule: String,
    val targetType: String,
    val targetId: String,
    val targetAnchor: String,
    val relationType: RelationType,
)

enum class RelationType {
    SUPPORTS,
    CONTRADICTS,
    CONTEXTUALIZES,
}
```

- [ ] **Step 2: Write claim invariant test**

```kotlin
package com.hyejin.portfolio.evidence.domain

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ClaimTest {
    @Test
    fun `fact claim requires source`() {
        assertFailsWith<IllegalArgumentException> {
            Claim(
                id = UUID.randomUUID(),
                text = "Samsung Electronics is listed on KRX.",
                type = ClaimType.FACT,
                confidence = Confidence.HIGH,
                sourceIds = emptyList(),
                limitation = null,
            )
        }
    }
}
```

- [ ] **Step 3: Implement evidence use case**

```kotlin
package com.hyejin.portfolio.evidence.application.port.`in`

import com.hyejin.portfolio.evidence.domain.Claim
import com.hyejin.portfolio.evidence.domain.EvidenceLink
import com.hyejin.portfolio.evidence.domain.RelationType
import java.util.UUID

interface CreateEvidenceLinksUseCase {
    fun create(command: Command): Result

    data class Command(
        val claim: Claim,
        val targets: List<TargetReference>,
    )

    data class TargetReference(
        val targetModule: String,
        val targetType: String,
        val targetId: String,
        val targetAnchor: String,
        val relationType: RelationType,
    )

    data class Result(
        val claimId: UUID,
        val links: List<EvidenceLink>,
    )
}
```

```kotlin
package com.hyejin.portfolio.evidence.application.service

import com.hyejin.portfolio.evidence.application.port.`in`.CreateEvidenceLinksUseCase
import com.hyejin.portfolio.evidence.domain.EvidenceLink
import java.util.UUID

class CreateEvidenceLinksService : CreateEvidenceLinksUseCase {
    override fun create(command: CreateEvidenceLinksUseCase.Command): CreateEvidenceLinksUseCase.Result {
        val links = command.targets.map { target ->
            EvidenceLink(
                id = UUID.randomUUID(),
                claimId = command.claim.id,
                targetModule = target.targetModule,
                targetType = target.targetType,
                targetId = target.targetId,
                targetAnchor = target.targetAnchor,
                relationType = target.relationType,
            )
        }
        return CreateEvidenceLinksUseCase.Result(command.claim.id, links)
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
- Create: `portfolio-api/src/main/kotlin/com/hyejin/portfolio/api/PortfolioApiApplication.kt`
- Create: `portfolio-api/src/main/kotlin/com/hyejin/portfolio/api/adapter/in/web/PortfolioIntentController.kt`
- Create: `portfolio-api/src/main/kotlin/com/hyejin/portfolio/api/adapter/in/web/PortfolioProposalController.kt`
- Create: `portfolio-api/src/main/kotlin/com/hyejin/portfolio/api/adapter/in/web/EvidenceController.kt`
- Create: `portfolio-api/src/main/kotlin/com/hyejin/portfolio/api/application/service/ProposalQueryService.kt`
- Create: `portfolio-api/src/main/kotlin/com/hyejin/portfolio/api/application/service/ProposalDetailResponse.kt`
- Test: `portfolio-api/src/test/kotlin/com/hyejin/portfolio/api/application/service/ProposalQueryServiceTest.kt`

- [ ] **Step 1: Create Spring Boot app**

```kotlin
package com.hyejin.portfolio.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PortfolioApiApplication

fun main(args: Array<String>) {
    runApplication<PortfolioApiApplication>(*args)
}
```

- [ ] **Step 2: Create proposal response DTO**

```kotlin
package com.hyejin.portfolio.api.application.service

import java.math.BigDecimal
import java.util.UUID

data class ProposalDetailResponse(
    val proposalId: UUID,
    val status: String,
    val title: String,
    val recommendedHorizonMonths: Int,
    val allocations: List<AllocationResponse>,
    val capitalGrowth: List<CapitalGrowthPointResponse>,
)

data class AllocationResponse(
    val assetId: UUID,
    val initialWeight: BigDecimal,
    val role: String,
)

data class CapitalGrowthPointResponse(
    val month: Int,
    val cumulativePrincipal: BigDecimal,
    val expectedValue: BigDecimal,
    val expectedProfit: BigDecimal,
)

data class EvidenceDetailResponse(
    val evidenceId: UUID,
    val claim: String,
    val claimType: String,
    val confidence: String,
    val sources: List<String>,
    val limitations: List<String>,
)
```

- [ ] **Step 3: Create `ProposalQueryService`**

```kotlin
package com.hyejin.portfolio.api.application.service

import java.util.UUID

class ProposalQueryService {
    fun getProposal(proposalId: UUID): ProposalDetailResponse {
        return ProposalDetailResponse(
            proposalId = proposalId,
            status = "COMPLETED",
            title = "Mock cycle momentum portfolio proposal",
            recommendedHorizonMonths = 12,
            allocations = emptyList(),
            capitalGrowth = emptyList(),
        )
    }

    fun getEvidence(evidenceId: UUID): EvidenceDetailResponse {
        return EvidenceDetailResponse(
            evidenceId = evidenceId,
            claim = "Mock evidence-backed claim",
            claimType = "INTERPRETATION",
            confidence = "MEDIUM",
            sources = listOf("mock-source"),
            limitations = listOf("mock data only"),
        )
    }
}
```

- [ ] **Step 4: Create controllers that do not compose directly**

```kotlin
package com.hyejin.portfolio.api.adapter.`in`.web

import com.hyejin.portfolio.api.application.service.ProposalQueryService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/portfolio-proposals")
class PortfolioProposalController(
    private val proposalQueryService: ProposalQueryService,
) {
    @GetMapping("/{proposalId}")
    fun getProposal(@PathVariable proposalId: UUID) = proposalQueryService.getProposal(proposalId)

    @PostMapping
    fun createProposal(): CreateProposalResponse {
        return CreateProposalResponse(
            proposalId = UUID.randomUUID(),
            status = "QUEUED",
        )
    }
}

data class CreateProposalResponse(
    val proposalId: UUID,
    val status: String,
)
```

```kotlin
package com.hyejin.portfolio.api.adapter.`in`.web

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.UUID

@RestController
@RequestMapping("/api/portfolio-intents")
class PortfolioIntentController {
    @PostMapping
    fun createIntent(@RequestBody request: CreateIntentRequest): CreateIntentResponse {
        return CreateIntentResponse(UUID.randomUUID())
    }
}

data class CreateIntentRequest(
    val availableCash: BigDecimal,
    val monthlyContribution: BigDecimal?,
    val riskProfile: String,
    val assets: List<CreateIntentAssetRequest>,
)

data class CreateIntentAssetRequest(
    val assetId: UUID,
    val thesis: String?,
)

data class CreateIntentResponse(
    val intentId: UUID,
)
```

```kotlin
package com.hyejin.portfolio.api.adapter.`in`.web

import com.hyejin.portfolio.api.application.service.ProposalQueryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/evidence")
class EvidenceController(
    private val proposalQueryService: ProposalQueryService,
) {
    @GetMapping("/{evidenceId}")
    fun getEvidence(@PathVariable evidenceId: UUID) = proposalQueryService.getEvidence(evidenceId)
}
```

- [ ] **Step 5: Register `ProposalQueryService` bean**

Add to `ProposalQueryService`:

```kotlin
import org.springframework.stereotype.Service

@Service
class ProposalQueryService {
    fun getProposal(proposalId: UUID): ProposalDetailResponse {
        return ProposalDetailResponse(
            proposalId = proposalId,
            status = "COMPLETED",
            title = "Mock cycle momentum portfolio proposal",
            recommendedHorizonMonths = 12,
            allocations = emptyList(),
            capitalGrowth = emptyList(),
        )
    }

    fun getEvidence(evidenceId: UUID): EvidenceDetailResponse {
        return EvidenceDetailResponse(
            evidenceId = evidenceId,
            claim = "Mock evidence-backed claim",
            claimType = "INTERPRETATION",
            confidence = "MEDIUM",
            sources = listOf("mock-source"),
            limitations = listOf("mock data only"),
        )
    }
}
```

- [ ] **Step 6: Write query service test**

```kotlin
package com.hyejin.portfolio.api.application.service

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class ProposalQueryServiceTest {
    @Test
    fun `returns completed mock proposal detail`() {
        val proposalId = UUID.randomUUID()
        val response = ProposalQueryService().getProposal(proposalId)

        assertEquals(proposalId, response.proposalId)
        assertEquals("COMPLETED", response.status)
    }

    @Test
    fun `returns mock evidence detail`() {
        val evidenceId = UUID.randomUUID()
        val response = ProposalQueryService().getEvidence(evidenceId)

        assertEquals(evidenceId, response.evidenceId)
        assertEquals("INTERPRETATION", response.claimType)
    }
}
```

- [ ] **Step 7: Run API tests/build**

```bash
./gradlew :portfolio-api:test
```

Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add portfolio-api
git commit -m "feat: add proposal query service and controllers"
```

---

### Task 7: Worker Mock Proposal Flow

**Files:**
- Create: `portfolio-worker/src/main/kotlin/com/hyejin/portfolio/worker/PortfolioWorkerApplication.kt`
- Create: `portfolio-worker/src/main/kotlin/com/hyejin/portfolio/worker/application/MockProposalJobRunner.kt`
- Test: `portfolio-worker/src/test/kotlin/com/hyejin/portfolio/worker/application/MockProposalJobRunnerTest.kt`

- [ ] **Step 1: Create worker app**

```kotlin
package com.hyejin.portfolio.worker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PortfolioWorkerApplication

fun main(args: Array<String>) {
    runApplication<PortfolioWorkerApplication>(*args)
}
```

- [ ] **Step 2: Create mock process manager**

```kotlin
package com.hyejin.portfolio.worker.application

import java.util.UUID

class MockProposalJobRunner {
    fun run(intentId: UUID): MockProposalJobResult {
        return MockProposalJobResult(
            intentId = intentId,
            steps = listOf(
                "VALIDATE_INTENT",
                "RESOLVE_ASSETS",
                "GENERATE_ALLOCATION",
                "SIMULATE_CAPITAL_GROWTH",
                "CREATE_EVIDENCE_LINKS",
                "COMPLETE_PROPOSAL",
            ),
        )
    }
}

data class MockProposalJobResult(
    val intentId: UUID,
    val steps: List<String>,
)
```

- [ ] **Step 3: Write worker flow test**

```kotlin
package com.hyejin.portfolio.worker.application

import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class MockProposalJobRunnerTest {
    @Test
    fun `worker coordinates workflow steps without domain decisions`() {
        val result = MockProposalJobRunner().run(UUID.randomUUID())

        assertEquals(
            listOf(
                "VALIDATE_INTENT",
                "RESOLVE_ASSETS",
                "GENERATE_ALLOCATION",
                "SIMULATE_CAPITAL_GROWTH",
                "CREATE_EVIDENCE_LINKS",
                "COMPLETE_PROPOSAL",
            ),
            result.steps,
        )
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
- Modify: `portfolio-api/build.gradle.kts`
- Create: `portfolio-api/src/test/kotlin/com/hyejin/portfolio/api/ArchitectureTest.kt`

- [ ] **Step 1: Add ArchUnit dependency**

Add to `portfolio-api/build.gradle.kts`:

```kotlin
testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
```

- [ ] **Step 2: Create architecture tests**

```kotlin
package com.hyejin.portfolio.api

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import kotlin.test.Test

class ArchitectureTest {
    private val classes = ClassFileImporter().importPackages("com.hyejin.portfolio")

    @Test
    fun `domain packages do not depend on spring`() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
            .check(classes)
    }

    @Test
    fun `api controllers do not depend on adapter out packages`() {
        noClasses()
            .that().resideInAPackage("..api.adapter.in.web..")
            .should().dependOnClassesThat().resideInAPackage("..adapter.out..")
            .check(classes)
    }

    @Test
    fun `business modules do not depend on each other directly`() {
        noClasses()
            .that().resideInAPackage("..allocation..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..asset..",
                "..evidence..",
                "..simulation..",
                "..recommendation..",
                "..proposal..",
            )
            .check(classes)
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
git add portfolio-api/build.gradle.kts portfolio-api/src/test/kotlin/com/hyejin/portfolio/api/ArchitectureTest.kt
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
