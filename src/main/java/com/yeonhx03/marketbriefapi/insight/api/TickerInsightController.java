package com.yeonhx03.marketbriefapi.insight.api;

import com.yeonhx03.marketbriefapi.insight.application.TickerInsightService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/tickers/{ticker}/insight")
public class TickerInsightController {

    private final TickerInsightService tickerInsightService;

    public TickerInsightController(TickerInsightService tickerInsightService) {
        this.tickerInsightService = tickerInsightService;
    }

    @GetMapping
    TickerInsightResponse getInsight(
            @PathVariable
            @Pattern(regexp = "[A-Za-z][A-Za-z0-9.-]{0,15}")
            String ticker,
            @RequestParam(defaultValue = "10")
            @Min(1)
            @Max(50)
            int limit
    ) {
        return tickerInsightService.getInsight(ticker, limit);
    }
}
