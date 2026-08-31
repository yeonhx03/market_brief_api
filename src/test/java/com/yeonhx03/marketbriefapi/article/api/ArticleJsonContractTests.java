package com.yeonhx03.marketbriefapi.article.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ArticleJsonContractTests {

    @Autowired
    private JacksonTester<CreateArticleRequest> requestJson;

    @Autowired
    private JacksonTester<ArticleResponse> responseJson;

    @Test
    void deserializesCreateArticleRequest() throws Exception {
        var json = """
                {
                  "source": "Reuters",
                  "sourceArticleId": "article-123",
                  "title": "Market closes higher",
                  "url": "https://example.com/articles/123",
                  "canonicalUrl": "https://example.com/articles/123",
                  "publishedAt": "2026-08-30T08:30:00+09:00",
                  "collectedAt": "2026-08-30T09:00:00+09:00",
                  "rawContent": "raw content",
                  "cleanedContent": "cleaned content",
                  "contentHash": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                }
                """;

        var request = requestJson.parseObject(json);

        assertThat(request).isEqualTo(new CreateArticleRequest(
                "Reuters",
                "article-123",
                "Market closes higher",
                "https://example.com/articles/123",
                "https://example.com/articles/123",
                OffsetDateTime.parse("2026-08-29T23:30:00Z"),
                OffsetDateTime.parse("2026-08-30T00:00:00Z"),
                "raw content",
                "cleaned content",
                "a".repeat(64)
        ));
    }

    @Test
    void serializesArticleResponse() throws Exception {
        var response = new ArticleResponse(
                42L,
                "Reuters",
                "article-123",
                "Market closes higher",
                "https://example.com/articles/123",
                "https://example.com/articles/123",
                OffsetDateTime.parse("2026-08-30T08:30:00+09:00"),
                OffsetDateTime.parse("2026-08-30T09:00:00+09:00"),
                "raw content",
                "cleaned content",
                "a".repeat(64)
        );

        var expectedJson = """
                {
                  "id": 42,
                  "source": "Reuters",
                  "sourceArticleId": "article-123",
                  "title": "Market closes higher",
                  "url": "https://example.com/articles/123",
                  "canonicalUrl": "https://example.com/articles/123",
                  "publishedAt": "2026-08-30T08:30:00+09:00",
                  "collectedAt": "2026-08-30T09:00:00+09:00",
                  "rawContent": "raw content",
                  "cleanedContent": "cleaned content",
                  "contentHash": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                }
                """;

        assertThat(responseJson.write(response)).isEqualToJson(expectedJson);
    }
}
