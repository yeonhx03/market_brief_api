package com.yeonhx03.marketbriefapi.article.api;

import com.yeonhx03.marketbriefapi.article.application.ArticleService;
import com.yeonhx03.marketbriefapi.article.application.DuplicateArticleException;
import com.yeonhx03.marketbriefapi.article.application.ArticleNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleController.class)
class ArticleControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArticleService articleService;

    @Test
    void createsArticle() throws Exception {
        given(articleService.create(any(CreateArticleRequest.class)))
                .willReturn(new ArticleResponse(
                        42L,
                        "Reuters",
                        "article-123",
                        "Market closes higher",
                        "https://example.com/articles/123",
                        null,
                        OffsetDateTime.parse("2026-08-30T08:30:00+09:00"),
                        OffsetDateTime.parse("2026-08-30T09:00:00+09:00"),
                        "raw content",
                        "cleaned content",
                        "a".repeat(64)
                ));

        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "source": "Reuters",
                                  "sourceArticleId": "article-123",
                                  "title": "Market closes higher",
                                  "url": "https://example.com/articles/123",
                                  "publishedAt": "2026-08-30T08:30:00+09:00",
                                  "collectedAt": "2026-08-30T09:00:00+09:00",
                                  "rawContent": "raw content",
                                  "cleanedContent": "cleaned content",
                                  "contentHash": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.source").value("Reuters"))
                .andExpect(jsonPath("$.title").value("Market closes higher"));

        then(articleService).should().create(any(CreateArticleRequest.class));
    }

    @Test
    void rejectsInvalidArticle() throws Exception {
        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "source": " ",
                                  "title": "",
                                  "url": "",
                                  "collectedAt": null
                                }
                                """))
                .andExpect(status().isBadRequest());

        then(articleService).shouldHaveNoInteractions();
    }

    @Test
    void associatesTickerWithArticle() throws Exception {
        mockMvc.perform(post("/api/articles/42/tickers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ticker":"aapl"}
                                """))
                .andExpect(status().isNoContent());

        then(articleService).should().addTicker(42L, "aapl");
    }

    @Test
    void rejectsInvalidArticleTicker() throws Exception {
        mockMvc.perform(post("/api/articles/42/tickers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ticker":"AAPL!"}
                                """))
                .andExpect(status().isBadRequest());

        then(articleService).shouldHaveNoInteractions();
    }

    @Test
    void returnsNotFoundWhenAssociatingUnknownArticle() throws Exception {
        willThrow(new ArticleNotFoundException(42L))
                .given(articleService).addTicker(42L, "AAPL");

        mockMvc.perform(post("/api/articles/42/tickers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ticker":"AAPL"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ARTICLE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Article not found"))
                .andExpect(jsonPath("$.articleId").value(42));
    }

    @Test
    void returnsConflictForDuplicateArticle() throws Exception {
        given(articleService.create(any(CreateArticleRequest.class)))
                .willThrow(new DuplicateArticleException(42L));

        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "source": "Reuters",
                                  "sourceArticleId": "article-123",
                                  "title": "Market closes higher",
                                  "url": "https://example.com/articles/123",
                                  "collectedAt": "2026-08-30T09:00:00Z"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("ARTICLE_DUPLICATE"))
                .andExpect(jsonPath("$.message").value("Article already exists"))
                .andExpect(jsonPath("$.existingArticleId").value(42));
    }

    @Test
    void returnsLatestArticles() throws Exception {
        given(articleService.findLatest(2)).willReturn(List.of(
                new ArticleResponse(
                        42L,
                        "Reuters",
                        "article-123",
                        "Market closes higher",
                        "https://example.com/articles/123",
                        null,
                        OffsetDateTime.parse("2026-08-30T08:30:00Z"),
                        OffsetDateTime.parse("2026-08-30T09:00:00Z"),
                        "raw content",
                        "cleaned content",
                        "a".repeat(64)
                )
        ));

        mockMvc.perform(get("/api/articles/latest").param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(42))
                .andExpect(jsonPath("$[0].source").value("Reuters"));
    }

    @Test
    void usesDefaultLatestArticleLimit() throws Exception {
        given(articleService.findLatest(10)).willReturn(List.of());

        mockMvc.perform(get("/api/articles/latest"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        then(articleService).should().findLatest(10);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 101})
    void rejectsLatestArticleLimitOutsideRange(int limit) throws Exception {
        mockMvc.perform(get("/api/articles/latest")
                        .param("limit", Integer.toString(limit)))
                .andExpect(status().isBadRequest());

        then(articleService).shouldHaveNoInteractions();
    }
}
