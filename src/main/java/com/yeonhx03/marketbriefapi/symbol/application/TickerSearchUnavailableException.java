package com.yeonhx03.marketbriefapi.symbol.application;

public class TickerSearchUnavailableException extends RuntimeException {

    public TickerSearchUnavailableException() {
        super("Ticker search provider is unavailable");
    }

    public TickerSearchUnavailableException(Throwable cause) {
        super("Ticker search provider is unavailable", cause);
    }
}
