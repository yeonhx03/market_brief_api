import type { TickerInsight } from '../types/insight'

export async function getTickerInsight(
  ticker: string,
): Promise<TickerInsight> {
  const normalizedTicker = ticker.toUpperCase()

  return {
    ticker: normalizedTicker,
    articleCount: 2,
    sourceCount: 2,
    dataAsOf: '2026-08-31T14:30:00Z',
    sentiment: {
      positive: 0.62,
      neutral: 0.28,
      negative: 0.1,
    },
    articles: [
      {
        articleId: 1,
        title: `${normalizedTicker} 실적 전망에 시장 관심 집중`,
        source: 'Reuters',
        publishedAt: '2026-08-31T13:10:00Z',
        url: 'https://example.com/news/1',
      },
      {
        articleId: 2,
        title: `${normalizedTicker} 관련 산업 수요 동향`,
        source: 'BBC Business',
        publishedAt: '2026-08-31T12:20:00Z',
        url: 'https://example.com/news/2',
      },
    ],
  }
}