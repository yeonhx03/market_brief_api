package com.yeonhx03.marketbriefapi.symbol.api;

public record TickerSearchResponse(
        String ticker,
        String companyName,
        String exchange
) {
}
