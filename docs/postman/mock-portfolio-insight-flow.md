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

## 2. Create Proposal (S&P500 ETF + Bitcoin)

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

Expected: `status` is `COMPLETED`, `signals[0].type` is `FALSE_DIVERSIFICATION`.

---

## 3. Get Proposal Detail

```http
GET /api/portfolio-proposals/{proposalId}
```

Use the `proposalId` from the create response.

---

## 4. Get Evidence Detail

The `FALSE_DIVERSIFICATION` action includes an `evidenceIds` field.
Copy one ID and call:

```http
GET /api/evidence/{evidenceId}
```

Returns the claim, basis, limitation, review trigger, and source snapshots.

---

## 5. Create Proposal (Samsung + SOXL — overlapping semiconductor)

```json
{
  "intentId": "00000000-0000-0000-0000-000000000999",
  "assetIds": [
    "00000000-0000-0000-0000-000000000101",
    "00000000-0000-0000-0000-000000000106"
  ]
}
```

Expected: `signals` includes `OVERLAPPING_EXPOSURE` and `TACTICAL_PRODUCT_MISUSE`.

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
