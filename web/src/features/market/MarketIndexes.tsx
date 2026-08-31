import { useEffect, useState } from 'react'
import { getMarketIndexes } from '../../api/market'
import type { MarketIndexSummary } from '../../types/market'

export function MarketIndexes() {
  const [indexes, setIndexes] = useState<MarketIndexSummary[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function loadIndexes() {
      try {
        setIndexes(await getMarketIndexes())
      } catch {
        setError('주요 지수를 불러오지 못했습니다.')
      }
    }

    void loadIndexes()
  }, [])

  if (error) {
    return <p role="alert">{error}</p>
  }

  return (
    <section aria-labelledby="market-indexes-title">
      <h3 id="market-indexes-title">주요 지수</h3>

      <ul className="market-index-grid">
        {indexes.map((index) => {
          const isPositive = index.changePercent >= 0
          const changeSign = isPositive ? '+' : ''

          return (
            <li className="market-index-card" key={index.symbol}>
              <span>{index.name}</span>
              <strong>{index.value.toLocaleString('ko-KR')}</strong>
              <span className={isPositive ? 'positive' : 'negative'}>
                {changeSign}
                {index.changePercent.toFixed(2)}%
              </span>
            </li>
          )
        })}
      </ul>
    </section>
  )
}