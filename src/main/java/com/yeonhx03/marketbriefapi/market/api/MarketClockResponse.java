package com.yeonhx03.marketbriefapi.market.api;

public record MarketClockResponse(
        String city,
        String timeZone,
        String displayTime,
        String status
) {
}
