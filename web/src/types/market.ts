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

