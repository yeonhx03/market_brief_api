# market_brief_api

`market_brief`가 생성한 기사, FinBERT 분석 결과, 브리핑을 저장하고 React 웹과 향후
Swift 클라이언트에 제공하는 Spring Boot REST API입니다.

## 책임

- REST 요청 검증
- 기사와 분석 결과의 중복 방지
- PostgreSQL 영구 저장
- React 웹과 macOS/iOS 클라이언트용 조회 API 제공

RSS 수집, FinBERT 추론, 브리핑 생성은 Python `market_brief`가 담당합니다.

## 기술 스택

- Java 21
- Spring Boot 4.1.1
- Gradle Wrapper
- Spring Data JPA
- PostgreSQL
- Flyway

## API

```text
POST /api/articles
GET  /api/articles/latest?limit=10
POST /api/articles/{articleId}/analyses
GET  /api/articles/{articleId}/analyses
POST /api/briefings
GET  /api/briefings/latest
```

Python은 쓰기 API를 사용해 수집·분석·생성 결과를 저장하고, React 웹은 우선 조회 API만
사용합니다. 브라우저에 PostgreSQL 연결 정보나 Python 작업용 인증 정보를 전달하지 않습니다.

## 공개 API 경계

- `GET /api/**`는 첫 웹 공개 범위에서 인증 없이 조회할 수 있습니다.
- `WRITE_API_KEY`가 설정되면 `POST /api/**`는 `X-Market-Brief-Key` 헤더가 필요합니다.
- 로컬 기본 프로필에서는 키가 비어 있어 기존 개발 흐름을 유지합니다.
- `prod` 프로필에서는 `WRITE_API_KEY`와 `WEB_ORIGINS`가 반드시 필요합니다.
- 상태 확인은 `GET /actuator/health`를 사용하며 상세 내부 정보는 공개하지 않습니다.

운영 환경변수 예시:

```text
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://database-host:5432/market_brief
DB_USERNAME=market_brief
DB_PASSWORD=replace-with-secret
WRITE_API_KEY=replace-with-long-random-secret
WEB_ORIGINS=https://brief.example.com
```

`WEB_ORIGINS`는 쉼표로 여러 React 배포 주소를 지정할 수 있습니다. CORS는 브라우저 접근
범위만 제한하며 Python 쓰기 인증을 대신하지 않습니다.

## 웹 우선 방향

첫 사용자 화면은 별도 `market_brief_web` 저장소에서 React, TypeScript, Vite로 구현합니다.
웹은 Spring API를 HTTPS로 호출하며 Python이나 PostgreSQL에 직접 접근하지 않습니다.

상세 구조, 보안 경계, 환경변수, 구현·배포 순서는
[`docs/web-first-architecture.md`](docs/web-first-architecture.md)에 기록합니다.

실제 PostgreSQL 종단간 검증 결과와 발견된 RSS 중복 결함은
[`docs/e2e-verification.md`](docs/e2e-verification.md)에 기록합니다.

## 테스트

```bash
./gradlew test
```
