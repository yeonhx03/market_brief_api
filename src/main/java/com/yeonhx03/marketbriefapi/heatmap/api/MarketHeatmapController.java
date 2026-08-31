package com.yeonhx03.marketbriefapi.heatmap.api;

import com.yeonhx03.marketbriefapi.heatmap.application.MarketHeatmapService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/market/heatmap")
public class MarketHeatmapController {

    private final MarketHeatmapService marketHeatmapService;

    public MarketHeatmapController(MarketHeatmapService marketHeatmapService) {
        this.marketHeatmapService = marketHeatmapService;
    }

    @GetMapping
    List<MarketHeatmapSectorResponse> getHeatmap() {
        return marketHeatmapService.getHeatmap();
    }
}
