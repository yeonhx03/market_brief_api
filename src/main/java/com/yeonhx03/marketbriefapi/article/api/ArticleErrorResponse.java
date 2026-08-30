package com.yeonhx03.marketbriefapi.article.api;

public record ArticleErrorResponse(
        String code,
        String message,
        Long existingArticleId
) {
}
