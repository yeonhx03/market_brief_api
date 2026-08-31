import { render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getMarketIndexes } from '../../api/market'
import { MarketIndexes } from './MarketIndexes'

vi.mock('../../api/market', () => ({
  getMarketIndexes: vi.fn(),
}))

const mockedGetMarketIndexes = vi.mocked(getMarketIndexes)

describe('MarketIndexes', () => {
  beforeEach(() => {
    mockedGetMarketIndexes.mockReset()
  })

  it('displays formatted index values and change directions', async () => {
    mockedGetMarketIndexes.mockResolvedValue([
      {
        ticker: 'SPX',
        name: 'S&P 500',
        value: 6481.32,
        changePercent: 0.42,
      },
      {
        ticker: 'DJI',
        name: 'DOW',
        value: 45544.88,
        changePercent: -0.21,
      },
    ])

    render(<MarketIndexes />)

    expect(await screen.findByText('6,481.32')).toBeInTheDocument()
    expect(screen.getByText('+0.42%')).toHaveClass('positive')
    expect(screen.getByText('-0.21%')).toHaveClass('negative')
  })

  it('displays an alert when loading indexes fails', async () => {
    mockedGetMarketIndexes.mockRejectedValue(new Error('network error'))

    render(<MarketIndexes />)

    expect(await screen.findByRole('alert')).toHaveTextContent(
      '주요 지수를 불러오지 못했습니다.',
    )
  })
})
