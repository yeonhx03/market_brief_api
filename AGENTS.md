# Market Brief API Agent Instructions

These instructions apply to the entire repository.

## Repository Layout

- This is a single Git repository for the Spring API and React web client.
- Spring source stays at the repository root.
- React + TypeScript + Vite source belongs only in `web/` inside this repository.
- Do not create, restore, or recommend a separate `market_brief_web` repository.
- Spring and React keep independent build commands and deployment targets even though they share one
  Git repository.

## Project Role

- Spring Boot owns the REST API, validation, transactions, and PostgreSQL.
- Python `market_brief` owns RSS collection, FinBERT inference, OpenAI calls, and briefing generation.
- Python communicates with this application through HTTP/JSON.
- A React web will be the first public client; Swift clients will follow using the same API over HTTPS.
- React communicates only with Spring. It never connects directly to Python, PostgreSQL, market-data
  providers, or OpenAI.

## Boundaries

- Do not implement RSS collection or FinBERT inference in Spring.
- Do not turn text sentiment into BUY/SELL signals or stock-price predictions.
- Do not let Python connect directly to PostgreSQL.
- Do not expose provider, OpenAI, database, or Python write credentials to React.
- OpenAI briefing generation starts only after the user selects a ticker and the cached briefing is
  missing or stale. Typing and autocomplete must not call OpenAI.
- Do not add watchlists, brokerage APIs, Kafka, or Kubernetes during the first web release.

## React Collaboration

- The user writes production React code while learning.
- Before each implementation unit, show the code in chat and explain its syntax and project role in
  Korean within at most two screenfuls.
- Use one meaningful implementation unit at a time; do not split work into trivial single-line steps.
- After the user writes a unit, review the actual files and clearly identify functional or syntax errors.
- Do not edit user-owned React implementation files unless the user explicitly asks for the edit.
- The agent writes and runs frontend tests, then explains what behavior those tests protect.
- Do not block progress over formatting-only differences.

## Development

- Java: 21
- Spring Boot: 4.1.1
- Build and dependency manager: Gradle Wrapper
- Test command: `./gradlew test`
- Prefer small, focused changes.
- Review user-written code before taking over implementation.
- Preserve unrelated user changes.

## Delivery Status And Order

Completed backend foundation:

1. Article, ArticleAnalysis, and Briefing REST contracts and tests.
2. Spring Data JPA, PostgreSQL, Flyway, and exact-URL duplicate handling.
3. Python HTTP adapters and live PostgreSQL end-to-end verification.
4. Production health, CORS, and protected Python write APIs.
5. React dashboard and the market-status, search, quote, index, heatmap, and ticker-insight REST
   integrations.
6. Explicit Python `collect --ticker` article linking and isolated PostgreSQL V6 end-to-end
   verification.

Next order:

1. Verify ticker-linked analysis with actual FinBERT in a supported runtime.
2. Define and connect the missing options and briefing-generation-status contracts.
3. Add backend-managed market-data WebSocket streaming after REST snapshot verification.
4. Deploy Spring, PostgreSQL, React, and the scheduled/on-demand Python worker.
