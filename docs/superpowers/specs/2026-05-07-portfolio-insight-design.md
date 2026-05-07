# Portfolio Insight MVP Design

Date: 2026-05-07

## 1. Product Goal

Build a proposal-first portfolio insight service for individual investors.

The user enters assets they are interested in, optional investment theses, available cash, optional monthly contribution, and a five-level risk profile. The service proposes portfolio weights, monthly contribution allocation, an appropriate investment horizon, scenario-based expected outcomes, evidence-backed thesis validation, exposure analysis, and additional assets to consider.

The MVP does not perform automated trading, brokerage integration, live rebalancing alerts, or discretionary portfolio management.

## 2. Product Principles

- AI proposes, but the user decides.
- Every important claim must have evidence, source date, used data points, reasoning, and limitations.
- Fact, estimate, and interpretation must be separated.
- Expected return must be shown as ranges and scenarios, not as a single confident number.
- Risk explanations must be specific. Generic statements such as "semiconductor exposure is high" are not acceptable unless the product explains the exposure path, affected assets, monitoring indicators, and decision-changing conditions.
- The first proposal mode is `CYCLE_MOMENTUM`, but APIs and model names must allow a future `LONG_TERM_WEALTH` mode.

## 3. MVP Input

The user provides:

- Interested assets: Korean stocks, US stocks, ETFs, and crypto assets.
- Optional thesis per asset.
- Available cash.
- Optional monthly contribution.
- Risk profile: `VERY_CONSERVATIVE`, `CONSERVATIVE`, `BALANCED`, `GROWTH`, `AGGRESSIVE`.
- Base currency, initially `KRW`.

The user does not need to choose portfolio weights or investment horizon in the default flow. The AI proposes both.

## 4. MVP Output

The proposal result includes:

- Conclusion summary.
- AI-recommended initial allocation.
- AI-recommended monthly contribution allocation.
- Recommended investment horizon.
- Capital growth simulation graph data.
- Bear/Base/Bull expected return and risk ranges.
- Asset-level thesis validation.
- Portfolio exposure map.
- Purpose-specific additional asset candidates.
- Thesis validation indicators and rebalancing consideration conditions.

Capital growth simulation must show actual money outcomes, not just return percentages:

- Cumulative invested principal.
- Expected evaluated value.
- Expected investment profit.
- Bear/Base/Bull paths at six-month intervals over the recommended horizon.

## 5. Main User Flow

1. User searches and selects assets.
2. User enters optional theses.
3. User enters available cash and optional monthly contribution.
4. User selects a five-level risk profile.
5. User creates a portfolio intent.
6. User requests a portfolio proposal.
7. The backend creates a proposal job.
8. The proposal is generated.
9. User sees conclusion first.
10. User drills into evidence, sources, scenario assumptions, and objections.

## 6. API Style

Use REST with job polling for the MVP. Leave room for WebFlux-based SSE progress streaming.

Core endpoints:

```http
GET  /api/assets/search?query={query}
POST /api/portfolio-intents
GET  /api/portfolio-intents/{intentId}
POST /api/portfolio-proposals
GET  /api/portfolio-proposals/{proposalId}
GET  /api/portfolio-proposals/{proposalId}/events
GET  /api/evidence/{evidenceId}
```

Initial proposal job states:

- `QUEUED`
- `COLLECTING_DATA`
- `ANALYZING_ASSETS`
- `BUILDING_SCENARIOS`
- `GENERATING_EVIDENCE`
- `COMPLETED`
- `FAILED`

## 7. Architecture Style

The project uses a Gradle multi-module structure inspired by an MSA-ready modular monolith.

Module boundaries are business-feature oriented, not purely layer oriented. Each business module uses a lightweight DDD and hexagonal package structure internally.

Top-level modules:

```text
portfolio-api
portfolio-worker
portfolio-proposal
portfolio-allocation
portfolio-simulation
portfolio-asset
portfolio-evidence
portfolio-recommendation
portfolio-common
portfolio-error
portfolio-infrastructure
```

Module responsibilities:

- `portfolio-api`: BFF/API gateway for frontend communication. Owns controllers, request/response DTOs, authentication entry points, and screen-oriented response composition.
- `portfolio-worker`: Executable worker application for async proposal jobs. It orchestrates business module use cases through `application/port/in` interfaces, but does not own business rules.
- `portfolio-proposal`: Owns portfolio intent, proposal job lifecycle, proposal summary, and final proposal assembly state. It does not decide allocations, scenarios, simulations, evidence classification, or recommendations.
- `portfolio-allocation`: Owns recommended initial allocation, monthly contribution split, risk-profile interpretation, recommended investment horizon, and scenario assumption generation.
- `portfolio-simulation`: Owns capital growth projection and cash-flow simulation. It converts allocation, contribution, horizon, and scenario assumptions into graph-ready money paths.
- `portfolio-asset`: Owns asset master data, asset search, symbols, markets, asset type, and basic price snapshots.
- `portfolio-evidence`: Owns claims, evidence items, evidence links, source snapshots, and evidence policy.
- `portfolio-recommendation`: Owns purpose-specific suggestions: stability, return enhancement, gap filling, and caution.
- `portfolio-common`: Contains only truly generic types such as money, percentage, currency code, and date ranges.
- `portfolio-error`: Contains shared error codes and error response models.
- `portfolio-infrastructure`: Contains only shared technical configuration such as WebClient, Redis, Jackson, and observability configuration.

Each business module follows this internal package structure:

```text
adapter/out
application/port/in
application/port/out
application/service
domain
```

The MVP avoids placing every persistence or external adapter inside `portfolio-infrastructure`. Each business module owns its own `adapter/out` implementation where possible. This keeps future MSA extraction easier.

The design favors small modules and small aggregates over broad "manager" modules. Each module should answer one business question:

- `portfolio-proposal`: What proposal is being requested, what state is it in, and what result sections belong to it?
- `portfolio-allocation`: Given assets, risk profile, and evidence-backed assumptions, what weights, horizon, and scenario assumptions are appropriate?
- `portfolio-simulation`: Given cash flows and scenario assumptions, what monetary paths result?
- `portfolio-asset`: What asset is this and what canonical market data identifies it?
- `portfolio-evidence`: What claim is being made and what evidence supports, contradicts, or contextualizes it?
- `portfolio-recommendation`: What additional asset should be considered for a specific purpose?

## 8. Evidence Ownership And Claim Flow

`portfolio-evidence` is the only module that owns `Claim`, `EvidenceItem`, `EvidenceLink`, and evidence classification rules.

Other business modules must not create persisted claims directly and must not classify a statement as `FACT`, `ESTIMATE`, or `INTERPRETATION`. They may produce local explanation text, rationale anchors, or claim drafts as part of their own result, but those drafts are not evidence-domain objects.

Claim creation flow:

1. A business module produces a decision result and local rationale drafts.
2. `portfolio-worker` passes those drafts, source snapshots, and target references to `portfolio-evidence`.
3. `portfolio-evidence` validates, classifies, and persists claims.
4. `portfolio-evidence` creates `EvidenceLink` records from claims to target result sections.
5. Read APIs compose proposal sections with evidence links, but business aggregates do not own claim collections.

Target references use stable identifiers, not direct object references:

```text
targetModule: portfolio-allocation
targetType: ProposedAllocation
targetId: allocation_123
targetAnchor: rationale
relationType: SUPPORTS
```

This keeps `portfolio-allocation`, `portfolio-simulation`, and `portfolio-recommendation` independent from `portfolio-evidence` while still allowing evidence drill-down.

## 9. Dependency Rules

Dependencies must be one-way and layer skipping is forbidden.

Rules:

- `domain` depends on nothing outside itself.
- `application/service` depends on `domain` and ports.
- `application/port/in` defines use cases exposed by the module.
- `application/port/out` defines external needs of the module.
- `adapter/out` implements `application/port/out`.
- `portfolio-api` may call business modules through `application/port/in`.
- `portfolio-worker` may call business modules through `application/port/in` to execute async proposal jobs.
- `portfolio-api` must not call `adapter/out` directly.
- `portfolio-worker` must not call `adapter/out` directly.
- Business modules must not directly depend on other business modules.
- `portfolio-common` must not contain business-specific concepts.
- `portfolio-common` must not depend on other project modules.
- Layer-skipping calls are forbidden.

Examples of forbidden calls:

- `portfolio-api` calling a repository adapter directly.
- `portfolio-api` directly mutating domain entities.
- `portfolio-worker` calling a repository adapter directly.
- `application/service` calling an adapter implementation directly.
- `adapter/out` calling `application/service`.
- `domain` calling ports or Spring components.
- `portfolio-allocation` directly depending on `portfolio-asset`, `portfolio-evidence`, `portfolio-simulation`, or `portfolio-recommendation`.
- `portfolio-proposal` directly depending on `portfolio-allocation`, `portfolio-simulation`, `portfolio-evidence`, or `portfolio-recommendation`.

Architecture rules should be enforced later with ArchUnit tests.

`portfolio-api` and `portfolio-worker` are executable app modules. They may coordinate multiple business modules, but they must not contain domain decisions such as how to assign weights, how to classify evidence, or how to decide whether a suggestion is stability-oriented or return-oriented. Those rules stay inside the relevant business modules.

Business module communication is in-process for the MVP, not network communication. Executable app modules coordinate through `application/port/in` interfaces:

```text
portfolio-api
  -> business module application/port/in

portfolio-worker
  -> portfolio-proposal application/port/in
  -> portfolio-asset application/port/in
  -> portfolio-allocation application/port/in
  -> portfolio-simulation application/port/in
  -> portfolio-evidence application/port/in
  -> portfolio-recommendation application/port/in
```

The worker is a process manager. It may choose the next workflow step, pass outputs from one use case into another, and mark jobs failed or completed. It must not make domain decisions. For example, it may call `GenerateAllocationPlanUseCase`, but it must not choose asset weights itself.

## 10. Proposal Read Composition

`GetPortfolioProposal` returns a composed read model, not a single aggregate.

The full frontend response is assembled by `ProposalQueryService` in the `portfolio-api` application layer. Controllers call only this query service for proposal detail reads.

```text
portfolio-api adapter/in/web controller
  -> portfolio-api application/service/ProposalQueryService
  -> business module application/port/in
```

`ProposalQueryService` composes read-oriented `application/port/in` calls:

```text
ProposalQueryService
  -> portfolio-proposal: proposal header, status, section ids
  -> portfolio-allocation: allocation plan and scenario assumptions
  -> portfolio-simulation: capital growth projection
  -> portfolio-recommendation: purpose-specific suggestions
  -> portfolio-evidence: evidence summaries for linked target references
```

Composition rules:

- Controllers must not compose proposal details directly.
- `ProposalQueryService` may compose response DTOs, but must not make business decisions.
- `portfolio-api` must not call repositories or adapters directly.
- Business modules return their own read models through `application/port/in`.
- Evidence is joined through `EvidenceLink` target references, not through fields embedded in business aggregates.
- The API response can be broad and screen-oriented; aggregate boundaries stay small.

If the proposal read path later becomes expensive or highly specialized, replace `ProposalQueryService` internals with a CQRS read model or materialized view. The controller contract stays stable because the replacement point is the query service, not the controller.

## 11. Technology Stack

Recommended MVP stack:

- Kotlin.
- Spring Boot WebFlux.
- Spring Data JPA.
- PostgreSQL.
- Flyway.
- Redis or DB-backed job queue for async proposal jobs.
- WebClient for external data and LLM calls.
- SSE endpoint reserved for proposal progress.
- JUnit 5.
- Testcontainers.
- ArchUnit.

WebFlux is used primarily for:

- External market data, disclosure, and LLM API calls.
- Parallel I/O-heavy enrichment.
- Future SSE progress streaming.

JPA is preferred for the MVP because the user wants to practice DDD and aggregate modeling. jOOQ can be added later for read models if proposal/evidence/reporting queries become too complex.

## 12. Core Use Cases

### SearchAsset

Search Korean stocks, US stocks, ETFs, and crypto assets.

### CreatePortfolioIntent

Create the user's investment input:

- Assets.
- Optional asset thesis.
- Available cash.
- Optional monthly contribution.
- Base currency.
- Risk profile.
- Proposal mode.

### GeneratePortfolioProposal

Start and coordinate proposal generation for the intent:

- Proposal job lifecycle.
- Final proposal section assembly.
- References to allocation, simulation, evidence, and recommendation results.

### GenerateAllocationPlan

Generate the investment decision core:

- Recommended initial allocation.
- Recommended monthly allocation.
- Recommended investment horizon.
- Bear/Base/Bull scenario assumptions.
- Allocation rationale drafts and local rationale anchors.

### GenerateCapitalGrowthProjection

Generate graph-ready monetary paths:

- Cumulative principal.
- Expected evaluated value range.
- Expected profit range.
- Six-month interval points over the recommended horizon.

### GeneratePurposeSpecificSuggestions

Generate additional assets to consider:

- Stability candidates.
- Return enhancement candidates.
- Gap filling candidates.
- Caution candidates.
- Rationale and counter-argument drafts.

### CreateEvidenceLinks

Validate and persist evidence-domain objects:

- Classify claim drafts into `FACT`, `ESTIMATE`, or `INTERPRETATION`.
- Persist claims and evidence items.
- Link claims to proposal result targets through `EvidenceLink`.

### GetPortfolioProposal

Return the proposal in conclusion-first shape for the frontend.

### GetEvidenceDetail

Return evidence details for a claim or recommendation.

## 13. Domain Model Outline

### portfolio-asset

- `Asset`
- `AssetPriceSnapshot`

`AssetPriceSnapshot` is normalized market data owned by `portfolio-asset`.

Use it for:

- Historical close price.
- Return and volatility calculation.
- Currency-aware price lookup.
- Quantitative simulation inputs.

It stores calculated or normalized market facts, not full source provenance.

### portfolio-proposal

- `PortfolioIntent`
- `SelectedAsset`
- `ProposalJob`
- `PortfolioProposal`

`SelectedAsset` stores the user's selected asset reference as an intent-time snapshot. It must not copy the full `Asset` aggregate.

Fields:

- `assetId`
- `symbol`
- `displayName`
- `assetType`
- `market`
- `currency`
- `userThesis`, nullable
- `displayOrder`

The snapshot fields support reproducibility and UI display if asset master data changes later. Canonical asset identity and asset metadata still belong to `portfolio-asset`.

`PortfolioProposal` is not a large aggregate that owns every result object. It is a proposal header and lifecycle aggregate that references section results by id. API responses may compose a full proposal document, but the aggregate boundary remains smaller than the response shape.

### portfolio-allocation

- `AllocationPlan`
- `ProposedAllocation`
- `ScenarioProjection`

Important invariants:

- Initial allocation weights sum to 100%.
- Monthly allocation weights sum to 100% when monthly contribution exists.
- Important rationales must expose local anchors so `portfolio-evidence` can link claims to them.
- Recommended horizon must be positive.
- Scenario return ranges must be ordered as min <= max.

### portfolio-simulation

- `CapitalGrowthProjection`
- `CapitalGrowthPoint`

Important invariants:

- Principal, expected value, and expected profit ranges cannot create invalid negative principal.
- Capital growth months start at 0 and progress to the recommended horizon.
- Projection points must be ordered by month.
- The simulation module does not decide expected returns, scenario assumptions, or asset weights. It only computes monetary paths from inputs.

### portfolio-evidence

- `Claim`
- `EvidenceItem`
- `EvidenceLink`
- `DataSourceSnapshot`

`DataSourceSnapshot` is source provenance owned by `portfolio-evidence`.

Use it for:

- Raw or normalized source payload reference.
- Source URL, publisher, retrieval time, and content hash.
- Company IR, filings, macro statistics, ETF documents, market data vendor responses, or LLM research inputs used to support a claim.
- Reproducing why a claim was made at proposal generation time.

When market data supports a claim, the normalized price belongs in `AssetPriceSnapshot`, while the vendor response or source metadata used as evidence belongs in `DataSourceSnapshot`.

Evidence rules:

- A `FACT` claim requires at least one source.
- An `ESTIMATE` claim requires method and limitations.
- An `INTERPRETATION` claim requires supporting evidence and a stated limitation or counterpoint.
- Evidence links target other module results through stable target references. Other business modules do not store `ClaimId` as part of their aggregate invariants.

### portfolio-recommendation

- `PortfolioSuggestion`

Suggestion types:

- `STABILITY`
- `RETURN_ENHANCEMENT`
- `GAP_FILLING`
- `CAUTION`

Each suggestion must include local rationale and counter-argument anchors so `portfolio-evidence` can attach claims without making `portfolio-recommendation` depend on evidence-domain objects.

## 14. Data To Store

MVP tables or persisted aggregates:

- Users, if authentication is included.
- Assets.
- Portfolio intents.
- Intent selected assets.
- Proposal jobs.
- Portfolio proposals.
- Allocation plans.
- Proposed allocations.
- Scenario projections.
- Capital growth projections.
- Capital growth points.
- Asset insights.
- Portfolio exposures.
- Portfolio suggestions.
- Claims.
- Evidence items.
- Evidence links.
- Data source snapshots.

Evidence and source snapshot data are required for trust and reproducibility. The product must be able to explain why a proposal was generated with the data available at that time.

## 15. Proposal Generation Pipeline

Initial proposal generation pipeline:

1. Validate intent.
2. Resolve asset metadata.
3. Collect market and source data.
4. Generate asset-level insights and evidence-backed assumptions.
5. Generate scenario assumptions.
6. Propose allocation and monthly contribution split.
7. Propose investment horizon.
8. Simulate capital growth.
9. Build exposure map.
10. Generate purpose-specific suggestions.
11. Convert rationale drafts into claims and evidence links through `portfolio-evidence`.
12. Save proposal and mark job completed.

The pipeline is coordinated by `portfolio-worker`, but each decision belongs to the module that owns the relevant business capability:

- Asset identity and metadata decisions belong to `portfolio-asset`.
- Normalized market price snapshots belong to `portfolio-asset`.
- Source provenance snapshots belong to `portfolio-evidence`.
- Evidence classification and evidence links belong to `portfolio-evidence`.
- Weight, horizon, and scenario assumption decisions belong to `portfolio-allocation`.
- Money-path calculation belongs to `portfolio-simulation`.
- Purpose-specific suggestion classification belongs to `portfolio-recommendation`.
- Proposal lifecycle and final section references belong to `portfolio-proposal`.

Step 3 writes data by responsibility:

- Price/time-series data used for quantitative calculations is persisted as `AssetPriceSnapshot`.
- Source payloads or source metadata used to support claims are persisted as `DataSourceSnapshot`.
- The same external market data response may produce both: normalized prices for calculation and a source snapshot for evidence traceability.

The first implementation may use deterministic mock or fixture-based proposal generation while preserving the final API and domain shapes.

## 16. Out Of Scope For MVP

- Brokerage connection.
- Automated trading.
- Real-time market execution.
- Push/email alerts.
- Tax optimization.
- Full long-term wealth mode.
- User-adjusted weight slider simulation UI.
- Complete external data ingestion automation.

The API and domain model should leave room for these features later.

## 17. First Implementation Slice

The first implementation should build an AI-free backend MVP:

- Gradle multi-module skeleton.
- Architecture dependency rules.
- Core domain models.
- Intent creation API.
- Proposal job creation API.
- Worker execution path for mock proposal generation.
- Mock proposal generator.
- Proposal result API.
- Evidence detail API.
- Capital growth projection calculation.
- Tests for domain invariants and API flow.

This validates the architecture and product shape before adding real external data or LLM research.
