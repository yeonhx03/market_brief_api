import type {MarketClock, MarketIndexSummary} from '../types/market'


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