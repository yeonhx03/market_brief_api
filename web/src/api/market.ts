import type {
  MarketClock,
  MarketHeatmapSector,
  MarketIndexSummary,
  OptionSnapshot,
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
        { symbol: 'SPX', name: 'S&P 500', value: 6481.32, changePercent: 0.42 },
        { symbol: 'IXIC', name: 'NASDAQ', value: 21544.27, changePercent: 0.68 },
        { symbol: 'DJI', name: 'DOW', value: 45544.88, changePercent: -0.21 },
        { symbol: 'KOSPI', name: 'KOSPI', value: 3186.01, changePercent: -0.32 },
        { symbol: 'SOXX', name: 'SOXX', value: 298.41, changePercent: 1.17 },
    ]
}


export async function getMarketHeatmap(): Promise<MarketHeatmapSector[]> {
  return [
    {
      sectorId: 'technology',
      sectorName: '기술',
      stocks: [
        { symbol: 'NVDA', companyName: 'NVIDIA', marketCapWeight: 6.8, changePercent: 2.41 },
        { symbol: 'AAPL', companyName: 'Apple', marketCapWeight: 5.9, changePercent: -0.84 },
        { symbol: 'MSFT', companyName: 'Microsoft', marketCapWeight: 5.4, changePercent: 0.37 },
      ],
    },
    {
      sectorId: 'communication',
      sectorName: '커뮤니케이션',
      stocks: [
        { symbol: 'GOOGL', companyName: 'Alphabet', marketCapWeight: 4.1, changePercent: -0.26 },
        { symbol: 'META', companyName: 'Meta', marketCapWeight: 2.8, changePercent: 1.03 },
      ],
    },
    {
      sectorId: 'consumer',
      sectorName: '소비재',
      stocks: [
        { symbol: 'AMZN', companyName: 'Amazon', marketCapWeight: 3.2, changePercent: -1.12 },
        { symbol: 'TSLA', companyName: 'Tesla', marketCapWeight: 1.8, changePercent: 0.08 },
      ],
    },
  ]
}


export async function getOptionSnapshots(): Promise<OptionSnapshot[]> {
  return [
    {
      symbol: 'SOXX',
      iv30Day: 0.428,
      putCallRatio: 0.91,
      expectedLow: 277.34,
      expectedHigh: 319.48,
      dataAsOf: '2026-08-28T20:00:00Z',
      delayMinutes: 1440,
    },
    {
      symbol: 'QQQ',
      iv30Day: 0.237,
      putCallRatio: 0.78,
      expectedLow: 557.12,
      expectedHigh: 604.86,
      dataAsOf: '2026-08-28T20:00:00Z',
      delayMinutes: 1440,
    },
  ]
}