package com.yeonhx03.marketbriefapi.quote.application;

public class TickerQuoteNotFoundException extends RuntimeException {

    public TickerQuoteNotFoundException() {
        super("Ticker quote was not found");
    }
}
