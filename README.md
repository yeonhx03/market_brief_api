# market_brief_api

`market_brief`가 생성한 기사, FinBERT 분석 결과, 브리핑을 저장하고 제공하는 Spring Boot REST API입니다.

## 책임

- REST 요청 검증
- 기사와 분석 결과의 중복 방지
- PostgreSQL 영구 저장
- macOS/iOS 클라이언트용 조회 API 제공

RSS 수집, FinBERT 추론, 브리핑 생성은 Python `market_brief`가 담당합니다.

## 기술 스택

- Java 21
- Spring Boot 4.1.1
- Gradle Wrapper

## 테스트

```bash
./gradlew test