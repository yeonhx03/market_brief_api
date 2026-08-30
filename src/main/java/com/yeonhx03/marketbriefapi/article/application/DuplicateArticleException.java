package com.yeonhx03.marketbriefapi.article.application;

public class DuplicateArticleException extends RuntimeException {

    private final Long existingArticleId;

    public DuplicateArticleException(Long existingArticleId) {
        super("Article already exists");
        this.existingArticleId = existingArticleId;
    }

    public Long getExistingArticleId() {
        return existingArticleId;
    }
}
