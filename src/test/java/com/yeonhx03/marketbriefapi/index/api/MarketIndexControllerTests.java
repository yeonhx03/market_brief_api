package com.yeonhx03.marketbriefapi.index.api;

import com.yeonhx03.marketbriefapi.index.application.MarketIndexService;
import com.yeonhx03.marketbriefapi.index.application.MarketIndexUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MarketIndexController.class)
class MarketIndexControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MarketIndexService marketIndexService;

    @Test
    void returnsMarketIndexes() throws Exception {
        given(marketIndexService.getIndexes()).willReturn(List.of(
                new MarketIndexResponse("SPX", "S&P 500", 6481.32, 0.42),
                new MarketIndexResponse("SOXX", "SOXX", 298.41, 1.17)
        ));

        mockMvc.perform(get("/api/market/indexes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticker").value("SPX"))
                .andExpect(jsonPath("$[0].name").value("S&P 500"))
                .andExpect(jsonPath("$[0].value").value(6481.32))
                .andExpect(jsonPath("$[0].changePercent").value(0.42))
                .andExpect(jsonPath("$[1].ticker").value("SOXX"));

        then(marketIndexService).should().getIndexes();
    }

    @Test
    void returnsServiceUnavailableWithoutLeakingProviderDetails() throws Exception {
        given(marketIndexService.getIndexes())
                .willThrow(new MarketIndexUnavailableException());

        mockMvc.perform(get("/api/market/indexes"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("MARKET_INDEX_UNAVAILABLE"))
                .andExpect(jsonPath("$.message")
                        .value("Market indexes are temporarily unavailable"));
    }
}
