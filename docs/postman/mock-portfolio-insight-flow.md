# Mock Portfolio Insight Flow

Run the API server:

```bash
./gradlew :portfolio-api:bootRun --args='--server.port=18080'
```

Base URL: `http://localhost:18080`

---

## 1. Search Assets

```http
GET /api/assets/search?query=bitcoin
```

Returns assets matching the query by symbol, display name, or alias.
Stable asset IDs allow repeatable Postman examples.

---

## 2. Create Intent (S&P500 ETF + Bitcoin)

```http
POST /api/portfolio-intents
Content-Type: application/json
```

```json
{
  "availableCash": 10000000,
  "monthlyContribution": 1000000,
  "riskProfile": "GROWTH",
  "assets": [
    {"assetId": "00000000-0000-0000-0000-000000000104", "thesis": "US core"},
    {"assetId": "00000000-0000-0000-0000-000000000107", "thesis": "Crypto upside"}
  ]
}
```

Save the returned `intentId`.

---

## 3. Create Proposal

```http
POST /api/portfolio-proposals
Content-Type: application/json
```

```json
{
  "intentId": "{intentId}"
}
```

Expected: `status` is `COMPLETED`, `signals[0].type` is `FALSE_DIVERSIFICATION`.

---

## 4. Get Proposal Detail

```http
GET /api/portfolio-proposals/{proposalId}
```

Use the `proposalId` from the create response. The detail response includes composed allocations and capital growth points.

---

## 5. Get Evidence Detail

The `FALSE_DIVERSIFICATION` action includes an `evidenceIds` field.
Copy one ID and call:

```http
GET /api/evidence/{evidenceId}
```

Returns the claim, basis, limitation, review trigger, and source snapshots.

---

## 6. Create Intent And Proposal (Samsung + SOXL — overlapping semiconductor)

```json
{
  "availableCash": 10000000,
  "monthlyContribution": 1000000,
  "riskProfile": "GROWTH",
  "assets": [
    {"assetId": "00000000-0000-0000-0000-000000000101", "thesis": "Korea semiconductor"},
    {"assetId": "00000000-0000-0000-0000-000000000106", "thesis": "Tactical semiconductor"}
  ]
}
```

Use the returned `intentId` to create a proposal. Expected: `signals` includes `OVERLAPPING_EXPOSURE` and `TACTICAL_PRODUCT_MISUSE`.

---

## Stable Asset IDs

| Asset                  | ID                                       |
|------------------------|------------------------------------------|
| Samsung Electronics    | 00000000-0000-0000-0000-000000000101     |
| SK Hynix               | 00000000-0000-0000-0000-000000000102     |
| Coca-Cola              | 00000000-0000-0000-0000-000000000103     |
| TIGER US S&P500 ETF    | 00000000-0000-0000-0000-000000000104     |
| Invesco QQQ ETF        | 00000000-0000-0000-0000-000000000105     |
| SOXL (3x Semiconductor)| 00000000-0000-0000-0000-000000000106     |
| Bitcoin (KRW-BTC)      | 00000000-0000-0000-0000-000000000107     |
| Ethereum (KRW-ETH)     | 00000000-0000-0000-0000-000000000108     |
