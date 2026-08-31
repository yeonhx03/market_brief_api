import { useEffect, useState } from 'react'
import { getTickerQuote } from '../../api/market'
import type { TickerQuote, TickerSearchResult } from '../../types/market'

type TickerSummaryProps = {
  selection: TickerSearchResult
}

export function TickerSummary({ selection }: TickerSummaryProps) {
  const [quote, setQuote] = useState<TickerQuote | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    async function loadQuote() {
      try {
        const response = await getTickerQuote(selection.ticker)

        if (!cancelled) {
          setQuote(response)
        }
      } catch {
        if (!cancelled) {
          setError('종목 시세를 불러오지 못했습니다.')
        }
      } finally {
        if (!cancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadQuote()

    return () => {
      cancelled = true
    }
  }, [selection.ticker])

  if (isLoading) {
    return <div className="ticker-summary">시세 조회 중...</div>
  }

  if (error || !quote) {
    return (
      <div className="ticker-summary">
        <p role="alert">{error ?? '시세 데이터가 없습니다.'}</p>
      </div>
    )
  }

  const changeClass = quote.changePercent >= 0 ? 'positive' : 'negative'

  return (
    <section className="ticker-summary" aria-labelledby="ticker-summary-title">
      <header>
        <div>
          <h3 id="ticker-summary-title">{quote.ticker}</h3>
          <span>{quote.companyName}</span>
        </div>

        <strong>
          {new Intl.NumberFormat('ko-KR', {
            style: 'currency',
            currency: quote.currency,
          }).format(quote.currentPrice)}
        </strong>
      </header>

      <dl>
        <div>
          <dt>등락률</dt>
          <dd className={changeClass}>
            {quote.changePercent >= 0 ? '+' : ''}
            {quote.changePercent.toFixed(2)}%
          </dd>
        </div>
        <div>
          <dt>거래량</dt>
          <dd>
            {quote.volume === null
              ? '데이터 없음'
              : quote.volume.toLocaleString('ko-KR')}
          </dd>
        </div>
      </dl>

      <small>
        기준 시각:{' '}
        {new Date(quote.dataAsOf).toLocaleString('ko-KR', {
          timeZone: 'Asia/Seoul',
        })}
      </small>
    </section>
  )
}