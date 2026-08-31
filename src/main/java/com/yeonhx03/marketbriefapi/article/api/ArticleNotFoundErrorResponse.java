package com.yeonhx03.marketbriefapi.article.api;

public record ArticleNotFoundErrorResponse(
        String code,
        String message,
        Long articleId
) {
}
