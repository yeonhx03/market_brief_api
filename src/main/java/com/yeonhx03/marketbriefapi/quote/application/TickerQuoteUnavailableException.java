package com.yeonhx03.marketbriefapi.quote.application;

public class TickerQuoteUnavailableException extends RuntimeException {

    public TickerQuoteUnavailableException() {
        super("Ticker quote provider is unavailable");
    }

    public TickerQuoteUnavailableException(Throwable cause) {
        super("Ticker quote provider is unavailable", cause);
    }
}
