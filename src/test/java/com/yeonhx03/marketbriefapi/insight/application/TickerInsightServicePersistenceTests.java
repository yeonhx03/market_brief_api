package com.yeonhx03.marketbriefapi.insight.application;

import com.yeonhx03.marketbriefapi.analysis.api.CreateArticleAnalysisRequest;
import com.yeonhx03.marketbriefapi.analysis.application.ArticleAnalysisService;
import com.yeonhx03.marketbriefapi.article.api.CreateArticleRequest;
import com.yeonhx03.marketbriefapi.article.application.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest
@Transactional
class TickerInsightServicePersistenceTests {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleAnalysisService articleAnalysisService;

    @Autowired
    private TickerInsightService tickerInsightService;

    @Test
    void returnsOnlyLatestTextSentimentForTickerArticles() {
        var olderArticleId = createArticle(
                "Reuters",
                "Older Apple article",
                "older-apple",
                "2026-08-30T12:00:00Z"
        );
        var newerArticleId = createArticle(
                "Bloomberg",
                "Newer Apple article",
                "newer-apple",
                "2026-08-31T12:00:00Z"
        );
        var unrelatedArticleId = createArticle(
                "Reuters",
                "Microsoft article",
                "microsoft",
                "2026-08-31T13:00:00Z"
        );
        articleService.addTicker(olderArticleId, "aapl");
        articleService.addTicker(olderArticleId, "AAPL");
        articleService.addTicker(newerArticleId, "AAPL");
        articleService.addTicker(unrelatedArticleId, "MSFT");

        createAnalysis(
                olderArticleId,
                "finbert-v1",
                "2026-08-30T13:00:00Z",
                0.10,
                0.20,
                0.70
        );
        createAnalysis(
                olderArticleId,
                "finbert-v2",
                "2026-08-31T13:00:00Z",
                0.60,
                0.30,
                0.10
        );
        createAnalysis(
                newerArticleId,
                "finbert-v2",
                "2026-08-31T14:00:00Z",
                0.20,
                0.50,
                0.30
        );
        createAnalysis(
                unrelatedArticleId,
                "finbert-v2",
                "2026-08-31T14:30:00Z",
                0.90,
                0.05,
                0.05
        );

        var insight = tickerInsightService.getInsight("aapl", 10);

        assertThat(insight.ticker()).isEqualTo("AAPL");
        assertThat(insight.articleCount()).isEqualTo(2);
        assertThat(insight.sourceCount()).isEqualTo(2);
        assertThat(insight.dataAsOf())
                .isEqualTo(OffsetDateTime.parse("2026-08-31T14:00:00Z"));
        assertThat(insight.sentiment().positive())
                .isCloseTo(0.40, within(0.000001));
        assertThat(insight.sentiment().neutral())
                .isCloseTo(0.40, within(0.000001));
        assertThat(insight.sentiment().negative())
                .isCloseTo(0.20, within(0.000001));
        assertThat(insight.articles())
                .extracting("articleId", "title", "source")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(
                                newerArticleId,
                                "Newer Apple article",
                                "Bloomberg"
                        ),
                        org.assertj.core.groups.Tuple.tuple(
                                olderArticleId,
                                "Older Apple article",
                                "Reuters"
                        )
                );
    }

    @Test
    void reportsNotFoundWhenTickerHasNoAnalyzedArticles() {
        var articleId = createArticle(
                "Reuters",
                "Apple article without analysis",
                "apple-without-analysis",
                "2026-08-31T12:00:00Z"
        );
        articleService.addTicker(articleId, "AAPL");

        assertThatThrownBy(() -> tickerInsightService.getInsight("AAPL", 10))
                .isInstanceOf(TickerInsightNotFoundException.class);
    }

    private Long createArticle(
            String source,
            String title,
            String slug,
            String publishedAt
    ) {
        return articleService.create(new CreateArticleRequest(
                source,
                slug,
                title,
                "https://example.com/" + slug,
                "https://canonical.example.com/" + slug,
                OffsetDateTime.parse(publishedAt),
                OffsetDateTime.parse(publishedAt).plusMinutes(5),
                "raw content",
                "cleaned content",
                null
        )).id();
    }

    private void createAnalysis(
            Long articleId,
            String analyzerVersion,
            String analyzedAt,
            double positive,
            double neutral,
            double negative
    ) {
        articleAnalysisService.create(
                articleId,
                new CreateArticleAnalysisRequest(
                        "text_sentiment",
                        "ProsusAI/finbert",
                        analyzerVersion,
                        OffsetDateTime.parse(analyzedAt),
                        "neutral",
                        positive,
                        neutral,
                        negative,
                        Math.max(positive, Math.max(neutral, negative))
                )
        );
    }
}
