package com.yeonhx03.marketbriefapi.briefing.api;

import tools.jackson.databind.JsonNode;

import java.time.OffsetDateTime;

public record BriefingResponse(
        Long briefingId,
        OffsetDateTime createdAt,
        Integer schemaVersion,
        String briefingType,
        JsonNode analysisSelector,
        JsonNode summary,
        JsonNode items
) {
}
