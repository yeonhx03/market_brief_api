package com.yeonhx03.marketbriefapi.symbol.api;

import com.yeonhx03.marketbriefapi.symbol.application.TickerSearchService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickers")
public class TickerSearchController {

    private final TickerSearchService tickerSearchService;

    public TickerSearchController(TickerSearchService tickerSearchService) {
        this.tickerSearchService = tickerSearchService;
    }

    @GetMapping("/search")
    List<TickerSearchResponse> search(
            @RequestParam
            @NotBlank
            @Size(max = 50)
            String query,
            @RequestParam(defaultValue = "10")
            @Min(1)
            @Max(20)
            int limit
    ) {
        return tickerSearchService.search(query.trim(), limit);
    }
}
