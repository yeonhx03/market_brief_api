package com.yeonhx03.marketbriefapi.article.application;

import com.yeonhx03.marketbriefapi.article.api.ArticleResponse;
import com.yeonhx03.marketbriefapi.article.api.CreateArticleRequest;
import com.yeonhx03.marketbriefapi.article.domain.Article;
import com.yeonhx03.marketbriefapi.article.persistence.ArticleRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
class JpaArticleService implements ArticleService {

    private final ArticleRepository articleRepository;

    JpaArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Override
    @Transactional
    public void addTicker(Long articleId, String ticker) {
        var article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException(articleId));
        article.addTicker(ticker.toUpperCase(Locale.ROOT));
    }

    @Override
    @Transactional
    public ArticleResponse create(CreateArticleRequest request) {
        var sourceArticleId = nullIfBlank(request.sourceArticleId());
        var canonicalUrl = nullIfBlank(request.canonicalUrl());
        var contentHash = nullIfBlank(request.contentHash());

        rejectDuplicate(
                request.source(),
                sourceArticleId,
                canonicalUrl,
                contentHash,
                request.url()
        );

        var article = new Article(
                request.source(),
                sourceArticleId,
                request.title(),
                request.url(),
                canonicalUrl,
                request.publishedAt(),
                request.collectedAt(),
                request.rawContent(),
                request.cleanedContent(),
                contentHash
        );

        var savedArticle = articleRepository.save(article);
        return toResponse(savedArticle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleResponse> findLatest(int limit) {
        return articleRepository
                .findAllByOrderByCollectedAtDescIdDesc(PageRequest.of(0, limit))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void rejectDuplicate(
            String source,
            String sourceArticleId,
            String canonicalUrl,
            String contentHash,
            String url
    ) {
        if (sourceArticleId != null) {
            articleRepository
                    .findBySourceAndSourceArticleId(source, sourceArticleId)
                    .ifPresent(this::throwDuplicate);
        }

        if (canonicalUrl != null) {
            articleRepository
                    .findByCanonicalUrl(canonicalUrl)
                    .ifPresent(this::throwDuplicate);
        }

        if (contentHash != null) {
            articleRepository
                    .findByContentHash(contentHash)
                    .ifPresent(this::throwDuplicate);
        }

        articleRepository
                .findByUrl(url)
                .ifPresent(this::throwDuplicate);
    }

    private void throwDuplicate(Article article) {
        throw new DuplicateArticleException(article.getId());
    }

    private String nullIfBlank(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private ArticleResponse toResponse(Article article) {
        return new ArticleResponse(
                article.getId(),
                article.getSource(),
                article.getSourceArticleId(),
                article.getTitle(),
                article.getUrl(),
                article.getCanonicalUrl(),
                article.getPublishedAt(),
                article.getCollectedAt(),
                article.getRawContent(),
                article.getCleanedContent(),
                article.getContentHash()
        );
    }
}
