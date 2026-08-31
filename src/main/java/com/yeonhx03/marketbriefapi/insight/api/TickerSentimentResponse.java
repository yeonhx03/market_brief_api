package com.yeonhx03.marketbriefapi.insight.api;

public record TickerSentimentResponse(
        double positive,
        double neutral,
        double negative
) {
}
