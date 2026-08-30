package com.yeonhx03.marketbriefapi.analysis.api;

public record ArticleAnalysisErrorResponse(
        String code,
        String message,
        Long existingAnalysisId
) {
}
