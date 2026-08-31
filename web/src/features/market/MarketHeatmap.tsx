import { useEffect, useState } from 'react'
import { getMarketHeatmap } from '../../api/market'
import type { MarketHeatmapSector } from '../../types/market'

function getChangeColor(changePercent: number) {
  if (changePercent === 0) return '#303744'

  const strength = Math.min(Math.abs(changePercent) / 3, 1)
  const alpha = 0.25 + strength * 0.65

  return changePercent > 0
    ? `rgba(18, 160, 100, ${alpha})`
    : `rgba(196, 55, 65, ${alpha})`
}

export function MarketHeatmap() {
  const [sectors, setSectors] = useState<MarketHeatmapSector[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function loadHeatmap() {
      try {
        setSectors(await getMarketHeatmap())
      } catch {
        setError('시장 히트맵을 불러오지 못했습니다.')
      }
    }

    void loadHeatmap()
  }, [])

  if (error) return <p role="alert">{error}</p>

  return (
    <section aria-labelledby="market-heatmap-title">
      <h3 id="market-heatmap-title">시장 히트맵</h3>

      <ul className="heatmap-sectors">
        {sectors.map((sector) => {
          const sectorWeight = sector.stocks.reduce(
            (sum, stock) => sum + stock.marketCapWeight,
            0,
          )

          return (
            <li
              className="heatmap-sector"
              key={sector.sectorId}
              style={{ flexGrow: sectorWeight }}
            >
              <h4>{sector.sectorName}</h4>

              <ul className="heatmap-stocks">
                {sector.stocks.map((stock) => (
                  <li
                    className="heatmap-stock"
                    key={stock.ticker}
                    style={{
                      flexGrow: stock.marketCapWeight,
                      backgroundColor: getChangeColor(stock.changePercent),
                    }}
                    aria-label={`${stock.companyName} ${stock.changePercent}%`}
                  >
                    <strong>{stock.ticker}</strong>
                    <span>
                      {stock.changePercent > 0 ? '+' : ''}
                      {stock.changePercent.toFixed(2)}%
                    </span>
                  </li>
                ))}
              </ul>
            </li>
          )
        })}
      </ul>
    </section>
  )
}
