import { render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getMarketHeatmap } from '../../api/market'
import { MarketHeatmap } from './MarketHeatmap'

vi.mock('../../api/market', () => ({
  getMarketHeatmap: vi.fn(),
}))

const mockedGetMarketHeatmap = vi.mocked(getMarketHeatmap)

describe('MarketHeatmap', () => {
  beforeEach(() => {
    mockedGetMarketHeatmap.mockReset()
  })

  it('groups stocks by sector and maps weights and changes to styles', async () => {
    mockedGetMarketHeatmap.mockResolvedValue([
      {
        sectorId: 'technology',
        sectorName: '기술',
        stocks: [
          {
            symbol: 'NVDA',
            companyName: 'NVIDIA',
            marketCapWeight: 6.8,
            changePercent: 2.41,
          },
          {
            symbol: 'AAPL',
            companyName: 'Apple',
            marketCapWeight: 5.9,
            changePercent: -0.84,
          },
        ],
      },
      {
        sectorId: 'consumer',
        sectorName: '소비재',
        stocks: [
          {
            symbol: 'AMZN',
            companyName: 'Amazon',
            marketCapWeight: 3.2,
            changePercent: -1.12,
          },
        ],
      },
    ])

    render(<MarketHeatmap />)

    const technologySector = (await screen.findByRole('heading', {
      name: '기술',
    })).closest('li')
    const nvdaCell = screen.getByLabelText('NVIDIA 2.41%')
    const appleCell = screen.getByLabelText('Apple -0.84%')
    const amazonCell = screen.getByLabelText('Amazon -1.12%')

    expect(technologySector).toHaveStyle({ flexGrow: '12.7' })
    expect(technologySector).toContainElement(nvdaCell)
    expect(technologySector).toContainElement(appleCell)
    expect(technologySector).not.toContainElement(amazonCell)
    expect(nvdaCell).toHaveTextContent('+2.41%')
    expect(nvdaCell.style.backgroundColor).toContain('rgba(18, 160, 100')
    expect(appleCell.style.backgroundColor).toContain('rgba(196, 55, 65')
    expect(amazonCell).toHaveTextContent('-1.12%')
  })

  it('displays an alert when loading the heatmap fails', async () => {
    mockedGetMarketHeatmap.mockRejectedValue(new Error('network error'))

    render(<MarketHeatmap />)

    expect(await screen.findByRole('alert')).toHaveTextContent(
      '시장 히트맵을 불러오지 못했습니다.',
    )
  })
})
