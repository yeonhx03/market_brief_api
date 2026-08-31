import { render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getTickerQuote } from '../../api/market'
import { TickerSummary } from './TickerSummary'

vi.mock('../../api/market', () => ({
  getTickerQuote: vi.fn(),
}))

const mockedGetTickerQuote = vi.mocked(getTickerQuote)
const appleSelection = {
  ticker: 'AAPL',
  companyName: 'Apple',
  exchange: 'NASDAQ',
}

describe('TickerSummary', () => {
  beforeEach(() => {
    mockedGetTickerQuote.mockReset()
  })

  it('loads and displays the selected ticker quote', async () => {
    mockedGetTickerQuote.mockResolvedValue({
      ticker: 'AAPL',
      companyName: 'Apple',
      currentPrice: 231.42,
      changePercent: 0.95,
      volume: 48_120_300,
      currency: 'USD',
      dataAsOf: '2026-08-31T14:30:00Z',
    })

    render(<TickerSummary selection={appleSelection} />)

    expect(screen.getByText('시세 조회 중...')).toBeInTheDocument()
    expect(await screen.findByRole('heading', { name: 'AAPL' })).toBeInTheDocument()
    expect(mockedGetTickerQuote).toHaveBeenCalledWith('AAPL')
    expect(screen.getByText(/231\.42/)).toBeInTheDocument()
    expect(screen.getByText('+0.95%')).toHaveClass('positive')
    expect(screen.getByText('48,120,300')).toBeInTheDocument()
    expect(screen.getByText(/기준 시각:/)).toBeInTheDocument()
  })

  it('displays data unavailable when volume is missing', async () => {
    mockedGetTickerQuote.mockResolvedValue({
      ticker: 'MSFT',
      companyName: 'Microsoft',
      currentPrice: 512.64,
      changePercent: -0.37,
      volume: null,
      currency: 'USD',
      dataAsOf: '2026-08-31T14:30:00Z',
    })

    render(
      <TickerSummary
        selection={{
          ticker: 'MSFT',
          companyName: 'Microsoft',
          exchange: 'NASDAQ',
        }}
      />,
    )

    expect(await screen.findByText('데이터 없음')).toBeInTheDocument()
    expect(screen.getByText('-0.37%')).toHaveClass('negative')
  })

  it('displays an alert when loading the quote fails', async () => {
    mockedGetTickerQuote.mockRejectedValue(new Error('network error'))

    render(<TickerSummary selection={appleSelection} />)

    expect(await screen.findByRole('alert')).toHaveTextContent(
      '종목 시세를 불러오지 못했습니다.',
    )
  })
})
