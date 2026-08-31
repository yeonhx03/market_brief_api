import type {
  MarketClock,
  MarketHeatmapSector,
  MarketIndexSummary,
  OptionSnapshot,
  TickerQuote,
  TickerSearchResult,
} from '../types/market'

export async function getMarketClocks(): Promise<MarketClock[]>
{
    return [
        {
            city: '서울',
            timeZone: 'Asia/Seoul',
            displayTime: '08:30',
            status: '장 시작 전'
        },
        {
            city: '뉴욕',
            timeZone: 'America/New_York',
            displayTime: '19:30',
            status: '장 마감'
        },
    ]
}

export async function getMarketIndexes(): Promise<MarketIndexSummary[]> {
    return [
        { ticker: 'SPX', name: 'S&P 500', value: 6481.32, changePercent: 0.42 },
        { ticker: 'IXIC', name: 'NASDAQ', value: 21544.27, changePercent: 0.68 },
        { ticker: 'DJI', name: 'DOW', value: 45544.88, changePercent: -0.21 },
        { ticker: 'KOSPI', name: 'KOSPI', value: 3186.01, changePercent: -0.32 },
        { ticker: 'SOXX', name: 'SOXX', value: 298.41, changePercent: 1.17 },
    ]
}

// 임시 히트맵
export async function getMarketHeatmap(): Promise<MarketHeatmapSector[]> {
  return [
    {
      sectorId: 'technology',
      sectorName: '기술',
      stocks: [
        { ticker: 'NVDA', companyName: 'NVIDIA', marketCapWeight: 6.8, changePercent: 2.41 },
        { ticker: 'AAPL', companyName: 'Apple', marketCapWeight: 5.9, changePercent: -0.84 },
        { ticker: 'MSFT', companyName: 'Microsoft', marketCapWeight: 5.4, changePercent: 0.37 },
      ],
    },
    {
      sectorId: 'communication',
      sectorName: '커뮤니케이션',
      stocks: [
        { ticker: 'GOOGL', companyName: 'Alphabet', marketCapWeight: 4.1, changePercent: -0.26 },
        { ticker: 'META', companyName: 'Meta', marketCapWeight: 2.8, changePercent: 1.03 },
      ],
    },
    {
      sectorId: 'consumer',
      sectorName: '소비재',
      stocks: [
        { ticker: 'AMZN', companyName: 'Amazon', marketCapWeight: 3.2, changePercent: -1.12 },
        { ticker: 'TSLA', companyName: 'Tesla', marketCapWeight: 1.8, changePercent: 0.08 },
      ],
    },
  ]
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


//임시 검색 함수
export async function searchTickers(
  query: string,
): Promise<TickerSearchResult[]> {
  const tickers: TickerSearchResult[] = [
    { ticker: 'AAPL', companyName: 'Apple', exchange: 'NASDAQ' },
    { ticker: 'AMZN', companyName: 'Amazon', exchange: 'NASDAQ' },
    { ticker: 'MSFT', companyName: 'Microsoft', exchange: 'NASDAQ' },
    { ticker: 'NVDA', companyName: 'NVIDIA', exchange: 'NASDAQ' },
  ]

  const normalizedQuery = query.trim().toLowerCase()

  if (!normalizedQuery) return []

  return tickers.filter(
    (item) =>
      item.ticker.toLowerCase().includes(normalizedQuery) ||
      item.companyName.toLowerCase().includes(normalizedQuery),
  )
}


//임시 시세 함수
export async function getTickerQuote(ticker: string): Promise<TickerQuote> {
  const quotes: Record<string, TickerQuote> = {
    AAPL: {
      ticker: 'AAPL',
      companyName: 'Apple',
      currentPrice: 231.42,
      changePercent: 0.95,
      volume: 48_120_300,
      currency: 'USD',
      dataAsOf: '2026-08-31T14:30:00Z',
    },
    AMZN: {
      ticker: 'AMZN',
      companyName: 'Amazon',
      currentPrice: 229.18,
      changePercent: -1.12,
      volume: 31_420_100,
      currency: 'USD',
      dataAsOf: '2026-08-31T14:30:00Z',
    },
    MSFT: {
      ticker: 'MSFT',
      companyName: 'Microsoft',
      currentPrice: 512.64,
      changePercent: 0.37,
      volume: null,
      currency: 'USD',
      dataAsOf: '2026-08-31T14:30:00Z',
    },
    NVDA: {
      ticker: 'NVDA',
      companyName: 'NVIDIA',
      currentPrice: 184.76,
      changePercent: 2.41,
      volume: 172_340_000,
      currency: 'USD',
      dataAsOf: '2026-08-31T14:30:00Z',
    },
  }

  const quote = quotes[ticker.toUpperCase()]

  if (!quote) {
    throw new Error('지원하지 않는 티커입니다.')
  }

  return quote
}