import { MarketHeader } from '../components/MarketHeader'
import { MarketHeatmap } from '../features/market/MarketHeatmap'
import type { MarketClock } from '../types/market'
import { MarketIndexes } from '../features/market/MarketIndexes'
import { OptionsOverview } from '../features/market/OptionsOverview'
type DashboardPageProps = {
  clocks: MarketClock[]
}

export function DashboardPage({ clocks }: DashboardPageProps) {
  return (
    <div className="dashboard">
      <MarketHeader clocks={clocks} />

      <main className="dashboard-content">
        <section
          className="market-overview"
          aria-labelledby="market-overview-title"
        >
          <h2 id="market-overview-title">시장 현황</h2>

          <MarketIndexes />
          <MarketHeatmap />
          <OptionsOverview />
        </section>

        <aside
          className="briefing-panel"
          aria-labelledby="briefing-panel-title"
        >
          <h2 id="briefing-panel-title">종목 브리핑</h2>

          <div className="placeholder">종목 검색</div>
          <div className="placeholder">종목 요약</div>
          <div className="placeholder placeholder-large">최신 브리핑</div>
        </aside>
      </main>
    </div>
  )
}