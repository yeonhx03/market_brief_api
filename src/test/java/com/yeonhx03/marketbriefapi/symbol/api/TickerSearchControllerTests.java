package com.yeonhx03.marketbriefapi.symbol.api;

import com.yeonhx03.marketbriefapi.symbol.application.TickerSearchService;
import com.yeonhx03.marketbriefapi.symbol.application.TickerSearchUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TickerSearchController.class)
class TickerSearchControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TickerSearchService tickerSearchService;

    @Test
    void returnsTickerSearchResults() throws Exception {
        given(tickerSearchService.search("app", 5)).willReturn(List.of(
                new TickerSearchResponse("AAPL", "APPLE INC", "US")
        ));

        mockMvc.perform(get("/api/tickers/search")
                        .param("query", " app ")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticker").value("AAPL"))
                .andExpect(jsonPath("$[0].companyName").value("APPLE INC"))
                .andExpect(jsonPath("$[0].exchange").value("US"));

        then(tickerSearchService).should().search("app", 5);
    }

    @Test
    void usesDefaultLimitAndReturnsEmptyArray() throws Exception {
        given(tickerSearchService.search("unknown", 10)).willReturn(List.of());

        mockMvc.perform(get("/api/tickers/search")
                        .param("query", "unknown"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        then(tickerSearchService).should().search("unknown", 10);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 21})
    void rejectsLimitOutsideRange(int limit) throws Exception {
        mockMvc.perform(get("/api/tickers/search")
                        .param("query", "apple")
                        .param("limit", Integer.toString(limit)))
                .andExpect(status().isBadRequest());

        then(tickerSearchService).shouldHaveNoInteractions();
    }

    @Test
    void rejectsBlankQuery() throws Exception {
        mockMvc.perform(get("/api/tickers/search")
                        .param("query", " "))
                .andExpect(status().isBadRequest());

        then(tickerSearchService).shouldHaveNoInteractions();
    }

    @Test
    void rejectsQueryLongerThanFiftyCharacters() throws Exception {
        mockMvc.perform(get("/api/tickers/search")
                        .param("query", "a".repeat(51)))
                .andExpect(status().isBadRequest());

        then(tickerSearchService).shouldHaveNoInteractions();
    }

    @Test
    void returnsServiceUnavailableWithoutLeakingProviderDetails() throws Exception {
        given(tickerSearchService.search("apple", 10))
                .willThrow(new TickerSearchUnavailableException());

        mockMvc.perform(get("/api/tickers/search")
                        .param("query", "apple"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("TICKER_SEARCH_UNAVAILABLE"))
                .andExpect(jsonPath("$.message")
                        .value("Ticker search is temporarily unavailable"));
    }
}
