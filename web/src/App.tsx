import { useEffect, useState } from 'react'
import { getMarketClocks } from './api/market'
import { DashboardPage } from './pages/DashboardPage'
import type { MarketClock } from './types/market'
import './App.css'


function App() {
  const [clocks, setClocks] = useState<MarketClock[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function loadMarketClocks() {
      try {
        const result = await getMarketClocks()
        setClocks(result)
      } catch {
        setError('시장 시각을 불러오지 못했습니다.')
      }
    }

    void loadMarketClocks()
  }, [])

  if (error) {
    return <p role="alert">{error}</p>
  }

  return <DashboardPage clocks={clocks} />
}

export default App