export type MarketStatus = | '장 시작 전' | '정규장' | '프리마켓' | '애프터마켓' | '장 마감'

export type MarketClock = {
    city: string
    timeZone: string
    displayTime: string
    status: MarketStatus
}

export type MarketIndexSummary = {
    symbol: string
    name: string
    value: number
    changePercent: number
}

export type MarketHeatmapStock = {
  symbol: string
  companyName: string
  marketCapWeight: number
  changePercent: number
}

export type MarketHeatmapSector = {
  sectorId: string
  sectorName: string
  stocks: MarketHeatmapStock[]
}

export type OptionSnapshot = {
    symbol: string
    iv30Day: number
    putCallRatio: number
    expectedLow: number
    expectedHigh: number
    dataAsOf: string
    delayMinutes: number
}