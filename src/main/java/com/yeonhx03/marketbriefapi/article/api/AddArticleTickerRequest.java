package com.yeonhx03.marketbriefapi.article.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddArticleTickerRequest(
        @NotBlank
        @Pattern(regexp = "[A-Za-z][A-Za-z0-9.-]{0,15}")
        String ticker
) {
}
