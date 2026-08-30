# Web-First Architecture

## 목표

앱스토어 배포 전에 브라우저에서 최신 금융 뉴스 브리핑을 확인할 수 있는 첫 제품을 공개한다.
React 웹은 새로운 백엔드가 아니라 Spring REST API의 첫 사용자 클라이언트다. 이후 Swift 앱도
같은 API 계약을 재사용한다.

## 저장소와 책임

```text
market_brief       Python: RSS 수집, FinBERT 추론, 브리핑 생성
market_brief_api   Spring: REST, Validation, 트랜잭션, PostgreSQL
market_brief_web   React: 조회 화면, 로딩·오류 상태, 반응형 UI
```

```text
RSS/Atom
   |
   v
Python scheduled job
   | POST JSON (server credential)
   v
Spring Boot API <---- GET JSON ---- React + Vite
   |
   v
PostgreSQL
```

Python과 React는 서로 통신하지 않는다. 두 프로그램 모두 Spring의 JSON 계약만 알고,
PostgreSQL 스키마와 JPA 엔티티는 알지 못한다.

## 첫 웹 범위

첫 공개 버전은 읽기 전용으로 제한한다.

1. `GET /api/briefings/latest`로 최신 감성 브리핑 표시
2. `GET /api/articles/latest?limit=20`으로 최신 기사 표시
3. 필요할 때 `GET /api/articles/{articleId}/analyses`로 기사 분석 상세 표시
4. 수집·분석 시각, 출처, 원문 링크, 긍정·중립·부정 확률 표시
5. 데이터 없음, API 오류, 로딩 상태를 명확히 표시

FinBERT 결과는 금융 문장의 정서 분류로 표현하고 매수·매도 신호나 가격 예측으로 표시하지
않는다. 로그인, 관심 종목, LLM 요약, 관리자 화면은 첫 공개 범위에 포함하지 않는다.

## React 프로젝트 기준

별도 `market_brief_web` 저장소를 다음 기준으로 생성한다.

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

프런트엔드 저장소에는 데이터베이스 비밀번호, Python 쓰기 자격 증명, 서버 비밀키를 넣지
않는다. `VITE_` 환경변수는 브라우저 번들에서 확인할 수 있으므로 공개 가능한 값만 사용한다.

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
4. `market_brief_web` 생성 및 최신 브리핑·기사 화면 구현
5. Spring과 PostgreSQL 배포 후 실제 HTTPS API 검증
6. React 정적 빌드 배포 및 운영 API 주소 연결
7. Python 작업 배포·예약 실행 후 새 데이터가 웹에 나타나는지 확인

첫 공개 완료 기준은 사용자가 URL을 열어 최신 브리핑과 기사 목록을 볼 수 있고, 브라우저에
서버 비밀값이 노출되지 않으며, 예약된 Python 실행 결과가 PostgreSQL을 거쳐 화면에 갱신되는
것이다.
