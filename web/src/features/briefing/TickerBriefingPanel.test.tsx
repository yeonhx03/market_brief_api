import { render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  getTickerBriefing,
  requestTickerBriefing,
} from '../../api/briefing'
import { TickerBriefingPanel } from './TickerBriefingPanel'

vi.mock('../../api/briefing', () => ({
  getTickerBriefing: vi.fn(),
  requestTickerBriefing: vi.fn(),
}))

const mockedGetTickerBriefing = vi.mocked(getTickerBriefing)
const mockedRequestTickerBriefing = vi.mocked(requestTickerBriefing)

describe('TickerBriefingPanel', () => {
  beforeEach(() => {
    mockedGetTickerBriefing.mockReset()
    mockedRequestTickerBriefing.mockReset()
  })

  it('displays a ready briefing and its evidence without requesting generation', async () => {
    mockedGetTickerBriefing.mockResolvedValue({
      status: 'ready',
      ticker: 'AAPL',
      briefingId: 1,
      generatedAt: '2026-08-31T14:35:00Z',
      briefingText: '실적 기대와 공급망 위험이 함께 관찰됩니다.',
      evidenceArticles: [
        {
          articleId: 1,
          title: 'AAPL 실적 전망',
          source: 'Reuters',
          url: 'https://example.com/news/1',
        },
      ],
    })

    render(<TickerBriefingPanel ticker="AAPL" />)

    expect(screen.getByText('브리핑 확인 중...')).toBeInTheDocument()
    expect(await screen.findByText('실적 기대와 공급망 위험이 함께 관찰됩니다.'))
      .toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'AAPL 실적 전망' }))
      .toHaveAttribute('href', 'https://example.com/news/1')
    expect(screen.getByText(/생성 시각:/)).toBeInTheDocument()
    expect(mockedRequestTickerBriefing).not.toHaveBeenCalled()
  })

  it('displays the generating state without making another request', async () => {
    mockedGetTickerBriefing.mockResolvedValue({
      status: 'generating',
      ticker: 'MSFT',
      requestedAt: '2026-08-31T14:34:00Z',
    })

    render(<TickerBriefingPanel ticker="MSFT" />)

    expect(await screen.findByText('브리핑 생성 중...')).toBeInTheDocument()
    expect(mockedRequestTickerBriefing).not.toHaveBeenCalled()
  })

  it('requests generation only when the cached briefing is missing', async () => {
    mockedGetTickerBriefing.mockResolvedValue({
      status: 'missing',
      ticker: 'AMZN',
      reason: 'not_found',
    })
    mockedRequestTickerBriefing.mockResolvedValue({
      status: 'generating',
      ticker: 'AMZN',
      requestedAt: '2026-08-31T14:34:00Z',
    })

    render(<TickerBriefingPanel ticker="AMZN" />)

    expect(await screen.findByText('브리핑 생성 중...')).toBeInTheDocument()
    expect(mockedRequestTickerBriefing).toHaveBeenCalledOnce()
    expect(mockedRequestTickerBriefing).toHaveBeenCalledWith('AMZN')
  })

  it('displays a generation failure returned by the server', async () => {
    mockedGetTickerBriefing.mockResolvedValue({
      status: 'failed',
      ticker: 'NVDA',
      message: '브리핑 생성에 실패했습니다.',
    })

    render(<TickerBriefingPanel ticker="NVDA" />)

    expect(await screen.findByRole('alert')).toHaveTextContent(
      '브리핑 생성에 실패했습니다.',
    )
    expect(mockedRequestTickerBriefing).not.toHaveBeenCalled()
  })

  it('displays an alert when loading briefing state fails', async () => {
    mockedGetTickerBriefing.mockRejectedValue(new Error('network error'))

    render(<TickerBriefingPanel ticker="AAPL" />)

    expect(await screen.findByRole('alert')).toHaveTextContent(
      '브리핑 상태를 불러오지 못했습니다.',
    )
  })
})
