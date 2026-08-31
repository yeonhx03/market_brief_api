import type { TickerBriefingState } from '../types/briefing'

export async function getTickerBriefing(
  ticker: string,
): Promise<TickerBriefingState> {
  const normalizedTicker = ticker.toUpperCase()

  if (normalizedTicker === 'AAPL') {
    return {
      status: 'ready',
      ticker: 'AAPL',
      briefingId: 1,
      generatedAt: '2026-08-31T14:35:00Z',
      briefingText:
        '최근 기사에서는 실적 기대와 공급망 위험이 함께 관찰됩니다.',
      evidenceArticles: [
        {
          articleId: 1,
          title: 'AAPL 실적 전망에 시장 관심 집중',
          source: 'Reuters',
          url: 'https://example.com/news/1',
        },
      ],
    }
  }

  if (normalizedTicker === 'MSFT') {
    return {
      status: 'generating',
      ticker: 'MSFT',
      requestedAt: '2026-08-31T14:34:00Z',
    }
  }

  if (normalizedTicker === 'NVDA') {
    return {
      status: 'failed',
      ticker: 'NVDA',
      message: '브리핑 생성에 실패했습니다.',
    }
  }

  return {
    status: 'missing',
    ticker: normalizedTicker,
    reason: 'not_found',
  }
}

export async function requestTickerBriefing(
  ticker: string,
): Promise<TickerBriefingState> {
  return {
    status: 'generating',
    ticker: ticker.toUpperCase(),
    requestedAt: new Date().toISOString(),
  }
}