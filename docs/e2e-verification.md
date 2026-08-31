# PostgreSQL End-to-End Verification

## 2026-08-30 Local Verification

Environment:

- Intel Mac, Java 21
- Spring Boot 4.1.1 on `127.0.0.1:18080`
- Homebrew PostgreSQL 17.11 on an isolated temporary cluster and port `55432`
- database `market_brief_e2e`
- Python `market_brief` HTTP adapters

The temporary PostgreSQL server and data directory were removed after verification. PostgreSQL 17
remains installed but was not registered as an automatically started Homebrew service.

## Passed

- Spring connected to a real PostgreSQL 17 database.
- Flyway validated and applied migrations V1 through V4.
- Hibernate `ddl-auto=validate` accepted the migrated schema.
- A live BBC Business RSS run passed through Python -> Spring -> PostgreSQL.
- `HttpArticleAnalysisRepository` persisted an analysis and returned the same existing analysis ID
  for a repeated request with the same article and analyzer identity.
- `HttpBriefingRepository` persisted a three-item sentiment briefing.
- `GET /api/briefings/latest` returned the stored briefing, including one analyzed and two explicit
  missing-analysis items.
- The API response and PostgreSQL briefing payload agreed.

Observed persisted results before cleanup:

```text
Flyway migrations: 4, latest version: 4
Articles: 70
Article analyses: 2
Briefings: 1
Latest briefing: 3 articles, 1 analyzed, 2 missing analyses
```

The analysis persistence path used constructed probabilities to exercise the real Python HTTP,
Spring validation, JPA, and PostgreSQL path. FinBERT inference itself was not run on this Mac.

## Initial Failure: Live RSS Duplicate Handling

The same live BBC feed was collected twice:

```text
First run:  Saved 35 new articles.
Second run: Saved 35 new articles.  (expected 0)
Database:   70 rows, 33 distinct URLs
```

All live RSS requests had null `sourceArticleId`, `canonicalUrl`, and `contentHash`. The current
Spring duplicate checks and unique indexes use only those fields, so they do not protect the
required `url` fallback. The feed also contained repeated URLs inside a single batch, which is why
35 first-run items represented only 33 distinct URLs.

URL canonicalization can follow as a separate refinement, but the immediate behavior had to prevent
an identical submitted URL from being stored twice.

## URL Duplicate Fix And Reverification

The Spring article service now checks the required `url` after the existing source article ID,
canonical URL, and content hash checks. Flyway V5 adds a unique index on `articles.url` as a race
condition backstop.

Verification covered both layers:

- service persistence test returns `DuplicateArticleException` with the existing article ID
- direct repository writes with the same URL are rejected by the database index
- all 36 Spring tests pass
- PostgreSQL 17 applied Flyway V1 through V5
- the same live BBC feed was collected twice through the Python HTTP adapter

Actual repeated-feed result:

```text
First run:  Saved 33 new articles.
Second run: Saved 0 new articles.
Database:   33 rows, 33 distinct URLs
```

The first run stored 33 rather than the feed's 35 entries because two repeated URLs inside the same
batch were also rejected. Exact submitted URL duplicate handling is therefore verified end to end.

## Production Authentication Reverification

The complete integration was run again with the Spring `prod` profile, `WRITE_API_KEY`, restricted
`WEB_ORIGINS`, a fresh PostgreSQL 17 database, and the Python adapters reading the same key from
their environment.

Verified results:

- `GET /actuator/health` remained public and returned `UP`.
- `POST /api/articles` without `X-Market-Brief-Key` returned `401 UNAUTHORIZED`.
- Python sent the key only on writes and collected 33 live BBC Business articles through Spring.
- a constructed analysis exercised the authenticated analysis JSON and persistence path.
- Python generated and persisted a three-item sentiment briefing through the authenticated API.
- PostgreSQL contained 33 articles, one analysis, and one briefing.
- Flyway successfully applied V1 through V5 in the same production-profile run.

The constructed analysis used fixed probabilities (`positive=0.2`, `neutral=0.7`, `negative=0.1`)
only to verify authentication, HTTP mapping, validation, JPA, and PostgreSQL. It was not represented
as FinBERT inference; actual FinBERT CPU inference remains verified in the Windows environment.

The Spring process, isolated PostgreSQL server, database, and temporary data directory were stopped
and removed after verification. The Homebrew PostgreSQL service was not enabled.

## 2026-08-31 Article-Ticker-Insight Verification

Environment:

- Spring Boot on `127.0.0.1:18081`
- Homebrew PostgreSQL 17.11 on an isolated temporary cluster and port `55433`
- a controlled two-item RSS fixture served only on `127.0.0.1:18765`
- Python `market_brief` branch `ticker-http-integration` at `c7c1292`
- Spring and React branch `codex/web-api-integration` at `029bb11`

The fixture articles were explicitly linked with `--ticker AAPL`. This ticker was user-supplied
collection metadata, not an automatic relevance classification. The fixture was not represented as
real AAPL news.

The first Python collection called `POST /api/articles` and then
`POST /api/articles/{articleId}/tickers` for each item. PostgreSQL contained:

```text
Flyway migrations: 6, latest version: 6
Articles: 2
Article tickers: 2
Article analyses before the isolated insight fixture: 0
```

Collecting the same feed again reported `Saved 0 new articles.` Spring returned the existing article
ID for each ordinary duplicate, Python repeated the ticker association, and the composite primary key
on `article_tickers (article_id, ticker)` kept the operation idempotent. The final article and ticker
counts remained 2 and 2.

### Isolated Insight Contract

Before any analysis rows existed, `GET /api/tickers/AAPL/insight?limit=10` returned
`404 TICKER_INSIGHT_NOT_FOUND` even though two articles were linked to AAPL.

Three fixed-probability `text_sentiment` rows were then written through
`POST /api/articles/{articleId}/analyses`:

- article 1: an older `0.8 / 0.1 / 0.1` row
- article 1: a newer `0.2 / 0.7 / 0.1` row
- article 2: a `0.4 / 0.3 / 0.3` row

The test analyzer name was `e2e-fixed-fixture`. These constructed probabilities exercised HTTP,
validation, JPA, PostgreSQL, the ticker insight query, and the React response contract. They were not
FinBERT inference results.

The AAPL insight returned two articles and averaged only the latest `text_sentiment` row for each
article:

```text
Positive: 0.30
Neutral:  0.50
Negative: 0.20
Data as of: 2026-08-31T08:00:00Z
```

`limit=1` returned only the most recently published linked article. A ticker without insight data
returned `404 TICKER_INSIGHT_NOT_FOUND`. The React API and component tests verified the same response
shape, percentage display, related-news metadata, and the notice that sentence sentiment is not a
trading signal.

### Remaining FinBERT And Concurrent-Conflict Limits

PyTorch and Transformers were not installed on this macOS environment, and no model dependency was
added. Actual FinBERT inference therefore remains a separate supported-runtime verification step.

Ordinary duplicates return `existingArticleId`, but a database unique constraint violation caused by
a true concurrent insert is currently mapped to `409 ARTICLE_DUPLICATE` with a null ID. A stable ID
would require separating the failed insert transaction from a new duplicate-resolution read transaction
and verifying the result with concurrent requests against PostgreSQL. No unverified lookup or fallback
was added during this run.

The complete Spring test suite, all 35 React tests, the React production build, 31 ticker-related Python
tests, and Ruff passed. The Spring process, controlled feed server, isolated PostgreSQL server, database,
and temporary directory were removed after verification.
