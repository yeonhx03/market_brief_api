export type BriefingEvidenceArticle = {
  articleId: number
  title: string
  source: string
  url: string
}

export type TickerBriefingState =
  | {
      status: 'missing'
      ticker: string
      reason: 'not_found' | 'stale'
    }
  | {
      status: 'generating'
      ticker: string
      requestedAt: string
    }
  | {
      status: 'ready'
      ticker: string
      briefingId: number
      generatedAt: string
      briefingText: string
      evidenceArticles: BriefingEvidenceArticle[]
    }
  | {
      status: 'failed'
      ticker: string
      message: string
    }