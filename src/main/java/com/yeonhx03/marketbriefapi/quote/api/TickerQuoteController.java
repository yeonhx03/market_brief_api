package com.yeonhx03.marketbriefapi.quote.api;

import com.yeonhx03.marketbriefapi.quote.application.TickerQuoteService;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickers")
public class TickerQuoteController {

    private final TickerQuoteService tickerQuoteService;

    public TickerQuoteController(TickerQuoteService tickerQuoteService) {
        this.tickerQuoteService = tickerQuoteService;
    }

    @GetMapping("/{ticker}/quote")
    TickerQuoteResponse getQuote(
            @PathVariable
            @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9.-]{0,14}")
            String ticker
    ) {
        return tickerQuoteService.getQuote(ticker);
    }
}
