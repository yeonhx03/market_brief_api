package com.yeonhx03.marketbriefapi.index.api;

import com.yeonhx03.marketbriefapi.index.application.MarketIndexService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/market/indexes")
public class MarketIndexController {

    private final MarketIndexService marketIndexService;

    public MarketIndexController(MarketIndexService marketIndexService) {
        this.marketIndexService = marketIndexService;
    }

    @GetMapping
    List<MarketIndexResponse> getIndexes() {
        return marketIndexService.getIndexes();
    }
}
