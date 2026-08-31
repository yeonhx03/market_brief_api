package com.yeonhx03.marketbriefapi.heatmap.application;

import com.yeonhx03.marketbriefapi.heatmap.api.MarketHeatmapSectorResponse;

import java.util.List;

public interface MarketHeatmapService {

    List<MarketHeatmapSectorResponse> getHeatmap();
}
