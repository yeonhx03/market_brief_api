import type { TickerInsight } from '../types/insight'

const API_BASE_URL = (
  import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
).replace(/\/$/, '')

export async function getTickerInsight(
  ticker: string,
): Promise<TickerInsight> {
  const normalizedTicker = ticker.trim().toUpperCase()

  if (!normalizedTicker) {
    throw new Error('Ticker is required')
  }

  const response = await fetch(
    `${API_BASE_URL}/api/tickers/${encodeURIComponent(normalizedTicker)}/insight?limit=10`,
  )

  if (!response.ok) {
    throw new Error(`Ticker insight request failed: ${response.status}`)
  }

  return (await response.json()) as TickerInsight
}
