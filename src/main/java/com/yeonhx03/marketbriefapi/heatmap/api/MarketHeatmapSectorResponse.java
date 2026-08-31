package com.yeonhx03.marketbriefapi.heatmap.api;

import java.util.List;

public record MarketHeatmapSectorResponse(
        String sectorId,
        String sectorName,
        List<MarketHeatmapStockResponse> stocks
) {
}
