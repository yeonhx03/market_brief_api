package com.yeonhx03.marketbriefapi.heatmap.application;

public class MarketHeatmapUnavailableException extends RuntimeException {

    public MarketHeatmapUnavailableException() {
        super("Market heatmap provider is unavailable");
    }

    public MarketHeatmapUnavailableException(Throwable cause) {
        super("Market heatmap provider is unavailable", cause);
    }
}
