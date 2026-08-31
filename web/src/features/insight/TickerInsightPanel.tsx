import { useEffect, useState } from 'react'
import { getTickerInsight } from '../../api/insight'
import type { TickerInsight } from '../../types/insight'

type TickerInsightPanelProps = {
  ticker: string
}

export function TickerInsightPanel({ ticker }: TickerInsightPanelProps) {
  const [insight, setInsight] = useState<TickerInsight | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    async function loadInsight() {
      try {
        const response = await getTickerInsight(ticker)

        if (!cancelled) setInsight(response)
      } catch {
        if (!cancelled) {
          setError('관련 뉴스와 감성을 불러오지 못했습니다.')
        }
      } finally {
        if (!cancelled) setIsLoading(false)
      }
    }

    void loadInsight()

    return () => {
      cancelled = true
    }
  }, [ticker])

  if (isLoading) return <div className="ticker-insight">뉴스 조회 중...</div>
  if (error || !insight) return <p role="alert">{error ?? '뉴스가 없습니다.'}</p>

  const scores = [
    ['긍정', insight.sentiment.positive, 'positive'],
    ['중립', insight.sentiment.neutral, 'neutral'],
    ['부정', insight.sentiment.negative, 'negative'],
  ] as const

  return (
    <section className="ticker-insight" aria-labelledby="ticker-news-title">
      <h3 id="ticker-news-title">관련 뉴스</h3>
      <p className="insight-meta">
        기사 {insight.articleCount}건 · 출처 {insight.sourceCount}곳
        <br />
        기준 시각:{' '}
        {new Date(insight.dataAsOf).toLocaleString('ko-KR', {
            timeZone: 'Asia/Seoul',
        })}
      </p>

      <ul className="ticker-news-list">
        {insight.articles.map((article) => (
          <li key={article.articleId}>
            <a href={article.url} target="_blank" rel="noreferrer">
              {article.title}
            </a>
            <small>{article.source}</small>
          </li>
        ))}
      </ul>

      <h3>FinBERT 문장 감성</h3>
      <dl className="sentiment-scores">
        {scores.map(([label, score, className]) => (
          <div key={label}>
            <dt>{label}</dt>
            <dd>
              <progress className={className} max={1} value={score} />
              <span>{(score * 100).toFixed(0)}%</span>
            </dd>
          </div>
        ))}
      </dl>

      <p className="sentiment-notice">
        문장 감성이며 매매 신호가 아닙니다.
      </p>
    </section>
  )
}