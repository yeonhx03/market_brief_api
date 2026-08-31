package com.yeonhx03.marketbriefapi.market.api;

import com.yeonhx03.marketbriefapi.market.application.MarketStatusService;
import com.yeonhx03.marketbriefapi.market.application.MarketStatusUnavailableException;
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

@WebMvcTest(MarketStatusController.class)
class MarketStatusControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MarketStatusService marketStatusService;

    @Test
    void returnsMarketClocks() throws Exception {
        given(marketStatusService.getStatus()).willReturn(List.of(
                new MarketClockResponse(
                        "서울", "Asia/Seoul", "09:30", "정규장"
                ),
                new MarketClockResponse(
                        "뉴욕", "America/New_York", "20:30", "장 마감"
                )
        ));

        mockMvc.perform(get("/api/market/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value("서울"))
                .andExpect(jsonPath("$[0].timeZone").value("Asia/Seoul"))
                .andExpect(jsonPath("$[0].displayTime").value("09:30"))
                .andExpect(jsonPath("$[0].status").value("정규장"))
                .andExpect(jsonPath("$[1].city").value("뉴욕"))
                .andExpect(jsonPath("$[1].status").value("장 마감"));

        then(marketStatusService).should().getStatus();
    }

    @Test
    void returnsServiceUnavailableWithoutLeakingProviderDetails() throws Exception {
        given(marketStatusService.getStatus())
                .willThrow(new MarketStatusUnavailableException());

        mockMvc.perform(get("/api/market/status"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("MARKET_STATUS_UNAVAILABLE"))
                .andExpect(jsonPath("$.message")
                        .value("Market status is temporarily unavailable"));
    }
}
