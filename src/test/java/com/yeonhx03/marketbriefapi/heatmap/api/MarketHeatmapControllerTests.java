package com.yeonhx03.marketbriefapi.heatmap.api;

import com.yeonhx03.marketbriefapi.heatmap.application.MarketHeatmapService;
import com.yeonhx03.marketbriefapi.heatmap.application.MarketHeatmapUnavailableException;
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

@WebMvcTest(MarketHeatmapController.class)
class MarketHeatmapControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MarketHeatmapService marketHeatmapService;

    @Test
    void returnsMarketHeatmap() throws Exception {
        given(marketHeatmapService.getHeatmap()).willReturn(List.of(
                new MarketHeatmapSectorResponse(
                        "technology",
                        "기술",
                        List.of(new MarketHeatmapStockResponse(
                                "NVDA",
                                "NVIDIA",
                                25.0,
                                2.41
                        ))
                )
        ));

        mockMvc.perform(get("/api/market/heatmap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sectorId").value("technology"))
                .andExpect(jsonPath("$[0].sectorName").value("기술"))
                .andExpect(jsonPath("$[0].stocks[0].ticker").value("NVDA"))
                .andExpect(jsonPath("$[0].stocks[0].companyName").value("NVIDIA"))
                .andExpect(jsonPath("$[0].stocks[0].marketCapWeight").value(25.0))
                .andExpect(jsonPath("$[0].stocks[0].changePercent").value(2.41));

        then(marketHeatmapService).should().getHeatmap();
    }

    @Test
    void returnsServiceUnavailableWithoutLeakingProviderDetails() throws Exception {
        given(marketHeatmapService.getHeatmap())
                .willThrow(new MarketHeatmapUnavailableException());

        mockMvc.perform(get("/api/market/heatmap"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("MARKET_HEATMAP_UNAVAILABLE"))
                .andExpect(jsonPath("$.message")
                        .value("Market heatmap is temporarily unavailable"));
    }
}
