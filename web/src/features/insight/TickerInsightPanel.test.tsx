import { render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getTickerInsight } from '../../api/insight'
import { TickerInsightPanel } from './TickerInsightPanel'

vi.mock('../../api/insight', () => ({
  getTickerInsight: vi.fn(),
}))

const mockedGetTickerInsight = vi.mocked(getTickerInsight)

describe('TickerInsightPanel', () => {
  beforeEach(() => {
    mockedGetTickerInsight.mockReset()
  })

  it('displays related news metadata and FinBERT sentiment', async () => {
    mockedGetTickerInsight.mockResolvedValue({
      ticker: 'AAPL',
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
          title: 'AAPL 실적 전망',
          source: 'Reuters',
          publishedAt: '2026-08-31T13:10:00Z',
          url: 'https://example.com/news/1',
        },
      ],
    })

    render(<TickerInsightPanel ticker="AAPL" />)

    expect(screen.getByText('뉴스 조회 중...')).toBeInTheDocument()
    expect(await screen.findByRole('link', { name: 'AAPL 실적 전망' }))
      .toHaveAttribute('href', 'https://example.com/news/1')
    const metadata = screen.getByText((_, element) =>
      element?.classList.contains('insight-meta') ?? false,
    )
    expect(metadata).toHaveTextContent('기사 2건 · 출처 2곳')
    expect(metadata).toHaveTextContent('기준 시각:')
    expect(screen.getByText('62%')).toBeInTheDocument()
    expect(screen.getByText('28%')).toBeInTheDocument()
    expect(screen.getByText('10%')).toBeInTheDocument()
    expect(
      screen.getByText('문장 감성이며 매매 신호가 아닙니다.'),
    ).toBeInTheDocument()
  })

  it('displays an alert when loading ticker insight fails', async () => {
    mockedGetTickerInsight.mockRejectedValue(new Error('network error'))

    render(<TickerInsightPanel ticker="AAPL" />)

    expect(await screen.findByRole('alert')).toHaveTextContent(
      '관련 뉴스와 감성을 불러오지 못했습니다.',
    )
  })
})
