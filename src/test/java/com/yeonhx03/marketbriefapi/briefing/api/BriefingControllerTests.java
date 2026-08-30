package com.yeonhx03.marketbriefapi.briefing.api;

import com.yeonhx03.marketbriefapi.briefing.application.BriefingNotFoundException;
import com.yeonhx03.marketbriefapi.briefing.application.BriefingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BriefingController.class)
class BriefingControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BriefingService briefingService;

    @Test
    void createsBriefing() throws Exception {
        given(briefingService.create(any(CreateBriefingRequest.class)))
                .willReturn(response());

        mockMvc.perform(post("/api/briefings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.briefingId").value(9))
                .andExpect(jsonPath("$.schemaVersion").value(1))
                .andExpect(jsonPath("$.briefingType").value("text_sentiment"))
                .andExpect(jsonPath("$.summary.articleCount").value(1))
                .andExpect(jsonPath("$.items[0].title").value("시장 상승"));
    }

    @Test
    void returnsLatestBriefing() throws Exception {
        given(briefingService.findLatest()).willReturn(response());

        mockMvc.perform(get("/api/briefings/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.briefingId").value(9))
                .andExpect(jsonPath("$.analysisSelector.analyzerName")
                        .value("ProsusAI/finbert"));
    }

    @Test
    void rejectsInvalidBriefingShape() throws Exception {
        mockMvc.perform(post("/api/briefings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "schemaVersion": 1,
                                  "briefingType": "text_sentiment",
                                  "analysisSelector": {},
                                  "summary": {},
                                  "items": {}
                                }
                                """))
                .andExpect(status().isBadRequest());

        then(briefingService).shouldHaveNoInteractions();
    }

    @Test
    void returnsNotFoundWhenNoBriefingExists() throws Exception {
        given(briefingService.findLatest()).willThrow(new BriefingNotFoundException());

        mockMvc.perform(get("/api/briefings/latest"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BRIEFING_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Briefing not found"));
    }

    private BriefingResponse response() throws Exception {
        return new BriefingResponse(
                9L,
                OffsetDateTime.parse("2026-08-30T09:00:00Z"),
                1,
                "text_sentiment",
                objectMapper.readTree("""
                        {
                          "analysisType": "text_sentiment",
                          "analyzerName": "ProsusAI/finbert",
                          "analyzerVersion": "revision-1"
                        }
                        """),
                objectMapper.readTree("""
                        {"articleCount": 1}
                        """),
                objectMapper.readTree("""
                        [{"articleId": 42, "title": "시장 상승"}]
                        """)
        );
    }

    private String validJson() {
        return """
                {
                  "schemaVersion": 1,
                  "briefingType": "text_sentiment",
                  "analysisSelector": {
                    "analysisType": "text_sentiment",
                    "analyzerName": "ProsusAI/finbert",
                    "analyzerVersion": "revision-1"
                  },
                  "summary": {
                    "articleCount": 1,
                    "analyzedCount": 1,
                    "missingAnalysisCount": 0
                  },
                  "items": [
                    {"articleId": 42, "title": "시장 상승"}
                  ]
                }
                """;
    }
}
