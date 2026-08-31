# # 시장 브리핑 웹 Market Brief
<img width="1112" height="720" alt="market_brief_image" src="https://github.com/user-attachments/assets/a466e27f-a36b-4a51-8e33-7b51b12729be" />
<img width="800" height="900" alt="finbert" src="https://github.com/user-attachments/assets/c43dc437-2788-4ad8-a18b-00fc0b670676" />

## 프로젝트 소개

- Market Brief는 시장 현황과 종목별 뉴스 브리핑을 확인하는 웹입니다.
- 서울과 뉴욕 시장 시각, 주요 지수, 시장 히트맵과 옵션 현황을 조회할 수 있습니다.
- 티커 또는 종목명을 검색하면 시세, 관련 뉴스, FinBERT 문장 감성과 한국어 브리핑을 확인할 수 있습니다.
- React는 Spring API를 호출하고, Python은 RSS 수집과 FinBERT 분석 및 브리핑 생성을 담당합니다.

## 개발 환경

- Front End: React 19, TypeScript, Vite 8, CSS
- Back End: Java 21, Spring Boot 4.1.1, Spring Data JPA
- Database: PostgreSQL, Flyway
- Data/Analysis: Python, RSS, FinBERT

## 페이지별 기능

### [시장 현황]

- 서울과 뉴욕 시장 시각과 개장 상태를 표시합니다.
- 주요 지수와 업종별 시장 히트맵을 확인할 수 있습니다.
- 옵션 거래량과 Put/Call 비율을 표시합니다.
- 현재 KOSPI 지수 API 연동 불가로 해결 중에 있습니다.

### [종목 검색]

- 티커 또는 종목명으로 종목을 검색할 수 있습니다.
- 검색 결과에서 종목을 선택하면 현재 시세를 표시합니다.

### [관련 뉴스 및 FinBERT]

- 선택한 종목에 연결된 관련 뉴스를 표시합니다.
- 기사별 최신 FinBERT 문장 감성을 긍정, 중립, 부정 확률로 표시합니다.

### [한국어 브리핑]

- 선택한 종목의 한국어 브리핑과 근거 기사를 표시합니다.
- 브리핑 생성 상태를 확인할 수 있습니다.
