package com.yeonhx03.marketbriefapi.article.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateArticleRequest(
        @NotBlank
        @Size(max = 255)
        String source,

        @Size(max = 255)
        String sourceArticleId,

        @NotBlank
        @Size(max = 1000)
        String title,

        @NotBlank
        @Size(max = 2048)
        String url,

        @Size(max = 2048)
        String canonicalUrl,

        OffsetDateTime publishedAt,

        @NotNull
        OffsetDateTime collectedAt,

        String rawContent,

        String cleanedContent,

        @Size(max = 64)
        String contentHash
) {
}