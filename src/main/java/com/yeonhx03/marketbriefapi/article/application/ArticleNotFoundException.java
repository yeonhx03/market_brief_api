package com.yeonhx03.marketbriefapi.article.application;

public class ArticleNotFoundException extends RuntimeException {

    private final Long articleId;

    public ArticleNotFoundException(Long articleId) {
        super("Article not found: " + articleId);
        this.articleId = articleId;
    }

    public Long getArticleId() {
        return articleId;
    }
}
