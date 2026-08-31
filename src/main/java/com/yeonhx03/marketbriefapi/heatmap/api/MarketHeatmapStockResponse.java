package com.yeonhx03.marketbriefapi.heatmap.api;

public record MarketHeatmapStockResponse(
        String ticker,
        String companyName,
        double marketCapWeight,
        double changePercent
) {
}
