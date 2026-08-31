import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { searchTickers } from '../../api/market'
import { TickerSearch } from './TickerSearch'

vi.mock('../../api/market', () => ({
  searchTickers: vi.fn(),
}))

const mockedSearchTickers = vi.mocked(searchTickers)

describe('TickerSearch', () => {
  beforeEach(() => {
    mockedSearchTickers.mockReset()
  })

  it('searches after typing and passes the selected ticker to its parent', async () => {
    const user = userEvent.setup()
    const onSelect = vi.fn()
    const apple = {
      ticker: 'AAPL',
      companyName: 'Apple',
      exchange: 'NASDAQ',
    }
    mockedSearchTickers.mockResolvedValue([apple])

    render(<TickerSearch onSelect={onSelect} />)

    const input = screen.getByRole('searchbox', { name: '티커 또는 종목명' })
    await user.type(input, 'app')

    expect(screen.getByRole('status')).toHaveTextContent('검색 중...')

    const result = await screen.findByRole('button', { name: /AAPL.*Apple/ })
    expect(mockedSearchTickers).toHaveBeenCalledOnce()
    expect(mockedSearchTickers).toHaveBeenCalledWith('app')

    await user.click(result)

    expect(onSelect).toHaveBeenCalledWith(apple)
    expect(input).toHaveValue('AAPL')
    expect(result).not.toBeInTheDocument()
  })

  it('displays an alert when ticker search fails', async () => {
    const user = userEvent.setup()
    mockedSearchTickers.mockRejectedValue(new Error('network error'))

    render(<TickerSearch onSelect={vi.fn()} />)
    await user.type(
      screen.getByRole('searchbox', { name: '티커 또는 종목명' }),
      'nvda',
    )

    expect(await screen.findByRole('alert')).toHaveTextContent(
      '종목을 검색하지 못했습니다.',
    )
  })
})
