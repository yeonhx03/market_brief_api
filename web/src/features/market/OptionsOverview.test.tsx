import { render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getOptionSnapshots } from '../../api/market'
import { OptionsOverview } from './OptionsOverview'

vi.mock('../../api/market', () => ({
  getOptionSnapshots: vi.fn(),
}))

const mockedGetOptionSnapshots = vi.mocked(getOptionSnapshots)

describe('OptionsOverview', () => {
  beforeEach(() => {
    mockedGetOptionSnapshots.mockReset()
  })

  it('displays option metrics, delay, and data timestamp', async () => {
    mockedGetOptionSnapshots.mockResolvedValue([
      {
        symbol: 'SOXX',
        iv30Day: 0.428,
        putCallRatio: 0.91,
        expectedLow: 277.34,
        expectedHigh: 319.48,
        dataAsOf: '2026-08-28T20:00:00Z',
        delayMinutes: 1440,
      },
    ])

    render(<OptionsOverview />)

    const optionRow = await screen.findByRole('row', { name: /SOXX/ })
    expect(optionRow).toHaveTextContent('42.8%')
    expect(optionRow).toHaveTextContent('0.91')
    expect(optionRow).toHaveTextContent('$277.34–$319.48')
    expect(optionRow).toHaveTextContent('24시간 지연')
    expect(screen.getByText(/기준 시각:/)).toBeInTheDocument()
  })

  it('displays an alert when loading options fails', async () => {
    mockedGetOptionSnapshots.mockRejectedValue(new Error('network error'))

    render(<OptionsOverview />)

    expect(await screen.findByRole('alert')).toHaveTextContent(
      '옵션 현황을 불러오지 못했습니다.',
    )
  })
})
