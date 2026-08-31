package com.yeonhx03.marketbriefapi.article.domain;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "articles")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String source;

    @Column(name = "source_article_id", length = 255)
    private String sourceArticleId;

    @Column(nullable = false, length = 1000)
    private String title;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(name = "canonical_url", length = 2048)
    private String canonicalUrl;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "collected_at", nullable = false)
    private OffsetDateTime collectedAt;

    @Column(name = "raw_content", columnDefinition = "text")
    private String rawContent;

    @Column(name = "cleaned_content", columnDefinition = "text")
    private String cleanedContent;

    @Column(name = "content_hash", length = 64)
    private String contentHash;

    @ElementCollection
    @CollectionTable(
            name = "article_tickers",
            joinColumns = @JoinColumn(name = "article_id")
    )
    @Column(name = "ticker", nullable = false, length = 16)
    private Set<String> tickers = new LinkedHashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Article() {
    }

    public Article(
            String source,
            String sourceArticleId,
            String title,
            String url,
            String canonicalUrl,
            OffsetDateTime publishedAt,
            OffsetDateTime collectedAt,
            String rawContent,
            String cleanedContent,
            String contentHash
    ) {
        this.source = source;
        this.sourceArticleId = sourceArticleId;
        this.title = title;
        this.url = url;
        this.canonicalUrl = canonicalUrl;
        this.publishedAt = publishedAt;
        this.collectedAt = collectedAt;
        this.rawContent = rawContent;
        this.cleanedContent = cleanedContent;
        this.contentHash = contentHash;
    }

    public Long getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public String getSourceArticleId() {
        return sourceArticleId;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getCanonicalUrl() {
        return canonicalUrl;
    }

    public OffsetDateTime getPublishedAt() {
        return publishedAt;
    }

    public OffsetDateTime getCollectedAt() {
        return collectedAt;
    }

    public String getRawContent() {
        return rawContent;
    }

    public String getCleanedContent() {
        return cleanedContent;
    }

    public String getContentHash() {
        return contentHash;
    }

    public Set<String> getTickers() {
        return Set.copyOf(tickers);
    }

    public void addTicker(String ticker) {
        tickers.add(ticker);
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
