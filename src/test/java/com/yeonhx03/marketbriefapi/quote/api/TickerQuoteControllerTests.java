package com.yeonhx03.marketbriefapi.quote.api;

import com.yeonhx03.marketbriefapi.quote.application.TickerQuoteNotFoundException;
import com.yeonhx03.marketbriefapi.quote.application.TickerQuoteService;
import com.yeonhx03.marketbriefapi.quote.application.TickerQuoteUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TickerQuoteController.class)
class TickerQuoteControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TickerQuoteService tickerQuoteService;

    @Test
    void returnsTickerQuote() throws Exception {
        given(tickerQuoteService.getQuote("aapl")).willReturn(
                new TickerQuoteResponse(
                        "AAPL",
                        "Apple Inc",
                        231.42,
                        0.95,
                        null,
                        "USD",
                        Instant.parse("2026-08-31T14:30:00Z")
                )
        );

        mockMvc.perform(get("/api/tickers/aapl/quote"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticker").value("AAPL"))
                .andExpect(jsonPath("$.companyName").value("Apple Inc"))
                .andExpect(jsonPath("$.currentPrice").value(231.42))
                .andExpect(jsonPath("$.changePercent").value(0.95))
                .andExpect(jsonPath("$.volume").value(nullValue()))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.dataAsOf")
                        .value("2026-08-31T14:30:00Z"));

        then(tickerQuoteService).should().getQuote("aapl");
    }

    @Test
    void rejectsInvalidTicker() throws Exception {
        mockMvc.perform(get("/api/tickers/invalid_ticker/quote"))
                .andExpect(status().isBadRequest());

        then(tickerQuoteService).shouldHaveNoInteractions();
    }

    @Test
    void returnsNotFoundForUnknownTicker() throws Exception {
        given(tickerQuoteService.getQuote("UNKNOWN"))
                .willThrow(new TickerQuoteNotFoundException());

        mockMvc.perform(get("/api/tickers/UNKNOWN/quote"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TICKER_QUOTE_NOT_FOUND"));
    }

    @Test
    void returnsServiceUnavailableWithoutLeakingProviderDetails() throws Exception {
        given(tickerQuoteService.getQuote("AAPL"))
                .willThrow(new TickerQuoteUnavailableException());

        mockMvc.perform(get("/api/tickers/AAPL/quote"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("TICKER_QUOTE_UNAVAILABLE"))
                .andExpect(jsonPath("$.message")
                        .value("Ticker quote is temporarily unavailable"));
    }
}
