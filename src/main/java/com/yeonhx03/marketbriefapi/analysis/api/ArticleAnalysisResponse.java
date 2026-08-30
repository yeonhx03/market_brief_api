package com.yeonhx03.marketbriefapi.analysis.api;

import java.time.OffsetDateTime;

public record ArticleAnalysisResponse(
        Long analysisId,
        Long articleId,
        String analysisType,
        String analyzerName,
        String analyzerVersion,
        OffsetDateTime analyzedAt,
        String textSentiment,
        Double positiveScore,
        Double neutralScore,
        Double negativeScore,
        Double confidence
) {
}
