import type {
  MarketClock,
  MarketHeatmapSector,
  MarketIndexSummary,
  OptionSnapshot,
  TickerQuote,
  TickerSearchResult,
} from '../types/market'


const API_BASE_URL = (
  import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
).replace(/\/$/, '')


export async function getMarketClocks(): Promise<MarketClock[]> {
  const response = await fetch(`${API_BASE_URL}/api/market/status`)

  if (!response.ok) {
    throw new Error(`Market status request failed: ${response.status}`)
  }

  return (await response.json()) as MarketClock[]
}

// 시장 지수
export async function getMarketIndexes(): Promise<MarketIndexSummary[]> {
  const response = await fetch(`${API_BASE_URL}/api/market/indexes`)

  if (!response.ok) {
    throw new Error(`Market indexes request failed: ${response.status}`)
  }

  const indexes = (await response.json()) as MarketIndexSummary[]

  return [
    ...indexes.slice(0, 3),
    {
      ticker: 'KOSPI',
      name: 'KOSPI',
      value: 3186.01,
      changePercent: -0.32,
    },
    ...indexes.slice(3),
  ]
}

export async function getMarketHeatmap(): Promise<MarketHeatmapSector[]> {
  const response = await fetch(`${API_BASE_URL}/api/market/heatmap`)

  if (!response.ok) {
    throw new Error(`Market heatmap request failed: ${response.status}`)
  }

  return (await response.json()) as MarketHeatmapSector[]
}


export async function getOptionSnapshots(): Promise<OptionSnapshot[]> {
  return [
    {
      ticker: 'SOXX',
      iv30Day: 0.428,
      putCallRatio: 0.91,
      expectedLow: 277.34,
      expectedHigh: 319.48,
      dataAsOf: '2026-08-28T20:00:00Z',
      delayMinutes: 1440,
    },
    {
      ticker: 'QQQ',
      iv30Day: 0.237,
      putCallRatio: 0.78,
      expectedLow: 557.12,
      expectedHigh: 604.86,
      dataAsOf: '2026-08-28T20:00:00Z',
      delayMinutes: 1440,
    },
  ]
}


export async function searchTickers(
  query: string,
): Promise<TickerSearchResult[]> {
  const normalizedQuery = query.trim()

  if (!normalizedQuery) return []

  const params = new URLSearchParams({
    query: normalizedQuery,
  })

  const response = await fetch(
    `${API_BASE_URL}/api/tickers/search?${params.toString()}`,
  )

  if (!response.ok) {
    throw new Error(`Ticker search failed: ${response.status}`)
  }

  return (await response.json()) as TickerSearchResult[]
}


// 시세 조회
export async function getTickerQuote(ticker: string): Promise<TickerQuote> {
  const normalizedTicker = ticker.trim().toUpperCase()

  if (!normalizedTicker) {
    throw new Error('Ticker is required')
  }

  const response = await fetch(
    `${API_BASE_URL}/api/tickers/${encodeURIComponent(normalizedTicker)}/quote`,
  )

  if (!response.ok) {
    throw new Error(`Ticker quote request failed: ${response.status}`)
  }

  return (await response.json()) as TickerQuote
}
