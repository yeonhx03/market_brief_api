package com.yeonhx03.marketbriefapi.article.api;

import java.time.OffsetDateTime;

public record ArticleResponse(
        Long id,
        String source,
        String sourceArticleId,
        String title,
        String url,
        String canonicalUrl,
        OffsetDateTime publishedAt,
        OffsetDateTime collectedAt,
        String rawContent,
        String cleanedContent,
        String contentHash
) {
}
