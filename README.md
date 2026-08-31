# market_brief_api

`market_brief`가 생성한 기사, FinBERT 분석 결과, 브리핑을 저장하고 React 웹과 향후
Swift 클라이언트에 제공하는 Spring Boot REST API입니다. 한국어 React 웹은 같은
저장소의 `web/`에서 개발합니다.

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

## 현재 API

```text
POST /api/articles
GET  /api/articles/latest?limit=10
POST /api/articles/{articleId}/tickers
POST /api/articles/{articleId}/analyses
GET  /api/articles/{articleId}/analyses
POST /api/briefings
GET  /api/briefings/latest
GET  /api/tickers/search?query=AAPL
GET  /api/tickers/{ticker}/quote
GET  /api/tickers/{ticker}/insight?limit=10
GET  /api/market/status
GET  /api/market/indexes
GET  /api/market/heatmap
```

Python은 보호된 쓰기 API로 수집·분석·생성 결과를 저장합니다. React는 Spring API만
사용하며, 브라우저에 PostgreSQL 연결 정보, 외부 데이터·OpenAI 키 또는 Python 작업용
인증 정보를 전달하지 않습니다.

종목 검색, 시장 상태, 시세, 주요 지수, 히트맵과 관련 뉴스·FinBERT 조회 계약은 구현했습니다.
검색·시세·히트맵은 Finnhub, 한국·미국 시장 상태는 각각 Twelve Data와 Finnhub, 미국 대표
지수는 FMP, SOXX는 Twelve Data를 Spring이 호출합니다. 공급자 호출 결과는 용도별로 짧게
캐시하며 React에는 공급자 키를 노출하지 않습니다.

옵션과 브리핑 생성 상태 API는 아직 구현하지 않았습니다. 사용자가 종목을 선택했을 때 필요한
브리핑이 없거나 오래된 경우에만 별도의 브라우저용 Spring 엔드포인트로 생성을 요청할
예정입니다. 이 엔드포인트는 Python 쓰기 API와 인증 경계를 공유하지 않고 캐시와 호출 빈도
제한을 적용합니다.

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

첫 사용자 화면은 확정된 한국어 대시보드 시안을 기준으로 이 저장소의 `web/`에 React,
TypeScript, Vite로 구현할 예정입니다. 같은 Git 저장소를 사용하더라도 웹은 Spring과
독립적으로 빌드·배포합니다.

현재 `web/`에는 React + TypeScript + Vite 프로젝트와 Vitest 기반 테스트 환경을 구성했습니다.
서울·뉴욕 시장 시각, 티커 자동완성, 선택 티커 시세, 미국 대표 지수와 SOXX, 계층형 히트맵,
관련 뉴스·FinBERT 화면은 Spring REST 호출로 교체했습니다. KOSPI와 옵션은 적합한 데이터
공급자를 확정할 때까지 기존 제품 더미 데이터를 유지합니다. 브리핑 화면도 조회·생성 상태
API를 구현하기 전까지 기존 상태 데이터를 유지합니다.

관련 뉴스·FinBERT 조회는 기사-티커 연결 테이블과 티커별 최신 분석 집계까지 구현했습니다.
Python `market_brief`의 HTTP 수집은 `collect --ticker AAPL --api-url ...`처럼 사용자가 지정한
티커를 신규 기사와 기존 중복 기사에 연결합니다. `--ticker`는 수집 단위의 명시적 메타데이터일
뿐 기사 내용의 자동 관련성 판정이 아닙니다.

2026-08-31에는 통제된 로컬 RSS 2건을 격리 PostgreSQL 17 데이터베이스에 수집해 기사 2행과
`article_tickers` 2행을 확인했습니다. 같은 피드를 다시 수집하면 일반 중복 `409`의
`existingArticleId`로 연결을 복구해 새 기사를 만들지 않고 기존 티커 연결을 유지했습니다.
고정 확률 테스트 분석으로 insight 계약도 검증했지만, 현재 macOS에는 PyTorch와 Transformers가
없으므로 이 결과를 실제 FinBERT 추론으로 표현하지 않습니다. 상세 결과는
[`docs/e2e-verification.md`](docs/e2e-verification.md)에 기록합니다.

`market_brief_web`이라는 별도 저장소는 만들지 않습니다. Spring과 React는 하나의 Git
저장소와 작업 이력을 사용하고, 빌드 결과와 배포 대상만 분리합니다.

대시보드는 왼쪽 시장 현황 영역과 오른쪽 종목 검색·브리핑 영역을 4:1로 배치합니다.
종목 검색 중에는 OpenAI를 호출하지 않고, 사용자가 검색 결과를 선택한 뒤 최신 브리핑
캐시가 없을 때만 Python의 생성 흐름을 시작합니다. 외부 시장 데이터와 OpenAI 비밀키는
브라우저에 전달하지 않습니다.

React 구현 코드는 학습을 위해 사용자가 직접 작성합니다. 작업은 한 번에 하나의 의미 있는
단위로 진행하며, 구현 전 코드와 문법·역할 설명을 최대 두 화면 분량으로 제공합니다. 작성된
코드는 실제 파일을 기준으로 리뷰하고 프런트엔드 테스트 작성과 실행은 에이전트가 담당합니다.

상세 구조, 보안 경계, 환경변수, 구현·배포 순서는
[`docs/web-first-architecture.md`](docs/web-first-architecture.md)에 기록합니다.

실제 PostgreSQL 종단간 검증 결과와 발견된 RSS 중복 결함은
[`docs/e2e-verification.md`](docs/e2e-verification.md)에 기록합니다.

## 테스트

```bash
./gradlew test
```
