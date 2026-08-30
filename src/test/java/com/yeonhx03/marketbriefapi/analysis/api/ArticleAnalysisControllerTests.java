package com.yeonhx03.marketbriefapi.analysis.api;

import com.yeonhx03.marketbriefapi.analysis.application.ArticleAnalysisService;
import com.yeonhx03.marketbriefapi.analysis.application.ArticleNotFoundException;
import com.yeonhx03.marketbriefapi.analysis.application.DuplicateArticleAnalysisException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleAnalysisController.class)
class ArticleAnalysisControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArticleAnalysisService analysisService;

    @Test
    void createsArticleAnalysis() throws Exception {
        given(analysisService.create(any(Long.class), any(CreateArticleAnalysisRequest.class)))
                .willReturn(response());

        mockMvc.perform(post("/api/articles/42/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.analysisId").value(7))
                .andExpect(jsonPath("$.articleId").value(42))
                .andExpect(jsonPath("$.analysisType").value("text_sentiment"))
                .andExpect(jsonPath("$.textSentiment").value("neutral"))
                .andExpect(jsonPath("$.positiveScore").value(0.08))
                .andExpect(jsonPath("$.neutralScore").value(0.89))
                .andExpect(jsonPath("$.negativeScore").value(0.03));
    }

    @Test
    void listsArticleAnalyses() throws Exception {
        given(analysisService.findByArticleId(42L)).willReturn(List.of(response()));

        mockMvc.perform(get("/api/articles/42/analyses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].analysisId").value(7))
                .andExpect(jsonPath("$[0].analyzerName").value("ProsusAI/finbert"));
    }

    @Test
    void rejectsInvalidScoreSum() throws Exception {
        mockMvc.perform(post("/api/articles/42/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "analysisType": "text_sentiment",
                                  "analyzerName": "ProsusAI/finbert",
                                  "analyzerVersion": "revision-1",
                                  "analyzedAt": "2026-08-30T09:00:00Z",
                                  "textSentiment": "neutral",
                                  "positiveScore": 0.1,
                                  "neutralScore": 0.7,
                                  "negativeScore": 0.1,
                                  "confidence": 0.7
                                }
                                """))
                .andExpect(status().isBadRequest());

        then(analysisService).shouldHaveNoInteractions();
    }

    @Test
    void returnsConflictForDuplicateAnalysis() throws Exception {
        given(analysisService.create(any(Long.class), any(CreateArticleAnalysisRequest.class)))
                .willThrow(new DuplicateArticleAnalysisException(7L));

        mockMvc.perform(post("/api/articles/42/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ARTICLE_ANALYSIS_DUPLICATE"))
                .andExpect(jsonPath("$.existingAnalysisId").value(7));
    }

    @Test
    void returnsNotFoundForMissingArticle() throws Exception {
        given(analysisService.findByArticleId(999L))
                .willThrow(new ArticleNotFoundException(999L));

        mockMvc.perform(get("/api/articles/999/analyses"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ARTICLE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Article not found: 999"));
    }

    private ArticleAnalysisResponse response() {
        return new ArticleAnalysisResponse(
                7L,
                42L,
                "text_sentiment",
                "ProsusAI/finbert",
                "revision-1",
                OffsetDateTime.parse("2026-08-30T09:00:00Z"),
                "neutral",
                0.08,
                0.89,
                0.03,
                0.89
        );
    }

    private String validJson() {
        return """
                {
                  "analysisType": "text_sentiment",
                  "analyzerName": "ProsusAI/finbert",
                  "analyzerVersion": "revision-1",
                  "analyzedAt": "2026-08-30T09:00:00Z",
                  "textSentiment": "neutral",
                  "positiveScore": 0.08,
                  "neutralScore": 0.89,
                  "negativeScore": 0.03,
                  "confidence": 0.89
                }
                """;
    }
}
