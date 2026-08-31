export type MarketStatus = | '장 시작 전' | '정규장' | '프리마켓' | '애프터마켓' | '장 마감'

// 장 시각
export type MarketClock = {
    city: string
    timeZone: string
    displayTime: string
    status: MarketStatus
}

// 상단 시장 지수
export type MarketIndexSummary = {
    ticker: string
    name: string
    value: number
    changePercent: number
}

// 히트맵 내부 개별 주식
export type MarketHeatmapStock = {
  ticker: string
  companyName: string
  marketCapWeight: number
  changePercent: number
}

//히트맵 섹터
export type MarketHeatmapSector = {
  sectorId: string
  sectorName: string
  stocks: MarketHeatmapStock[]
}

// 옵션 화면
export type OptionSnapshot = {
    ticker: string
    iv30Day: number
    putCallRatio: number
    expectedLow: number
    expectedHigh: number
    dataAsOf: string
    delayMinutes: number
}

// 종목 ticker 검색
export type TickerSearchResult = {
  ticker: string
  companyName: string
  exchange: string
}

// 검색된 종목 정보
export type TickerQuote = {
  ticker: string
  companyName: string
  currentPrice: number
  changePercent: number
  volume: number | null
  currency: string
  dataAsOf: string
}