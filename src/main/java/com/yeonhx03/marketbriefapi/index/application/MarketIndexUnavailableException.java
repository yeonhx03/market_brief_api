package com.yeonhx03.marketbriefapi.index.application;

public class MarketIndexUnavailableException extends RuntimeException {

    public MarketIndexUnavailableException() {
        super("Market index provider is unavailable");
    }

    public MarketIndexUnavailableException(Throwable cause) {
        super("Market index provider is unavailable", cause);
    }
}
