package com.yeonhx03.marketbriefapi.article.application;

import com.yeonhx03.marketbriefapi.article.api.CreateArticleRequest;
import com.yeonhx03.marketbriefapi.article.domain.Article;
import com.yeonhx03.marketbriefapi.article.persistence.ArticleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ArticleServicePersistenceTests {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    void persistsArticle() {
        var request = new CreateArticleRequest(
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
        );

        var response = articleService.create(request);

        assertThat(response.id()).isNotNull();

        var persistedArticle = articleRepository.findById(response.id()).orElseThrow();
        assertThat(persistedArticle.getSource()).isEqualTo("Reuters");
        assertThat(persistedArticle.getSourceArticleId()).isEqualTo("article-123");
        assertThat(persistedArticle.getTitle()).isEqualTo("Market closes higher");
        assertThat(persistedArticle.getCollectedAt())
                .isEqualTo(OffsetDateTime.parse("2026-08-30T09:00:00Z"));
        assertThat(persistedArticle.getCreatedAt()).isNotNull();
    }

    @Test
    void rejectsDuplicateSourceArticleId() {
        var firstResponse = articleService.create(request(
                "article-123",
                null,
                null,
                "https://example.com/articles/123"
        ));

        assertThatThrownBy(() -> articleService.create(request(
                "article-123",
                null,
                null,
                "https://example.com/articles/different"
        )))
                .isInstanceOf(DuplicateArticleException.class)
                .hasFieldOrPropertyWithValue("existingArticleId", firstResponse.id());

        assertThat(articleRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectsDuplicateCanonicalUrl() {
        var firstResponse = articleService.create(request(
                "article-123",
                "https://example.com/articles/canonical",
                null,
                "https://feed.example.com/articles/123"
        ));

        assertThatThrownBy(() -> articleService.create(request(
                "article-456",
                "https://example.com/articles/canonical",
                null,
                "https://feed.example.com/articles/456"
        )))
                .isInstanceOf(DuplicateArticleException.class)
                .hasFieldOrPropertyWithValue("existingArticleId", firstResponse.id());

        assertThat(articleRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectsDuplicateContentHash() {
        var firstResponse = articleService.create(request(
                "article-123",
                null,
                "a".repeat(64),
                "https://example.com/articles/123"
        ));

        assertThatThrownBy(() -> articleService.create(request(
                "article-456",
                null,
                "a".repeat(64),
                "https://example.com/articles/456"
        )))
                .isInstanceOf(DuplicateArticleException.class)
                .hasFieldOrPropertyWithValue("existingArticleId", firstResponse.id());

        assertThat(articleRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectsDuplicateUrlWhenOptionalDuplicateKeysAreMissing() {
        var firstResponse = articleService.create(request(
                null,
                null,
                null,
                "https://example.com/articles/same"
        ));

        assertThatThrownBy(() -> articleService.create(request(
                null,
                null,
                null,
                "https://example.com/articles/same"
        )))
                .isInstanceOf(DuplicateArticleException.class)
                .hasFieldOrPropertyWithValue("existingArticleId", firstResponse.id());

        assertThat(articleRepository.count()).isEqualTo(1);
    }

    @Test
    void databaseUniqueIndexRejectsDuplicateUrl() {
        articleRepository.saveAndFlush(article(
                "https://example.com/articles/database-duplicate"
        ));

        assertThatThrownBy(() -> articleRepository.saveAndFlush(article(
                "https://example.com/articles/database-duplicate"
        )))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void allowsArticlesWithoutDuplicateKeys() {
        var firstResponse = articleService.create(request(
                " ",
                " ",
                " ",
                "https://example.com/articles/123"
        ));
        var secondResponse = articleService.create(request(
                null,
                null,
                null,
                "https://example.com/articles/456"
        ));

        assertThat(firstResponse.id()).isNotEqualTo(secondResponse.id());
        assertThat(firstResponse.sourceArticleId()).isNull();
        assertThat(firstResponse.canonicalUrl()).isNull();
        assertThat(firstResponse.contentHash()).isNull();
        assertThat(articleRepository.count()).isEqualTo(2);
    }

    @Test
    void returnsLatestArticlesWithStableOrderingAndLimit() {
        articleService.create(latestRequest(
                "latest-older",
                "https://example.com/articles/older",
                "2026-08-30T08:00:00Z"
        ));
        var firstAtSameTime = articleService.create(latestRequest(
                "latest-first",
                "https://example.com/articles/first",
                "2026-08-30T09:00:00Z"
        ));
        var secondAtSameTime = articleService.create(latestRequest(
                "latest-second",
                "https://example.com/articles/second",
                "2026-08-30T09:00:00Z"
        ));

        assertThat(articleService.findLatest(2))
                .extracting(article -> article.id())
                .containsExactly(secondAtSameTime.id(), firstAtSameTime.id());
    }

    private CreateArticleRequest request(
            String sourceArticleId,
            String canonicalUrl,
            String contentHash,
            String url
    ) {
        return new CreateArticleRequest(
                "Reuters",
                sourceArticleId,
                "Market closes higher",
                url,
                canonicalUrl,
                OffsetDateTime.parse("2026-08-30T08:30:00Z"),
                OffsetDateTime.parse("2026-08-30T09:00:00Z"),
                "raw content",
                "cleaned content",
                contentHash
        );
    }

    private CreateArticleRequest latestRequest(
            String sourceArticleId,
            String url,
            String collectedAt
    ) {
        return new CreateArticleRequest(
                "Reuters",
                sourceArticleId,
                "Market closes higher",
                url,
                null,
                null,
                OffsetDateTime.parse(collectedAt),
                null,
                null,
                null
        );
    }

    private Article article(String url) {
        return new Article(
                "Reuters",
                null,
                "Market closes higher",
                url,
                null,
                null,
                OffsetDateTime.parse("2026-08-30T09:00:00Z"),
                null,
                null,
                null
        );
    }
}
