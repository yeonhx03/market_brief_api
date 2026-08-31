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
