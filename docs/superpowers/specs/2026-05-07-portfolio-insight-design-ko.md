# 포트폴리오 인사이트 MVP 설계

날짜: 2026-05-07

## 1. 제품 목표

개인 투자자를 위한 proposal-first 포트폴리오 인사이트 서비스를 만든다.

사용자는 관심 있는 자산, 선택적 투자 가설, 투자 가능 금액, 선택적 월 적립금, 5단계 위험 성향을 입력한다. 서비스는 포트폴리오 비중, 월 적립 배분, 적절한 투자 기간, 시나리오 기반 기대 결과, 근거 기반 투자 가설 검증, 노출 분석, 추가 검토 자산을 제안한다.

MVP는 자동 매매, 증권사 연동, 실시간 리밸런싱 알림, 일임 운용을 제공하지 않는다.

## 2. 제품 원칙

- AI는 제안하고, 사용자가 결정한다.
- 중요한 모든 주장은 근거, 출처 날짜, 사용한 데이터 포인트, 추론 과정, 한계를 가져야 한다.
- 사실, 추정, 해석은 분리되어야 한다.
- 기대수익률은 하나의 확신에 찬 숫자가 아니라 범위와 시나리오로 보여준다.
- 리스크 설명은 구체적이어야 한다. "반도체 노출이 높다" 같은 일반 문장은 노출 경로, 영향받는 자산, 모니터링 지표, 판단 변경 조건을 설명하지 못하면 허용하지 않는다.
- 첫 proposal mode는 `CYCLE_MOMENTUM`이지만, API와 모델명은 미래의 `LONG_TERM_WEALTH` 모드를 수용할 수 있어야 한다.

## 3. MVP 입력

사용자는 다음을 제공한다.

- 관심 자산: 국내주식, 미국주식, ETF, crypto asset.
- 자산별 선택적 thesis.
- 투자 가능 금액.
- 선택적 월 적립금.
- 위험 성향: `VERY_CONSERVATIVE`, `CONSERVATIVE`, `BALANCED`, `GROWTH`, `AGGRESSIVE`.
- 기준 통화. 초기값은 `KRW`.

기본 흐름에서 사용자는 포트폴리오 비중이나 투자 기간을 직접 고를 필요가 없다. AI가 둘 다 제안한다.

## 4. MVP 출력

proposal 결과는 다음을 포함한다.

- 결론 요약.
- AI 추천 초기 투자 배분.
- AI 추천 월 적립 배분.
- 추천 투자 기간.
- 자산 성장 시뮬레이션 그래프 데이터.
- Bear/Base/Bull 기대수익률 및 리스크 범위.
- 자산별 thesis 검증.
- 포트폴리오 노출 지도.
- 목적별 추가 검토 자산 후보.
- thesis 검증 지표와 리밸런싱 검토 조건.

자산 성장 시뮬레이션은 단순 수익률이 아니라 실제 금액 결과를 보여줘야 한다.

- 누적 투자 원금.
- 예상 평가금액.
- 예상 투자수익.
- 추천 투자 기간 동안 6개월 단위 Bear/Base/Bull 경로.

## 5. 주요 사용자 흐름

1. 사용자가 자산을 검색하고 선택한다.
2. 사용자가 선택적으로 thesis를 입력한다.
3. 사용자가 투자 가능 금액과 선택적 월 적립금을 입력한다.
4. 사용자가 5단계 위험 성향을 선택한다.
5. 사용자가 portfolio intent를 생성한다.
6. 사용자가 portfolio proposal을 요청한다.
7. 백엔드가 proposal job을 생성한다.
8. proposal이 생성된다.
9. 사용자가 결론을 먼저 확인한다.
10. 사용자가 근거, 출처, 시나리오 가정, 반론을 drill-down한다.

## 6. API 스타일

MVP는 REST와 job polling을 사용한다. WebFlux 기반 SSE 진행률 스트리밍을 붙일 수 있는 여지는 남긴다.

핵심 endpoint:

```http
GET  /api/assets/search?query={query}
POST /api/portfolio-intents
GET  /api/portfolio-intents/{intentId}
POST /api/portfolio-proposals
GET  /api/portfolio-proposals/{proposalId}
GET  /api/portfolio-proposals/{proposalId}/events
GET  /api/evidence/{evidenceId}
```

초기 proposal job 상태:

- `QUEUED`
- `COLLECTING_DATA`
- `ANALYZING_ASSETS`
- `BUILDING_SCENARIOS`
- `GENERATING_EVIDENCE`
- `COMPLETED`
- `FAILED`

## 7. 아키텍처 스타일

프로젝트는 MSA 전환을 고려한 modular monolith에 가까운 Gradle 멀티모듈 구조를 사용한다.

모듈 경계는 순수 레이어 기준이 아니라 비즈니스 기능 기준으로 나눈다. 각 비즈니스 모듈 내부는 lightweight DDD와 hexagonal package structure를 따른다.

상위 모듈:

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

모듈 책임:

- `portfolio-api`: 프론트와 통신하는 BFF/API gateway. controller, request/response DTO, 인증 진입점, 화면용 응답 조립을 소유한다.
- `portfolio-worker`: 비동기 proposal job을 실행하는 executable worker application. `application/port/in` 인터페이스를 통해 비즈니스 모듈 use case를 조율하지만, 비즈니스 규칙은 소유하지 않는다.
- `portfolio-proposal`: portfolio intent, proposal job lifecycle, proposal summary, 최종 proposal assembly state를 소유한다. allocation, scenario, simulation, evidence classification, recommendation은 결정하지 않는다.
- `portfolio-allocation`: 추천 초기 투자 배분, 월 적립 배분, risk profile 해석, 추천 투자 기간, scenario assumption 생성을 소유한다.
- `portfolio-simulation`: capital growth projection과 cash-flow simulation을 소유한다. allocation, contribution, horizon, scenario assumption을 그래프용 금액 경로로 변환한다.
- `portfolio-asset`: asset master data, asset search, symbol, market, asset type, 기본 price snapshot을 소유한다.
- `portfolio-evidence`: claim, evidence item, evidence link, source snapshot, evidence policy를 소유한다.
- `portfolio-recommendation`: 안정성 보강, 수익성 보강, 빈틈 보완, 주의 후보 같은 목적별 suggestion을 소유한다.
- `portfolio-common`: money, percentage, currency code, date range처럼 진짜 범용적인 타입만 포함한다.
- `portfolio-error`: 공통 error code와 error response model을 포함한다.
- `portfolio-infrastructure`: WebClient, Redis, Jackson, observability 설정 같은 공통 기술 설정만 포함한다.

각 비즈니스 모듈은 다음 내부 package structure를 따른다.

```text
adapter/out
application/port/in
application/port/out
application/service
domain
```

MVP는 모든 persistence/external adapter를 `portfolio-infrastructure`에 몰아넣지 않는다. 가능하면 각 비즈니스 모듈이 자기 `adapter/out` 구현을 소유한다. 이 구조는 나중에 MSA로 추출하기 쉽다.

설계는 넓은 "manager" 모듈보다 작은 모듈과 작은 aggregate를 선호한다. 각 모듈은 하나의 비즈니스 질문에 답해야 한다.

- `portfolio-proposal`: 어떤 proposal이 요청되었고, 어떤 상태이며, 어떤 result section들이 속하는가?
- `portfolio-allocation`: 자산, 위험 성향, 근거 기반 가정이 주어졌을 때 어떤 비중, 기간, scenario assumption이 적절한가?
- `portfolio-simulation`: cash flow와 scenario assumption이 주어졌을 때 어떤 금액 경로가 나오는가?
- `portfolio-asset`: 이 자산은 무엇이며 canonical market data는 무엇인가?
- `portfolio-evidence`: 어떤 claim이 만들어졌고, 어떤 evidence가 이를 지지/반박/맥락화하는가?
- `portfolio-recommendation`: 특정 목적을 위해 어떤 추가 자산을 검토해야 하는가?

## 8. Evidence 소유권과 Claim 흐름

`portfolio-evidence`는 `Claim`, `EvidenceItem`, `EvidenceLink`, evidence classification rule을 소유하는 유일한 모듈이다.

다른 비즈니스 모듈은 persisted claim을 직접 생성하면 안 되고, 어떤 statement를 `FACT`, `ESTIMATE`, `INTERPRETATION`으로 분류하면 안 된다. 다른 모듈은 자기 결과의 일부로 local explanation text, rationale anchor, claim draft를 만들 수 있지만, 이 draft는 evidence domain object가 아니다.

Claim 생성 흐름:

1. 비즈니스 모듈이 decision result와 local rationale draft를 만든다.
2. `portfolio-worker`가 해당 draft, source snapshot, target reference를 `portfolio-evidence`에 전달한다.
3. `portfolio-evidence`가 claim을 검증, 분류, 저장한다.
4. `portfolio-evidence`가 claim과 target result section을 연결하는 `EvidenceLink` record를 생성한다.
5. Read API는 proposal section과 evidence link를 조합하지만, business aggregate는 claim collection을 소유하지 않는다.

Target reference는 직접 객체 참조가 아니라 안정적인 identifier를 사용한다.

```text
targetModule: portfolio-allocation
targetType: ProposedAllocation
targetId: allocation_123
targetAnchor: rationale
relationType: SUPPORTS
```

이 구조는 evidence drill-down을 가능하게 하면서도 `portfolio-allocation`, `portfolio-simulation`, `portfolio-recommendation`이 `portfolio-evidence`로부터 독립적으로 유지되게 한다.

## 9. 의존성 규칙

의존성은 단방향이어야 하며, 레이어 건너뛰기는 금지된다.

규칙:

- `domain`은 자기 자신 외부에 의존하지 않는다.
- `application/service`는 `domain`과 port에 의존한다.
- `application/port/in`은 모듈이 외부에 노출하는 use case를 정의한다.
- `application/port/out`은 모듈이 외부에 요구하는 기능을 정의한다.
- `adapter/out`은 `application/port/out`을 구현한다.
- `portfolio-api`는 `application/port/in`을 통해 비즈니스 모듈을 호출할 수 있다.
- `portfolio-worker`는 비동기 proposal job 실행을 위해 `application/port/in`을 통해 비즈니스 모듈을 호출할 수 있다.
- `portfolio-api`는 `adapter/out`을 직접 호출하면 안 된다.
- `portfolio-worker`는 `adapter/out`을 직접 호출하면 안 된다.
- 비즈니스 모듈은 다른 비즈니스 모듈에 직접 의존하면 안 된다.
- `portfolio-common`은 비즈니스 특화 개념을 포함하면 안 된다.
- `portfolio-common`은 다른 project module에 의존하면 안 된다.
- 레이어 건너뛰기 호출은 금지된다.

금지 예시:

- `portfolio-api`가 repository adapter를 직접 호출.
- `portfolio-api`가 domain entity를 직접 변경.
- `portfolio-worker`가 repository adapter를 직접 호출.
- `application/service`가 adapter implementation을 직접 호출.
- `adapter/out`이 `application/service`를 호출.
- `domain`이 port 또는 Spring component를 호출.
- `portfolio-allocation`이 `portfolio-asset`, `portfolio-evidence`, `portfolio-simulation`, `portfolio-recommendation`에 직접 의존.
- `portfolio-proposal`이 `portfolio-allocation`, `portfolio-simulation`, `portfolio-evidence`, `portfolio-recommendation`에 직접 의존.

아키텍처 규칙은 나중에 ArchUnit test로 강제해야 한다.

`portfolio-api`와 `portfolio-worker`는 executable app module이다. 두 모듈은 여러 비즈니스 모듈을 조율할 수 있지만, 자산 비중을 어떻게 정할지, evidence를 어떻게 분류할지, suggestion이 안정성 목적인지 수익성 목적인지 같은 도메인 결정을 포함하면 안 된다. 그런 규칙은 관련 비즈니스 모듈 내부에 있어야 한다.

MVP에서 비즈니스 모듈 간 통신은 네트워크 통신이 아니라 같은 프로세스 안의 `application/port/in` 호출이다. executable app module이 조율한다.

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

worker는 process manager다. 다음 workflow step을 선택하고, 한 use case의 출력을 다른 use case에 전달하고, job을 failed/completed로 표시할 수 있다. 하지만 도메인 결정은 하면 안 된다. 예를 들어 `GenerateAllocationPlanUseCase`를 호출할 수는 있지만, asset weight를 직접 고르면 안 된다.

## 10. Proposal Read Composition

`GetPortfolioProposal`은 단일 aggregate가 아니라 composed read model을 반환한다.

프론트엔드용 전체 응답은 `portfolio-api` application layer의 `ProposalQueryService`가 조립한다. Controller는 proposal detail read에 대해 이 query service만 호출한다.

```text
portfolio-api adapter/in/web controller
  -> portfolio-api application/service/ProposalQueryService
  -> business module application/port/in
```

`ProposalQueryService`는 read-oriented `application/port/in` 호출을 조합한다.

```text
ProposalQueryService
  -> portfolio-proposal: proposal header, status, section ids
  -> portfolio-allocation: allocation plan and scenario assumptions
  -> portfolio-simulation: capital growth projection
  -> portfolio-recommendation: purpose-specific suggestions
  -> portfolio-evidence: evidence summaries for linked target references
```

Composition rule:

- Controller는 proposal detail을 직접 조합하면 안 된다.
- `ProposalQueryService`는 response DTO를 조합할 수 있지만, 비즈니스 결정을 하면 안 된다.
- `portfolio-api`는 repository나 adapter를 직접 호출하면 안 된다.
- 비즈니스 모듈은 자기 read model을 `application/port/in`을 통해 반환한다.
- Evidence는 business aggregate에 내장된 field가 아니라 `EvidenceLink` target reference를 통해 join된다.
- API response는 넓고 화면 중심일 수 있지만, aggregate boundary는 작게 유지한다.

나중에 proposal read path가 비싸지거나 특화되면 `ProposalQueryService` 내부를 CQRS read model 또는 materialized view로 교체한다. Controller contract는 query service를 교체 지점으로 삼기 때문에 안정적으로 유지된다.

## 11. 기술 스택

MVP 추천 스택:

- Java 21.
- Spring Boot WebFlux.
- Spring Data JPA.
- PostgreSQL.
- Flyway.
- 비동기 proposal job을 위한 Redis 또는 DB-backed job queue.
- 외부 데이터와 LLM 호출을 위한 WebClient.
- proposal progress를 위한 SSE endpoint 여지.
- JUnit 5.
- Testcontainers.
- ArchUnit.

WebFlux는 주로 다음에 사용한다.

- 외부 market data, disclosure, LLM API 호출.
- I/O-heavy enrichment 병렬 처리.
- 미래의 SSE progress streaming.

사용자가 DDD와 aggregate modeling을 연습하고 싶어 하므로 MVP에서는 JPA를 우선한다. proposal/evidence/reporting query가 복잡해지면 나중에 read model용 jOOQ를 추가할 수 있다.

## 12. 핵심 Use Case

### SearchAsset

국내주식, 미국주식, ETF, crypto asset을 검색한다.

### CreatePortfolioIntent

사용자의 투자 입력을 생성한다.

- Assets.
- Optional asset thesis.
- Available cash.
- Optional monthly contribution.
- Base currency.
- Risk profile.
- Proposal mode.

### GeneratePortfolioProposal

Intent에 대한 proposal 생성을 시작하고 조율한다.

- Proposal job lifecycle.
- 최종 proposal section assembly.
- Allocation, simulation, evidence, recommendation result에 대한 reference.

### GenerateAllocationPlan

투자 결정의 핵심을 생성한다.

- 추천 초기 투자 배분.
- 추천 월 적립 배분.
- 추천 투자 기간.
- Bear/Base/Bull scenario assumption.
- Allocation rationale draft와 local rationale anchor.

### GenerateCapitalGrowthProjection

그래프에 바로 사용할 수 있는 금액 경로를 생성한다.

- 누적 원금.
- 예상 평가금액 범위.
- 예상 투자수익 범위.
- 추천 투자 기간 동안 6개월 단위 point.

### GeneratePurposeSpecificSuggestions

추가 검토 자산을 생성한다.

- Stability candidate.
- Return enhancement candidate.
- Gap filling candidate.
- Caution candidate.
- Rationale draft와 counter-argument draft.

### CreateEvidenceLinks

Evidence-domain object를 검증하고 저장한다.

- Claim draft를 `FACT`, `ESTIMATE`, `INTERPRETATION`으로 분류.
- Claim과 evidence item 저장.
- `EvidenceLink`를 통해 claim을 proposal result target에 연결.

### GetPortfolioProposal

프론트엔드를 위해 결론 우선 형태로 proposal을 반환한다.

### GetEvidenceDetail

Claim 또는 recommendation의 evidence detail을 반환한다.

## 13. 도메인 모델 개요

### portfolio-asset

- `Asset`
- `AssetPriceSnapshot`

`AssetPriceSnapshot`은 `portfolio-asset`이 소유하는 정규화된 market data다.

사용처:

- 과거 종가.
- 수익률과 변동성 계산.
- 통화 기준 가격 조회.
- 정량 simulation input.

계산되었거나 정규화된 market fact를 저장하며, 전체 source provenance를 저장하지 않는다.

### portfolio-proposal

- `PortfolioIntent`
- `SelectedAsset`
- `ProposalJob`
- `PortfolioProposal`

`SelectedAsset`은 사용자가 선택한 asset reference의 intent 생성 시점 snapshot이다. 전체 `Asset` aggregate를 복사하면 안 된다.

Fields:

- `assetId`
- `symbol`
- `displayName`
- `assetType`
- `market`
- `currency`
- `userThesis`, nullable
- `displayOrder`

Snapshot field는 asset master data가 나중에 바뀌어도 재현성과 UI 표시를 지원하기 위해 존재한다. Canonical asset identity와 asset metadata는 계속 `portfolio-asset`이 소유한다.

`PortfolioProposal`은 모든 result object를 소유하는 큰 aggregate가 아니다. proposal header와 lifecycle aggregate이며, result section을 id로 reference한다. API response는 full proposal document를 조합할 수 있지만, aggregate boundary는 response shape보다 작게 유지한다.

### portfolio-allocation

- `AllocationPlan`
- `ProposedAllocation`
- `ScenarioProjection`

중요한 invariant:

- 초기 투자 비중 합계는 100%여야 한다.
- 월 적립금이 있으면 월 적립 비중 합계도 100%여야 한다.
- 중요한 rationale은 `portfolio-evidence`가 claim을 연결할 수 있도록 local anchor를 노출해야 한다.
- 추천 투자 기간은 양수여야 한다.
- Scenario return range는 min <= max 순서를 지켜야 한다.

### portfolio-simulation

- `CapitalGrowthProjection`
- `CapitalGrowthPoint`

중요한 invariant:

- 원금, 예상 평가금액, 예상 투자수익 범위는 잘못된 음수 원금을 만들면 안 된다.
- Capital growth month는 0부터 시작해 추천 투자 기간까지 진행한다.
- Projection point는 month 기준으로 정렬되어야 한다.
- Simulation module은 expected return, scenario assumption, asset weight를 결정하지 않는다. 입력값으로부터 금액 경로만 계산한다.

### portfolio-evidence

- `Claim`
- `EvidenceItem`
- `EvidenceLink`
- `DataSourceSnapshot`

`DataSourceSnapshot`은 `portfolio-evidence`가 소유하는 source provenance다.

사용처:

- Raw 또는 normalized source payload reference.
- Source URL, publisher, retrieval time, content hash.
- Claim을 뒷받침하는 데 사용된 company IR, filing, macro statistics, ETF document, market data vendor response, LLM research input.
- Proposal 생성 시점에 왜 어떤 claim이 만들어졌는지 재현.

Market data가 claim을 뒷받침할 때, 정규화된 가격은 `AssetPriceSnapshot`에 속하고 vendor response 또는 source metadata는 `DataSourceSnapshot`에 속한다.

Evidence rule:

- `FACT` claim은 최소 하나 이상의 source가 필요하다.
- `ESTIMATE` claim은 방법과 한계가 필요하다.
- `INTERPRETATION` claim은 supporting evidence와 명시된 limitation 또는 counterpoint가 필요하다.
- Evidence link는 stable target reference를 통해 다른 모듈의 result를 target한다. 다른 비즈니스 모듈은 `ClaimId`를 aggregate invariant의 일부로 저장하지 않는다.

### portfolio-recommendation

- `PortfolioSuggestion`

Suggestion type:

- `STABILITY`
- `RETURN_ENHANCEMENT`
- `GAP_FILLING`
- `CAUTION`

각 suggestion은 local rationale과 counter-argument anchor를 포함해야 한다. 그래야 `portfolio-recommendation`이 evidence-domain object에 의존하지 않으면서도 `portfolio-evidence`가 claim을 붙일 수 있다.

## 14. 저장해야 할 데이터

MVP table 또는 persisted aggregate:

- 인증이 포함된다면 Users.
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

Evidence와 source snapshot data는 신뢰성과 재현성을 위해 필요하다. 제품은 proposal이 생성된 시점의 데이터로 왜 그런 제안이 만들어졌는지 설명할 수 있어야 한다.

## 15. Proposal 생성 파이프라인

초기 proposal generation pipeline:

1. Intent를 검증한다.
2. Asset metadata를 resolve한다.
3. Market data와 source data를 수집한다.
4. Asset-level insight와 evidence-backed assumption을 생성한다.
5. Scenario assumption을 생성한다.
6. Allocation과 monthly contribution split을 제안한다.
7. Investment horizon을 제안한다.
8. Capital growth를 simulation한다.
9. Exposure map을 만든다.
10. Purpose-specific suggestion을 생성한다.
11. Rationale draft를 `portfolio-evidence`를 통해 claim과 evidence link로 변환한다.
12. Proposal을 저장하고 job을 completed로 표시한다.

Pipeline은 `portfolio-worker`가 조율하지만, 각 결정은 해당 비즈니스 capability를 소유한 모듈에 속한다.

- Asset identity와 metadata decision은 `portfolio-asset`에 속한다.
- Normalized market price snapshot은 `portfolio-asset`에 속한다.
- Source provenance snapshot은 `portfolio-evidence`에 속한다.
- Evidence classification과 evidence link는 `portfolio-evidence`에 속한다.
- Weight, horizon, scenario assumption decision은 `portfolio-allocation`에 속한다.
- Money-path calculation은 `portfolio-simulation`에 속한다.
- Purpose-specific suggestion classification은 `portfolio-recommendation`에 속한다.
- Proposal lifecycle과 final section reference는 `portfolio-proposal`에 속한다.

3단계는 책임에 따라 데이터를 저장한다.

- 정량 계산에 쓰이는 price/time-series data는 `AssetPriceSnapshot`으로 저장한다.
- Claim을 뒷받침하는 source payload 또는 source metadata는 `DataSourceSnapshot`으로 저장한다.
- 같은 외부 market data response도 둘 다 만들 수 있다. 계산용 정규화 가격과 evidence traceability용 source snapshot을 각각 저장한다.

첫 구현은 최종 API와 domain shape를 유지하면서 deterministic mock 또는 fixture-based proposal generation을 사용할 수 있다.

## 16. MVP 범위 밖

- 증권사 연결.
- 자동 매매.
- 실시간 시장 체결.
- Push/email alert.
- 세금 최적화.
- 완전한 장기 자산형성 모드.
- 사용자가 비중을 조정하는 slider simulation UI.
- 완전한 외부 데이터 ingestion 자동화.

API와 domain model은 나중에 이런 기능을 추가할 수 있는 여지를 남겨야 한다.

## 17. 첫 구현 단위

첫 구현은 AI 없는 backend MVP를 만든다.

- Gradle multi-module skeleton.
- Architecture dependency rule.
- Core domain model.
- Intent creation API.
- Proposal job creation API.
- Mock proposal generation을 위한 worker execution path.
- Mock proposal generator.
- Proposal result API.
- Evidence detail API.
- Capital growth projection calculation.
- Domain invariant와 API flow test.

이 단위는 실제 external data나 LLM research를 붙이기 전에 architecture와 product shape를 검증한다.
