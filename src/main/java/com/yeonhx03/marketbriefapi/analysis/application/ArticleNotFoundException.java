package com.yeonhx03.marketbriefapi.analysis.application;

public class ArticleNotFoundException extends RuntimeException {

    public ArticleNotFoundException(Long articleId) {
        super("Article not found: " + articleId);
    }
}
