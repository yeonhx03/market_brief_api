package com.yeonhx03.marketbriefapi.analysis.application;

import com.yeonhx03.marketbriefapi.analysis.api.CreateArticleAnalysisRequest;
import com.yeonhx03.marketbriefapi.analysis.persistence.ArticleAnalysisRepository;
import com.yeonhx03.marketbriefapi.article.api.CreateArticleRequest;
import com.yeonhx03.marketbriefapi.article.application.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ArticleAnalysisServicePersistenceTests {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleAnalysisService analysisService;

    @Autowired
    private ArticleAnalysisRepository analysisRepository;

    @Test
    void persistsAndListsAnalysesNewestFirst() {
        var articleId = createArticle();

        var older = analysisService.create(
                articleId,
                analysisRequest("revision-1", "2026-08-30T09:00:00Z")
        );
        var newer = analysisService.create(
                articleId,
                analysisRequest("revision-2", "2026-08-30T10:00:00Z")
        );

        assertThat(older.analysisId()).isNotNull();
        assertThat(newer.analysisId()).isNotNull();
        assertThat(analysisRepository.count()).isEqualTo(2);
        assertThat(analysisService.findByArticleId(articleId))
                .extracting(analysis -> analysis.analyzerVersion())
                .containsExactly("revision-2", "revision-1");
    }

    @Test
    void rejectsSameAnalysisIdentity() {
        var articleId = createArticle();
        var first = analysisService.create(
                articleId,
                analysisRequest("revision-1", "2026-08-30T09:00:00Z")
        );

        assertThatThrownBy(() -> analysisService.create(
                articleId,
                analysisRequest("revision-1", "2026-08-30T10:00:00Z")
        ))
                .isInstanceOf(DuplicateArticleAnalysisException.class)
                .hasFieldOrPropertyWithValue("existingAnalysisId", first.analysisId());

        assertThat(analysisRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectsAnalysisForMissingArticle() {
        assertThatThrownBy(() -> analysisService.create(
                999L,
                analysisRequest("revision-1", "2026-08-30T09:00:00Z")
        ))
                .isInstanceOf(ArticleNotFoundException.class)
                .hasMessage("Article not found: 999");
    }

    private Long createArticle() {
        var article = articleService.create(new CreateArticleRequest(
                "Reuters",
                "analysis-test-article",
                "Market closes higher",
                "https://example.com/articles/analysis-test",
                null,
                OffsetDateTime.parse("2026-08-30T08:30:00Z"),
                OffsetDateTime.parse("2026-08-30T09:00:00Z"),
                "raw content",
                "cleaned content",
                "b".repeat(64)
        ));
        return article.id();
    }

    private CreateArticleAnalysisRequest analysisRequest(
            String analyzerVersion,
            String analyzedAt
    ) {
        return new CreateArticleAnalysisRequest(
                "text_sentiment",
                "ProsusAI/finbert",
                analyzerVersion,
                OffsetDateTime.parse(analyzedAt),
                "neutral",
                0.08,
                0.89,
                0.03,
                0.89
        );
    }
}
