package com.yeonhx03.marketbriefapi.insight.application;

public class TickerInsightNotFoundException extends RuntimeException {

    public TickerInsightNotFoundException() {
        super("Ticker insight was not found");
    }
}
