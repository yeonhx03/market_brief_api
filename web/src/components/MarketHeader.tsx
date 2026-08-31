import type { MarketClock } from '../types/market'

type MarketHeaderProps = {
  clocks: MarketClock[]
}

export function MarketHeader({ clocks }: MarketHeaderProps) {
  return (
    <header>
      <h1>market_brief</h1>

      <ul aria-label="시장 시각">
        {clocks.map((clock) => (
          <li className="market-clock" key={clock.timeZone}>
            <span>{clock.city}</span>
            <time>{clock.displayTime}</time>
            <span>{clock.status}</span>
          </li>
        ))}
      </ul>
    </header>
  )
}