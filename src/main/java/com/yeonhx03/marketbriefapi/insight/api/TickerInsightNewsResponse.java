package com.yeonhx03.marketbriefapi.insight.api;

import java.time.OffsetDateTime;

public record TickerInsightNewsResponse(
        Long articleId,
        String title,
        String source,
        OffsetDateTime publishedAt,
        String url
) {
}
