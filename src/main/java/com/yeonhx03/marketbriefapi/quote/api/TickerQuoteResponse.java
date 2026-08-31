package com.yeonhx03.marketbriefapi.quote.api;

import java.time.Instant;

public record TickerQuoteResponse(
        String ticker,
        String companyName,
        double currentPrice,
        double changePercent,
        Long volume,
        String currency,
        Instant dataAsOf
) {
}
