export type TickerNewsItem = {
  articleId: number
  title: string
  source: string
  publishedAt: string
  url: string
}

export type SentimentScores = {
  positive: number
  neutral: number
  negative: number
}

export type TickerInsight = {
  ticker: string
  articleCount: number
  sourceCount: number
  dataAsOf: string
  sentiment: SentimentScores
  articles: TickerNewsItem[]
}