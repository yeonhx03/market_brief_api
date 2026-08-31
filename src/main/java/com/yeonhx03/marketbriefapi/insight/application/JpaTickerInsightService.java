package com.yeonhx03.marketbriefapi.insight.application;

import com.yeonhx03.marketbriefapi.analysis.persistence.ArticleAnalysisRepository;
import com.yeonhx03.marketbriefapi.insight.api.TickerInsightNewsResponse;
import com.yeonhx03.marketbriefapi.insight.api.TickerInsightResponse;
import com.yeonhx03.marketbriefapi.insight.api.TickerSentimentResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
class JpaTickerInsightService implements TickerInsightService {

    private static final String TEXT_SENTIMENT = "text_sentiment";

    private final ArticleAnalysisRepository articleAnalysisRepository;

    JpaTickerInsightService(ArticleAnalysisRepository articleAnalysisRepository) {
        this.articleAnalysisRepository = articleAnalysisRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public TickerInsightResponse getInsight(String ticker, int limit) {
        var normalizedTicker = ticker.toUpperCase(Locale.ROOT);
        var analyses = articleAnalysisRepository.findLatestByTickerAndAnalysisType(
                normalizedTicker,
                TEXT_SENTIMENT,
                PageRequest.of(0, limit)
        );

        if (analyses.isEmpty()) {
            throw new TickerInsightNotFoundException();
        }

        var articleCount = analyses.size();
        var positive = analyses.stream()
                .mapToDouble(analysis -> analysis.getPositiveScore())
                .average()
                .orElseThrow();
        var neutral = analyses.stream()
                .mapToDouble(analysis -> analysis.getNeutralScore())
                .average()
                .orElseThrow();
        var negative = analyses.stream()
                .mapToDouble(analysis -> analysis.getNegativeScore())
                .average()
                .orElseThrow();
        var dataAsOf = analyses.stream()
                .map(analysis -> analysis.getAnalyzedAt())
                .max(java.time.OffsetDateTime::compareTo)
                .orElseThrow();
        var articles = analyses.stream()
                .map(analysis -> {
                    var article = analysis.getArticle();
                    var publishedAt = article.getPublishedAt() == null
                            ? article.getCollectedAt()
                            : article.getPublishedAt();
                    var url = article.getCanonicalUrl() == null
                            || article.getCanonicalUrl().isBlank()
                            ? article.getUrl()
                            : article.getCanonicalUrl();
                    return new TickerInsightNewsResponse(
                            article.getId(),
                            article.getTitle(),
                            article.getSource(),
                            publishedAt,
                            url
                    );
                })
                .toList();
        var sourceCount = (int) articles.stream()
                .map(TickerInsightNewsResponse::source)
                .distinct()
                .count();

        return new TickerInsightResponse(
                normalizedTicker,
                articleCount,
                sourceCount,
                dataAsOf,
                new TickerSentimentResponse(positive, neutral, negative),
                articles
        );
    }
}
