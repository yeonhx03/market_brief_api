package com.yeonhx03.marketbriefapi.analysis.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateArticleAnalysisRequest(
        @NotBlank
        @Size(max = 64)
        String analysisType,

        @NotBlank
        @Size(max = 255)
        String analyzerName,

        @NotBlank
        @Size(max = 255)
        String analyzerVersion,

        @NotNull
        OffsetDateTime analyzedAt,

        @NotBlank
        @Pattern(regexp = "positive|neutral|negative")
        String textSentiment,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        Double positiveScore,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        Double neutralScore,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        Double negativeScore,

        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        Double confidence
) {

    @AssertTrue(message = "sentiment scores must sum to 1")
    @JsonIgnore
    public boolean isScoreSumValid() {
        if (positiveScore == null || neutralScore == null || negativeScore == null) {
            return true;
        }

        var sum = positiveScore + neutralScore + negativeScore;
        return Math.abs(sum - 1.0) <= 0.000001;
    }
}
