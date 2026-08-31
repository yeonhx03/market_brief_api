package com.yeonhx03.marketbriefapi.insight.api;

import com.yeonhx03.marketbriefapi.insight.application.TickerInsightNotFoundException;
import com.yeonhx03.marketbriefapi.insight.application.TickerInsightService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TickerInsightController.class)
class TickerInsightControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TickerInsightService tickerInsightService;

    @Test
    void returnsRelatedNewsAndFinbertSentiment() throws Exception {
        given(tickerInsightService.getInsight("aapl", 5)).willReturn(
                new TickerInsightResponse(
                        "AAPL",
                        2,
                        2,
                        OffsetDateTime.parse("2026-08-31T14:30:00Z"),
                        new TickerSentimentResponse(0.62, 0.28, 0.10),
                        List.of(new TickerInsightNewsResponse(
                                42L,
                                "Apple earnings outlook",
                                "Reuters",
                                OffsetDateTime.parse("2026-08-31T13:10:00Z"),
                                "https://example.com/apple"
                        ))
                )
        );

        mockMvc.perform(get("/api/tickers/aapl/insight")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticker").value("AAPL"))
                .andExpect(jsonPath("$.articleCount").value(2))
                .andExpect(jsonPath("$.sourceCount").value(2))
                .andExpect(jsonPath("$.dataAsOf")
                        .value("2026-08-31T14:30:00Z"))
                .andExpect(jsonPath("$.sentiment.positive").value(0.62))
                .andExpect(jsonPath("$.sentiment.neutral").value(0.28))
                .andExpect(jsonPath("$.sentiment.negative").value(0.10))
                .andExpect(jsonPath("$.articles[0].articleId").value(42))
                .andExpect(jsonPath("$.articles[0].source").value("Reuters"));

        then(tickerInsightService).should().getInsight("aapl", 5);
    }

    @Test
    void usesDefaultLimit() throws Exception {
        given(tickerInsightService.getInsight("AAPL", 10))
                .willThrow(new TickerInsightNotFoundException());

        mockMvc.perform(get("/api/tickers/AAPL/insight"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("TICKER_INSIGHT_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("Ticker insight was not found"));

        then(tickerInsightService).should().getInsight("AAPL", 10);
    }

    @Test
    void rejectsInvalidTickerOrLimit() throws Exception {
        mockMvc.perform(get("/api/tickers/AAPL!/insight"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/tickers/AAPL/insight").param("limit", "51"))
                .andExpect(status().isBadRequest());

        then(tickerInsightService).shouldHaveNoInteractions();
    }
}
