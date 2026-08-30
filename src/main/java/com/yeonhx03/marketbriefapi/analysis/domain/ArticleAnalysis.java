package com.yeonhx03.marketbriefapi.analysis.domain;

import com.yeonhx03.marketbriefapi.article.domain.Article;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "article_analyses")
public class ArticleAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(name = "analysis_type", nullable = false, length = 64)
    private String analysisType;

    @Column(name = "analyzer_name", nullable = false, length = 255)
    private String analyzerName;

    @Column(name = "analyzer_version", nullable = false, length = 255)
    private String analyzerVersion;

    @Column(name = "analyzed_at", nullable = false)
    private OffsetDateTime analyzedAt;

    @Column(name = "text_sentiment", nullable = false, length = 16)
    private String textSentiment;

    @Column(name = "positive_score", nullable = false)
    private Double positiveScore;

    @Column(name = "neutral_score", nullable = false)
    private Double neutralScore;

    @Column(name = "negative_score", nullable = false)
    private Double negativeScore;

    @Column(nullable = false)
    private Double confidence;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected ArticleAnalysis() {
    }

    public ArticleAnalysis(
            Article article,
            String analysisType,
            String analyzerName,
            String analyzerVersion,
            OffsetDateTime analyzedAt,
            String textSentiment,
            Double positiveScore,
            Double neutralScore,
            Double negativeScore,
            Double confidence
    ) {
        this.article = article;
        this.analysisType = analysisType;
        this.analyzerName = analyzerName;
        this.analyzerVersion = analyzerVersion;
        this.analyzedAt = analyzedAt;
        this.textSentiment = textSentiment;
        this.positiveScore = positiveScore;
        this.neutralScore = neutralScore;
        this.negativeScore = negativeScore;
        this.confidence = confidence;
    }

    public Long getId() {
        return id;
    }

    public Article getArticle() {
        return article;
    }

    public String getAnalysisType() {
        return analysisType;
    }

    public String getAnalyzerName() {
        return analyzerName;
    }

    public String getAnalyzerVersion() {
        return analyzerVersion;
    }

    public OffsetDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public String getTextSentiment() {
        return textSentiment;
    }

    public Double getPositiveScore() {
        return positiveScore;
    }

    public Double getNeutralScore() {
        return neutralScore;
    }

    public Double getNegativeScore() {
        return negativeScore;
    }

    public Double getConfidence() {
        return confidence;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
