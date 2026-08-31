import { useState } from 'react'
import { MarketHeader } from '../components/MarketHeader'
import { MarketHeatmap } from '../features/market/MarketHeatmap'
import { MarketIndexes } from '../features/market/MarketIndexes'
import { OptionsOverview } from '../features/market/OptionsOverview'
import { TickerSearch } from '../features/search/TickerSearch'
import { TickerSummary } from '../features/quote/TickerSummary'
import type { MarketClock, TickerSearchResult } from '../types/market'
import { TickerInsightPanel } from '../features/insight/TickerInsightPanel'
import { TickerBriefingPanel } from '../features/briefing/TickerBriefingPanel'


type DashboardPageProps = {
  clocks: MarketClock[]
}

export function DashboardPage({ clocks }: DashboardPageProps) {

  const [selectedTicker, setSelectedTicker] =
    useState<TickerSearchResult | null>(null)
  
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

          <TickerSearch onSelect={setSelectedTicker} />

          {selectedTicker ? (
            <TickerSummary
              key={selectedTicker.ticker}
              selection={selectedTicker}
            />
          ) : (
            <div className="ticker-summary">
              <span>종목을 선택해 주세요.</span>
            </div>
          )}

          {selectedTicker && (
            <TickerInsightPanel
              key={selectedTicker.ticker}
              ticker={selectedTicker.ticker}
            />
          )}

          {selectedTicker ? (
            <TickerBriefingPanel
              key={selectedTicker.ticker}
              ticker={selectedTicker.ticker}
            />
          ) : (
            <div className="ticker-briefing">
              브리핑을 확인할 종목을 선택해 주세요.
            </div>
          )}
        </aside>
      </main>
    </div>
  )
}
