# Web-First Architecture

## 목표

앱스토어 배포 전에 브라우저에서 최신 금융 뉴스 브리핑을 확인할 수 있는 첫 제품을 공개한다.
React 웹은 새로운 백엔드가 아니라 Spring REST API의 첫 사용자 클라이언트다. 이후 Swift 앱도
같은 API 계약을 재사용한다.

## 저장소와 책임

이 프로젝트는 Spring과 React를 하나의 Git 저장소에서 관리한다. React를 위한 별도
`market_brief_web` 저장소는 만들지 않는다. `web/`은 독립된 npm 프로젝트이지만 저장소를
나눈다는 의미가 아니며, Spring과 빌드·배포 대상만 분리한다.

```text
market_brief       Python: RSS 수집, FinBERT 추론, 브리핑 생성
market_brief_api
  ├─ Spring: REST, Validation, 트랜잭션, PostgreSQL
  └─ web/: React 조회 화면, 로딩·오류 상태, 반응형 UI
```

```text
RSS/Atom
   |
   v
Python scheduled job
   | POST JSON (server credential)
   v
Spring Boot API <---- HTTPS/JSON ---- React + Vite
   |
   v
PostgreSQL
```

Python과 React는 서로 통신하지 않는다. 두 프로그램 모두 Spring의 JSON 계약만 알고,
PostgreSQL 스키마와 JPA 엔티티는 알지 못한다.

## 현재 결정 상태

확정된 내용:

- React + TypeScript + Vite를 사용한다.
- 별도 저장소를 만들지 않고 `market_brief_api/web`에 둔다.
- 이후 대화나 작업에서도 `market_brief_web` 저장소를 새로 만들거나 복원하지 않는다.
- npm 빌드와 Gradle 빌드, 웹 배포와 Spring 배포는 독립적으로 유지한다.
- 첫 화면의 언어는 한국어로 한다.
- 브라우저는 Spring API만 사용하고 Python, PostgreSQL, 외부 데이터 공급자, OpenAI에
  직접 접근하지 않는다.
- 외부 데이터 공급자와 OpenAI의 비밀키는 서버에서만 관리한다.

화면 구성과 시각 방향은 아래 사양으로 확정했다. FinBERT 결과는 금융 문장 감성으로
명시하고 매수·매도 신호나 가격 예측으로 표시하지 않는다.

## 확정 화면 사양

### 헤더

- 왼쪽에 `market_brief`를 표시한다.
- 오른쪽에 서울 시각과 뉴욕 시각을 함께 표시한다.
- 각 시각 옆에 `정규장`, `프리마켓`, `애프터마켓`, `장 마감`과 같은 시장 상태를
  한국어로 표시한다.
- 서머타임, 주말, 현지 휴장일이 있으므로 React에서 영업일을 단순 계산하지 않는다.
  Spring이 시장 상태 API와 시장 캘린더를 이용해 상태를 제공하고 React는 표시만 한다.
- 헤더와 본문 사이에는 구분선을 두지 않는다.

### 본문

데스크톱 본문은 세로선으로 나눈 A:B = 4:1 구조를 사용한다. 좁은 화면에서는 오른쪽
B 영역을 A 아래로 이동하고, 데스크톱의 B 영역은 독립적으로 스크롤할 수 있게 한다.

왼쪽 A 영역:

- S&P 500, NASDAQ, DOW, KOSPI, SOXX 등 주요 지수의 현재 값과 등락률
- 업종별·종목별 등락을 사각형 크기와 색으로 표현하는 Finviz 형태의 히트맵
- SOXX, QQQ 같은 ETF 중심의 옵션 현황
- 옵션 표의 기본 항목은 `30일 IV`, `풋/콜`, `±1σ 예상 범위`이며 데이터 기준 시각과
  지연 여부를 함께 표시

오른쪽 B 영역:

- 티커 또는 종목명으로 검색하는 검색창과 자동완성 목록
- 선택 전에도 높이를 유지하는 종목 요약 빈 영역
- 선택한 종목의 티커, 종목명, 현재가, 등락률과 데이터가 확보되면 거래량
- 선택 종목에 연결된 뉴스 요약과 출처 수·기준 시각
- FinBERT의 긍정·중립·부정 확률과 `문장 감성이며 매매 신호가 아닙니다` 안내
- OpenAI가 생성한 한국어 브리핑과 근거 기사 표시

시각 스타일은 짙은 배경, 얇은 경계선, 압축된 금융 터미널형 레이아웃을 사용한다.
현재 시안과 같이 상승은 녹색, 하락은 적색, 선택·강조 상태는 황색으로 표현한다.

## 검색과 브리핑 생성 흐름

OpenAI는 검색어를 입력하거나 자동완성 목록을 표시할 때 호출하지 않는다. 사용자가 검색
결과에서 종목을 최종 선택한 뒤, 저장된 최신 브리핑이 없거나 만료된 경우에만 서버 측
생성 흐름에서 호출한다.

```text
1. React -> Spring: 티커/종목명 검색
2. 사용자: 검색 결과에서 종목 선택
3. React -> Spring: 시세, 옵션, 저장된 최신 브리핑을 병렬 조회
4. 최신 브리핑이 있으면 즉시 표시
5. 브리핑이 없거나 만료됐으면 Spring을 통해 생성 요청
6. Python: 관련 기사 조회 -> 필요 시 FinBERT -> OpenAI 한국어 브리핑 생성
7. Python -> Spring: 결과 저장
8. Spring -> React: 완료 상태와 저장된 결과 전달
```

생성 중에는 오른쪽 영역에 `브리핑 생성 중` 상태를 표시한다. 브라우저가 OpenAI를 직접
호출하지 않으며, 공개 생성 요청에는 캐시, 지원 종목 제한, 호출 빈도 제한을 적용한다.
초기에는 OpenAI Responses API의 구조화된 JSON 응답을 직접 사용한다. 여러 도구의 분기,
재시도와 장기 상태 관리가 실제로 필요해질 때 LangChain 또는 LangGraph를 검토한다.

## 시장 데이터와 실시간 전송

- 종목 자동완성, 미국 주식 시세와 시장 상태의 첫 후보는 Finnhub다.
- 무료 Finnhub Quote 응답에는 거래량이 없으므로 거래량 공급자를 확정하기 전에는 해당
  항목을 숨기거나 `데이터 없음`으로 표시한다.
- 전체 히트맵에는 구성 종목, 업종, 시가총액과 시세가 필요하다. 무료 API만으로 안정적인
  전체 구성이 어려우므로 초기에는 관리되는 정적 구성 종목 목록과 캐시된 시세를 사용하고,
  공개 범위가 커질 때 데이터 라이선스를 다시 검토한다.
- 옵션 개발 데이터는 MarketData.app Free를 첫 후보로 사용한다. 전체 옵션 체인, IV와
  Greeks를 받을 수 있지만 24시간 지연 데이터이므로 화면에 지연 상태를 명시한다.
- KOSPI와 해외 지수 실시간 데이터는 무료 Finnhub 범위로 확정하지 않고 별도 공급자를
  검토한다.

초기 구현은 REST 스냅샷으로 화면과 데이터 계약을 먼저 검증한다. 이후 Spring이 외부 시세
WebSocket 연결과 API 키를 관리하고, 필요한 가격 변경을 브라우저 WebSocket으로 전달한다.
브라우저는 외부 공급자와 직접 WebSocket을 연결하지 않는다.

## React 프로젝트 기준

`market_brief_api/web` 디렉터리를 다음 기준으로 구성한다. 같은 저장소를 쓰더라도
Gradle과 npm 빌드는 서로 독립적으로 유지한다.

```text
React + TypeScript + Vite
src/
  api/          Spring HTTP 호출과 응답 타입
  components/   재사용 가능한 표시 컴포넌트
  features/     briefing, articles 기능 단위
  pages/        페이지 조합
  styles/       전역 토큰과 반응형 스타일
```

API 주소는 빌드 환경변수로만 주입한다.

```text
VITE_API_BASE_URL=http://localhost:8080
```

`web/` 프로젝트에는 데이터베이스 비밀번호, Python 쓰기 자격 증명, 서버 비밀키를 넣지
않는다. `VITE_` 환경변수는 브라우저 번들에서 확인할 수 있으므로 공개 가능한 값만 사용한다.

## React 학습과 구현 방식

- 실제 React 구현 코드는 사용자가 직접 작성한다.
- 각 작업 전에 작성할 코드와 핵심 문법, 전체 프로젝트에서의 역할을 한국어로 설명한다.
- 한 작업 단위의 코드와 설명은 최대 두 화면 분량으로 제한한다.
- 너무 작은 문법 조각이 아니라 실행하거나 눈으로 확인할 수 있는 의미 있는 기능 단위로
  진행한다.
- 사용자가 작성한 뒤 실제 파일을 읽어 기능·문법 오류를 리뷰하며 포맷 차이만으로 진행을
  막지 않는다.
- 프런트엔드 테스트 코드는 에이전트가 작성하고 실행하며, 테스트가 보호하는 동작을 사용자에게
  설명한다.
- 사용자가 명시적으로 수정을 요청하지 않는 한 에이전트가 React 구현 파일을 대신 작성하지
  않는다.

## React와 REST 연동 진행 상황

2026-08-31 현재 다음 범위를 완료했다.

- `web/`에 React + TypeScript + Vite 프로젝트 생성
- Vitest, React Testing Library와 jsdom 기반 테스트 환경 구성
- 타입이 있는 임시 API 함수와 서울·뉴욕 시장 시각 헤더 구현
- 데스크톱 A:B = 4:1 및 좁은 화면 세로 배치 대시보드 골격 구현
- S&P 500, NASDAQ, DOW, KOSPI, SOXX 주요 지수 값·등락률 카드 구현
- 고정 시장 구성 종목을 업종별로 묶고 시가총액 비중과 등락 강도를 표현하는 히트맵 구현
- SOXX, QQQ의 30일 IV, 풋/콜, 예상 범위, 기준 시각과 지연 여부를 표시하는 옵션 표 구현
- 티커·종목명 자동완성 검색, 입력 지연과 선택 상태 구현
- 선택 티커의 현재가, 등락률, 거래량과 기준 시각을 표시하는 시세 요약 구현
- 선택 티커의 관련 기사, 출처 수, 기준 시각과 FinBERT 긍정·중립·부정 확률 구현
- 브리핑 캐시 없음, 생성 중, 완료, 실패 상태와 근거 기사 표시 구현
- 시장 시각 로딩·오류, 대시보드 랜드마크, 지수 값 포맷과 상승·하락 표시 테스트
- 히트맵 업종 계층·비중·색상과 옵션 값·지연·기준 시각 테스트
- 티커 검색·선택·오류와 시세 로딩·가격 포맷·거래량 없음·오류 테스트
- 관련 뉴스·FinBERT 표시와 브리핑 상태·생성 요청 조건 테스트

목 화면을 완료한 뒤 다음 작은 종단간 단위의 Spring 계약과 React 실제 호출을 연결했다.

- `GET /api/tickers/search?query=`: Finnhub 기반 티커·종목명 검색
- `GET /api/market/status`: Twelve Data 한국 시장 상태와 Finnhub 미국 시장 상태
- `GET /api/tickers/{ticker}/quote`: Finnhub 기반 선택 티커 시세·회사명
- `GET /api/market/indexes`: FMP의 S&P 500·NASDAQ·DOW와 Twelve Data의 SOXX
- `GET /api/market/heatmap`: 관리되는 미국 종목 구성과 Finnhub 시세·회사 프로필
- `POST /api/articles/{articleId}/tickers`: Python용 보호된 기사-티커 연결
- `GET /api/tickers/{ticker}/insight?limit=`: 연결 기사와 각 기사의 최신 FinBERT 분석 집계

이 API들의 공급자 오류는 일관된 `503` 응답으로 변환하고, 용도별 Caffeine 캐시를 적용했다.
React의 시장 상태, 검색, 시세, 미국 지수·SOXX, 히트맵, 관련 뉴스·감성 API 함수는 실제
Spring `fetch`로 교체했으며 fetch 계약 테스트를 유지한다.

KOSPI는 금융위원회 데이터 연동을 보류했으므로 React 제품 더미를 유지한다. 옵션은 무료
공급자 범위와 가입 조건이 현재 요구에 맞지 않아 SOXX·QQQ 제품 더미를 유지한다. 테스트
fixture와 `vi.mock()`은 제품 더미가 아니므로 계속 사용한다.

관련 뉴스·FinBERT의 Spring·React 계약과 Python의 명시적 티커 연결까지 완료했다. Python은
HTTP 수집 모드에서 `collect --ticker AAPL --api-url ...`처럼 사용자가 지정한 티커를 신규
기사와 일반 중복 `409`로 확인한 기존 기사에 연결한다. 이 값은 수집 단위의 명시적
메타데이터이며 기사 내용에서 종목 관련성을 자동 추론한 결과가 아니다.

2026-08-31 통제 피드 검증에서는 격리 PostgreSQL에 기사 2건과 AAPL 연결 2건을 저장했고,
동일 피드 재수집 뒤에도 행 수와 연결이 유지됐다. 고정 확률 테스트 분석 3건으로 연결 기사마다
최신 `text_sentiment`만 선택하는 insight 집계와 분석이 없을 때의
`404 TICKER_INSIGHT_NOT_FOUND`도 확인했다. 현재 macOS에는 PyTorch와 Transformers가 없으므로
실제 FinBERT 추론은 별도의 지원 런타임에서 검증해야 한다.

## 공개 경계와 보안

- 초기 조회 `GET` API는 공개할 수 있다.
- Python이 호출하는 `POST` API는 배포 전에 서버 간 자격 증명으로 보호한다.
- 운영 CORS는 실제 React 배포 주소와 로컬 개발 주소만 허용한다.
- CORS는 브라우저 접근 제어이며 인증을 대신하지 않는다.
- Spring과 React의 외부 통신은 HTTPS만 사용한다.
- PostgreSQL은 인터넷에 직접 공개하지 않고 Spring만 접속한다.
- 운영 비밀값은 배포 환경변수 또는 호스팅 서비스의 secret 기능으로 관리한다.

## 배포 형태

```text
Static web host
  - React production build

Application host
  - Spring Boot always-on process

Managed PostgreSQL
  - Flyway migration on Spring startup

Scheduled worker
  - Python collect -> analyze -> briefing -> exit
```

호스팅 업체는 구현과 실통신 검증 후 비용, Java/Python 실행 지원, 예약 작업, PostgreSQL 제공
여부를 비교해 선택한다. 설계는 특정 업체에 종속시키지 않는다.

## 구현과 공개 순서

1. Python 브리핑 JSON을 `POST /api/briefings`에 저장하는 HTTP 어댑터 완성 (완료)
2. 로컬 PostgreSQL에서 RSS -> Spring -> 분석 -> 브리핑 전체 흐름 검증 (완료)
3. Spring 운영 프로필, 상태 확인, 제한된 CORS, 쓰기 API 인증과 Python 키 전달
   실통신 검증 (완료)
4. 한국어 대시보드 화면 내용·우선순위·상태·시각 방향 확정 (완료)
5. `market_brief_api/web` React 프로젝트와 확정된 화면을 타입이 있는 임시 데이터로 구현 (완료)
6. 종목 검색·시장 상태·시세·주요 지수·히트맵 REST 계약과 React 연결 (완료)
7. 기사-티커 연결과 관련 뉴스·FinBERT 조회 계약 및 React 연결 (완료)
8. Python 수집 HTTP 흐름의 명시적 티커 연결과 격리 PostgreSQL 검증 (완료)
9. FinBERT 지원 런타임에서 티커 연결 기사의 실제 분석 적재 검증
10. 옵션 공급자 확정 후 옵션 REST 연결
11. 브리핑 조회·생성 상태 계약과 제한된 polling 연결
12. REST 스냅샷 검증 후 외부 시세 WebSocket 연결
13. Spring, PostgreSQL, React와 Python 작업 배포 및 HTTPS 종단간 검증

첫 공개 완료 기준은 사용자가 URL을 열어 시장 현황과 선택한 종목의 최신 브리핑을 볼 수
있고, 브라우저에 서버 비밀값이 노출되지 않으며, 예약 또는 종목 선택으로 생성된 Python
결과가 PostgreSQL을 거쳐 화면에 갱신되는 것이다.
