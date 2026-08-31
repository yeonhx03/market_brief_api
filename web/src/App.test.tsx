import { render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  getMarketClocks,
  getMarketHeatmap,
  getMarketIndexes,
  getOptionSnapshots,
} from './api/market'
import App from './App'

vi.mock('./api/market', () => ({
  getMarketClocks: vi.fn(),
  getMarketHeatmap: vi.fn(),
  getMarketIndexes: vi.fn(),
  getOptionSnapshots: vi.fn(),
}))

const mockedGetMarketClocks = vi.mocked(getMarketClocks)
const mockedGetMarketHeatmap = vi.mocked(getMarketHeatmap)
const mockedGetMarketIndexes = vi.mocked(getMarketIndexes)
const mockedGetOptionSnapshots = vi.mocked(getOptionSnapshots)

describe('App', () => {
  beforeEach(() => {
    mockedGetMarketClocks.mockReset()
    mockedGetMarketHeatmap.mockReset()
    mockedGetMarketIndexes.mockReset()
    mockedGetOptionSnapshots.mockReset()
    mockedGetMarketHeatmap.mockResolvedValue([])
    mockedGetMarketIndexes.mockResolvedValue([])
    mockedGetOptionSnapshots.mockResolvedValue([])
  })

  it('loads and displays the market clocks', async () => {
    mockedGetMarketClocks.mockResolvedValue([
      {
        city: '서울',
        timeZone: 'Asia/Seoul',
        displayTime: '08:30',
        status: '장 시작 전',
      },
      {
        city: '뉴욕',
        timeZone: 'America/New_York',
        displayTime: '19:30',
        status: '장 마감',
      },
    ])

    render(<App />)

    expect(
      screen.getByRole('heading', { name: 'market_brief' }),
    ).toBeInTheDocument()
    expect(await screen.findByText('08:30')).toBeInTheDocument()
    expect(screen.getByText('19:30')).toBeInTheDocument()
    expect(screen.getByText('장 시작 전')).toBeInTheDocument()
    expect(screen.getByText('장 마감')).toBeInTheDocument()
    expect(screen.getByRole('main')).toBeInTheDocument()
    expect(
      screen.getByRole('region', { name: '시장 현황' }),
    ).toBeInTheDocument()
    expect(
      screen.getByRole('complementary', { name: '종목 브리핑' }),
    ).toBeInTheDocument()
  })

  it('displays an alert when loading the market clocks fails', async () => {
    mockedGetMarketClocks.mockRejectedValue(new Error('network error'))

    render(<App />)

    expect(await screen.findByRole('alert')).toHaveTextContent(
      '시장 시각을 불러오지 못했습니다.',
    )
  })
})
