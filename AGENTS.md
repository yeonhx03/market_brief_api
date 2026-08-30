# Market Brief API Agent Instructions

These instructions apply to the entire repository.

## Project Role

- Spring Boot owns the REST API, validation, transactions, and PostgreSQL.
- Python `market_brief` owns RSS collection, FinBERT inference, and briefing generation.
- Python communicates with this application through HTTP/JSON.
- Swift clients will read data through this API over HTTPS.

## Boundaries

- Do not implement RSS collection or FinBERT inference in Spring.
- Do not turn text sentiment into BUY/SELL signals or stock-price predictions.
- Do not let Python connect directly to PostgreSQL.
- Do not add watchlists, LLM summaries, brokerage APIs, Kafka, or Kubernetes during the initial API phase.

## Development

- Java: 21
- Spring Boot: 4.1.1
- Build and dependency manager: Gradle Wrapper
- Test command: `./gradlew test`
- Prefer small, focused changes.
- Review user-written code before taking over implementation.
- Preserve unrelated user changes.

## Initial Order

1. Define and test the Article REST contract.
2. Add Spring Data JPA, PostgreSQL, and Flyway.
3. Implement article duplicate handling.
4. Add ArticleAnalysis APIs.
5. Add Briefing persistence and latest retrieval.
6. Connect the Python HTTP repository adapters.