package com.yeonhx03.marketbriefapi.index.api;

public record MarketIndexResponse(
        String ticker,
        String name,
        double value,
        double changePercent
) {
}
