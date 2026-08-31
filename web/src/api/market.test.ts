import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import {
  getMarketClocks,
  getMarketHeatmap,
  getMarketIndexes,
  getTickerQuote,
  searchTickers,
} from './market'

const fetchMock = vi.fn()

describe('getMarketClocks', () => {
  beforeEach(() => {
    fetchMock.mockReset()
    vi.stubGlobal('fetch', fetchMock)
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('requests the Spring API and returns its market clocks', async () => {
    const clocks = [
      {
        city: '서울',
        timeZone: 'Asia/Seoul',
        displayTime: '09:30',
        status: '정규장' as const,
      },
      {
        city: '뉴욕',
        timeZone: 'America/New_York',
        displayTime: '20:30',
        status: '장 마감' as const,
      },
    ]

    fetchMock.mockResolvedValue(
      new Response(JSON.stringify(clocks), {
        status: 200,
        headers: {
          'Content-Type': 'application/json',
        },
      }),
    )

    await expect(getMarketClocks()).resolves.toEqual(clocks)
    expect(fetchMock).toHaveBeenCalledOnce()

    const [requestUrl] = fetchMock.mock.calls[0] as [string]
    expect(new URL(requestUrl).pathname).toBe('/api/market/status')
  })

  it('throws when the Spring API returns an error response', async () => {
    fetchMock.mockResolvedValue(
      new Response(null, {
        status: 503,
      }),
    )

    await expect(getMarketClocks()).rejects.toThrow(
      'Market status request failed: 503',
    )
  })
})

describe('getMarketIndexes', () => {
  beforeEach(() => {
    fetchMock.mockReset()
    vi.stubGlobal('fetch', fetchMock)
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('inserts the temporary KOSPI fixture into the Spring API results', async () => {
    const providerIndexes = [
      { ticker: 'SPX', name: 'S&P 500', value: 7711.76, changePercent: -0.25 },
      { ticker: 'IXIC', name: 'NASDAQ', value: 26402.42, changePercent: -0.52 },
      { ticker: 'DJI', name: 'DOW', value: 53559.99, changePercent: 0.18 },
      { ticker: 'SOXX', name: 'SOXX', value: 508.62, changePercent: -3.2 },
    ]

    fetchMock.mockResolvedValue(
      new Response(JSON.stringify(providerIndexes), {
        status: 200,
        headers: {
          'Content-Type': 'application/json',
        },
      }),
    )

    await expect(getMarketIndexes()).resolves.toEqual([
      ...providerIndexes.slice(0, 3),
      {
        ticker: 'KOSPI',
        name: 'KOSPI',
        value: 3186.01,
        changePercent: -0.32,
      },
      providerIndexes[3],
    ])

    expect(fetchMock).toHaveBeenCalledOnce()
    const [requestUrl] = fetchMock.mock.calls[0] as [string]
    expect(new URL(requestUrl).pathname).toBe('/api/market/indexes')
  })

  it('throws without returning the KOSPI fixture when Spring fails', async () => {
    fetchMock.mockResolvedValue(
      new Response(null, {
        status: 503,
      }),
    )

    await expect(getMarketIndexes()).rejects.toThrow(
      'Market indexes request failed: 503',
    )
  })
})

describe('getMarketHeatmap', () => {
  beforeEach(() => {
    fetchMock.mockReset()
    vi.stubGlobal('fetch', fetchMock)
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('requests the Spring API and returns its sector hierarchy', async () => {
    const heatmap = [
      {
        sectorId: 'technology',
        sectorName: '기술',
        stocks: [
          {
            ticker: 'NVDA',
            companyName: 'NVIDIA',
            marketCapWeight: 25,
            changePercent: 2.41,
          },
        ],
      },
    ]

    fetchMock.mockResolvedValue(
      new Response(JSON.stringify(heatmap), {
        status: 200,
        headers: {
          'Content-Type': 'application/json',
        },
      }),
    )

    await expect(getMarketHeatmap()).resolves.toEqual(heatmap)
    expect(fetchMock).toHaveBeenCalledOnce()

    const [requestUrl] = fetchMock.mock.calls[0] as [string]
    expect(new URL(requestUrl).pathname).toBe('/api/market/heatmap')
  })

  it('throws when the Spring API returns an error response', async () => {
    fetchMock.mockResolvedValue(
      new Response(null, {
        status: 503,
      }),
    )

    await expect(getMarketHeatmap()).rejects.toThrow(
      'Market heatmap request failed: 503',
    )
  })
})

describe('searchTickers', () => {
  beforeEach(() => {
    fetchMock.mockReset()
    vi.stubGlobal('fetch', fetchMock)
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('returns an empty array without fetching for a blank query', async () => {
    await expect(searchTickers('   ')).resolves.toEqual([])
    expect(fetchMock).not.toHaveBeenCalled()
  })

  it('requests the Spring API and returns its ticker results', async () => {
    const results = [
      {
        ticker: 'AAPL',
        companyName: 'APPLE INC',
        exchange: 'US',
      },
    ]

    fetchMock.mockResolvedValue(
      new Response(JSON.stringify(results), {
        status: 200,
        headers: {
          'Content-Type': 'application/json',
        },
      }),
    )

    await expect(searchTickers(' 애플 ')).resolves.toEqual(results)
    expect(fetchMock).toHaveBeenCalledOnce()

    const [requestUrl] = fetchMock.mock.calls[0] as [string]
    const url = new URL(requestUrl)

    expect(url.pathname).toBe('/api/tickers/search')
    expect(url.searchParams.get('query')).toBe('애플')
  })

  it('throws when the Spring API returns an error response', async () => {
    fetchMock.mockResolvedValue(
      new Response(null, {
        status: 503,
      }),
    )

    await expect(searchTickers('apple')).rejects.toThrow(
      'Ticker search failed: 503',
    )
  })
})

describe('getTickerQuote', () => {
  beforeEach(() => {
    fetchMock.mockReset()
    vi.stubGlobal('fetch', fetchMock)
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('rejects a blank ticker without fetching', async () => {
    await expect(getTickerQuote('   ')).rejects.toThrow('Ticker is required')
    expect(fetchMock).not.toHaveBeenCalled()
  })

  it('requests the normalized ticker quote and returns it', async () => {
    const quote = {
      ticker: 'AAPL',
      companyName: 'Apple Inc',
      currentPrice: 231.42,
      changePercent: 0.95,
      volume: null,
      currency: 'USD',
      dataAsOf: '2026-08-31T14:30:00Z',
    }

    fetchMock.mockResolvedValue(
      new Response(JSON.stringify(quote), {
        status: 200,
        headers: {
          'Content-Type': 'application/json',
        },
      }),
    )

    await expect(getTickerQuote(' aapl ')).resolves.toEqual(quote)
    expect(fetchMock).toHaveBeenCalledOnce()

    const [requestUrl] = fetchMock.mock.calls[0] as [string]
    expect(new URL(requestUrl).pathname).toBe('/api/tickers/AAPL/quote')
  })

  it('throws when the Spring API returns an error response', async () => {
    fetchMock.mockResolvedValue(
      new Response(null, {
        status: 503,
      }),
    )

    await expect(getTickerQuote('AAPL')).rejects.toThrow(
      'Ticker quote request failed: 503',
    )
  })
})
