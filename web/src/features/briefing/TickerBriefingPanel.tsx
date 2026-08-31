import { useEffect, useState } from 'react'
import {
  getTickerBriefing,
  requestTickerBriefing,
} from '../../api/briefing'
import type { TickerBriefingState } from '../../types/briefing'

type TickerBriefingPanelProps = {
  ticker: string
}

export function TickerBriefingPanel({
  ticker,
}: TickerBriefingPanelProps) {
  const [briefing, setBriefing] =
    useState<TickerBriefingState | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    async function loadBriefing() {
      try {
        let response = await getTickerBriefing(ticker)

        if (response.status === 'missing') {
          response = await requestTickerBriefing(ticker)
        }

        if (!cancelled) {
          setBriefing(response)
        }
      } catch {
        if (!cancelled) {
          setError('브리핑 상태를 불러오지 못했습니다.')
        }
      }
    }

    void loadBriefing()

    return () => {
      cancelled = true
    }
  }, [ticker])

  if (error) {
    return <p role="alert">{error}</p>
  }

  if (!briefing) {
    return <div className="ticker-briefing">브리핑 확인 중...</div>
  }

  if (briefing.status === 'missing') {
    return <div className="ticker-briefing">저장된 브리핑이 없습니다.</div>
  }

  if (briefing.status === 'generating') {
    return (
      <section className="ticker-briefing" aria-live="polite">
        <h3>한국어 브리핑</h3>
        <p>브리핑 생성 중...</p>
      </section>
    )
  }

  if (briefing.status === 'failed') {
    return (
      <section className="ticker-briefing">
        <h3>한국어 브리핑</h3>
        <p role="alert">{briefing.message}</p>
      </section>
    )
  }

  return (
    <section
      className="ticker-briefing"
      aria-labelledby="ticker-briefing-title"
    >
      <h3 id="ticker-briefing-title">한국어 브리핑</h3>
      <p>{briefing.briefingText}</p>

      <h4>근거 기사</h4>
      <ul>
        {briefing.evidenceArticles.map((article) => (
          <li key={article.articleId}>
            <a href={article.url} target="_blank" rel="noreferrer">
              {article.title}
            </a>
            <small>{article.source}</small>
          </li>
        ))}
      </ul>

      <small>
        생성 시각:{' '}
        {new Date(briefing.generatedAt).toLocaleString('ko-KR', {
          timeZone: 'Asia/Seoul',
        })}
      </small>
    </section>
  )
}