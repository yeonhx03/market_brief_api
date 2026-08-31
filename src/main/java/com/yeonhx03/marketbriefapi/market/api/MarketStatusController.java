package com.yeonhx03.marketbriefapi.market.api;

import com.yeonhx03.marketbriefapi.market.application.MarketStatusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/market")
public class MarketStatusController {

    private final MarketStatusService marketStatusService;

    public MarketStatusController(MarketStatusService marketStatusService) {
        this.marketStatusService = marketStatusService;
    }

    @GetMapping("/status")
    List<MarketClockResponse> getStatus() {
        return marketStatusService.getStatus();
    }
}
