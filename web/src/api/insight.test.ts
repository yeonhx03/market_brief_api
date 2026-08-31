import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { getTickerInsight } from './insight'

const fetchMock = vi.fn()

describe('getTickerInsight', () => {
  beforeEach(() => {
    fetchMock.mockReset()
    vi.stubGlobal('fetch', fetchMock)
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('rejects a blank ticker without fetching', async () => {
    await expect(getTickerInsight('   ')).rejects.toThrow('Ticker is required')
    expect(fetchMock).not.toHaveBeenCalled()
  })

  it('requests the normalized ticker insight and returns it', async () => {
    const insight = {
      ticker: 'AAPL',
      articleCount: 1,
      sourceCount: 1,
      dataAsOf: '2026-08-31T14:30:00Z',
      sentiment: {
        positive: 0.62,
        neutral: 0.28,
        negative: 0.1,
      },
      articles: [
        {
          articleId: 42,
          title: 'Apple earnings outlook',
          source: 'Reuters',
          publishedAt: '2026-08-31T13:10:00Z',
          url: 'https://example.com/apple',
        },
      ],
    }

    fetchMock.mockResolvedValue(
      new Response(JSON.stringify(insight), {
        status: 200,
        headers: {
          'Content-Type': 'application/json',
        },
      }),
    )

    await expect(getTickerInsight(' aapl ')).resolves.toEqual(insight)
    expect(fetchMock).toHaveBeenCalledOnce()

    const [requestUrl] = fetchMock.mock.calls[0] as [string]
    const url = new URL(requestUrl)
    expect(url.pathname).toBe('/api/tickers/AAPL/insight')
    expect(url.searchParams.get('limit')).toBe('10')
  })

  it('throws when the Spring API returns an error response', async () => {
    fetchMock.mockResolvedValue(
      new Response(null, {
        status: 404,
      }),
    )

    await expect(getTickerInsight('AAPL')).rejects.toThrow(
      'Ticker insight request failed: 404',
    )
  })
})
