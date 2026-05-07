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
7. The backend creates an analysis job.
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
portfolio-analysis
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
- `portfolio-analysis`: Owns portfolio intent, proposal generation, allocation, scenario projection, recommended horizon, and capital growth simulation.
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

## 8. Dependency Rules

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
- `portfolio-analysis` directly depending on `portfolio-evidence`, `portfolio-asset`, or `portfolio-recommendation`.

Architecture rules should be enforced later with ArchUnit tests.

`portfolio-api` and `portfolio-worker` are executable app modules. They may coordinate multiple business modules, but they must not contain domain decisions such as how to assign weights, how to classify evidence, or how to decide whether a suggestion is stability-oriented or return-oriented. Those rules stay inside the relevant business modules.

## 9. Technology Stack

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

## 10. Core Use Cases

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

Generate a proposal for the intent:

- Recommended initial allocation.
- Recommended monthly allocation.
- Recommended horizon.
- Scenario projections.
- Capital growth simulation.
- Thesis validation.
- Exposure map.
- Purpose-specific suggestions.
- Evidence-linked reasoning.

### GetPortfolioProposal

Return the proposal in conclusion-first shape for the frontend.

### GetEvidenceDetail

Return evidence details for a claim or recommendation.

## 11. Domain Model Outline

### portfolio-asset

- `Asset`
- `AssetPriceSnapshot`

### portfolio-analysis

- `PortfolioIntent`
- `SelectedAsset`
- `PortfolioProposal`
- `ProposedAllocation`
- `ScenarioProjection`
- `CapitalGrowthPoint`

Important invariants:

- Initial allocation weights sum to 100%.
- Monthly allocation weights sum to 100% when monthly contribution exists.
- Principal, expected value, and expected profit ranges cannot create invalid negative principal.
- Capital growth months start at 0 and progress to the recommended horizon.
- Important rationales reference claims or evidence.

### portfolio-evidence

- `Claim`
- `EvidenceItem`
- `EvidenceLink`
- `DataSourceSnapshot`

Evidence rules:

- A `FACT` claim requires at least one source.
- An `ESTIMATE` claim requires method and limitations.
- An `INTERPRETATION` claim requires supporting evidence and a stated limitation or counterpoint.

### portfolio-recommendation

- `PortfolioSuggestion`

Suggestion types:

- `STABILITY`
- `RETURN_ENHANCEMENT`
- `GAP_FILLING`
- `CAUTION`

Each suggestion must include rationale and counter-argument claim references.

## 12. Data To Store

MVP tables or persisted aggregates:

- Users, if authentication is included.
- Assets.
- Portfolio intents.
- Intent selected assets.
- Proposal jobs.
- Portfolio proposals.
- Proposed allocations.
- Scenario projections.
- Capital growth points.
- Asset insights.
- Portfolio exposures.
- Portfolio suggestions.
- Claims.
- Evidence items.
- Evidence links.
- Data source snapshots.

Evidence and source snapshot data are required for trust and reproducibility. The product must be able to explain why a proposal was generated with the data available at that time.

## 13. Analysis Pipeline

Initial generation pipeline:

1. Validate intent.
2. Resolve asset metadata.
3. Collect market and source data.
4. Generate asset-level insights.
5. Generate scenario assumptions.
6. Propose allocation and monthly contribution split.
7. Propose investment horizon.
8. Simulate capital growth.
9. Build exposure map.
10. Generate purpose-specific suggestions.
11. Attach claims and evidence links.
12. Save proposal and mark job completed.

The first implementation may use deterministic mock or fixture-based analysis while preserving the final API and domain shapes.

## 14. Out Of Scope For MVP

- Brokerage connection.
- Automated trading.
- Real-time market execution.
- Push/email alerts.
- Tax optimization.
- Full long-term wealth mode.
- User-adjusted weight slider simulation UI.
- Complete external data ingestion automation.

The API and domain model should leave room for these features later.

## 15. First Implementation Slice

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
