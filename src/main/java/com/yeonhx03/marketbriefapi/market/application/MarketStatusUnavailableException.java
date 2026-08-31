package com.yeonhx03.marketbriefapi.market.application;

public class MarketStatusUnavailableException extends RuntimeException {

    public MarketStatusUnavailableException() {
        super("Market status provider is unavailable");
    }

    public MarketStatusUnavailableException(Throwable cause) {
        super("Market status provider is unavailable", cause);
    }
}
