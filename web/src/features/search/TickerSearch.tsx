import { useEffect, useState } from 'react'
import { searchTickers } from '../../api/market'
import type { TickerSearchResult } from '../../types/market'

type TickerSearchProps = {
  onSelect: (result: TickerSearchResult) => void
}

export function TickerSearch({ onSelect }: TickerSearchProps) {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<TickerSearchResult[]>([])
  const [isSearching, setIsSearching] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!query.trim()) return

    let cancelled = false

    const timeoutId = window.setTimeout(async () => {
      try {
        const response = await searchTickers(query)

        if (!cancelled) {
          setResults(response)
          setError(null)
        }
      } catch {
        if (!cancelled) {
          setError('종목을 검색하지 못했습니다.')
        }
      } finally {
        if (!cancelled) {
          setIsSearching(false)
        }
      }
    }, 250)

    return () => {
      cancelled = true
      window.clearTimeout(timeoutId)
    }
  }, [query])

  function changeQuery(nextQuery: string) {
    setQuery(nextQuery)
    setError(null)

    if (nextQuery.trim()) {
      setIsSearching(true)
    } else {
      setIsSearching(false)
      setResults([])
    }
  }

  function selectResult(result: TickerSearchResult) {
    onSelect(result)
    setQuery(result.ticker)
    setResults([])
  }

  return (
    <section className="ticker-search" aria-labelledby="ticker-search-title">
      <h3 id="ticker-search-title">종목 검색</h3>

      <label htmlFor="ticker-query">티커 또는 종목명</label>
      <input
        id="ticker-query"
        type="search"
        value={query}
        onChange={(event) => changeQuery(event.target.value)}
        autoComplete="off"
      />

      {isSearching && <p role="status">검색 중...</p>}
      {error && <p role="alert">{error}</p>}

      {results.length > 0 && (
        <ul className="ticker-results">
          {results.map((result) => (
            <li key={result.ticker}>
              <button type="button" onClick={() => selectResult(result)}>
                <strong>{result.ticker}</strong>
                <span>{result.companyName}</span>
                <small>{result.exchange}</small>
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
