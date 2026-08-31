package com.yeonhx03.marketbriefapi.briefing.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.JsonNode;

public record CreateBriefingRequest(
        @NotNull
        @Min(1)
        Integer schemaVersion,

        @NotBlank
        @Size(max = 64)
        String briefingType,

        @NotNull
        JsonNode analysisSelector,

        @NotNull
        JsonNode summary,

        @NotNull
        JsonNode items
) {

    @AssertTrue(message = "analysisSelector and summary must be objects, and items must be an array")
    @JsonIgnore
    public boolean isPayloadShapeValid() {
        if (analysisSelector == null || summary == null || items == null) {
            return true;
        }

        return analysisSelector.isObject() && summary.isObject() && items.isArray();
    }
}
