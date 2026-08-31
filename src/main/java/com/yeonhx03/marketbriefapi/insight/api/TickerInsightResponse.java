package com.yeonhx03.marketbriefapi.insight.api;

import java.time.OffsetDateTime;
import java.util.List;

public record TickerInsightResponse(
        String ticker,
        int articleCount,
        int sourceCount,
        OffsetDateTime dataAsOf,
        TickerSentimentResponse sentiment,
        List<TickerInsightNewsResponse> articles
) {
}
