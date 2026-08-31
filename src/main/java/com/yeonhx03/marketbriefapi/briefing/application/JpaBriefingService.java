package com.yeonhx03.marketbriefapi.briefing.application;

import com.yeonhx03.marketbriefapi.briefing.api.BriefingResponse;
import com.yeonhx03.marketbriefapi.briefing.api.CreateBriefingRequest;
import com.yeonhx03.marketbriefapi.briefing.domain.Briefing;
import com.yeonhx03.marketbriefapi.briefing.persistence.BriefingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
class JpaBriefingService implements BriefingService {

    private final BriefingRepository briefingRepository;
    private final ObjectMapper objectMapper;

    JpaBriefingService(
            BriefingRepository briefingRepository,
            ObjectMapper objectMapper
    ) {
        this.briefingRepository = briefingRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public BriefingResponse create(CreateBriefingRequest request) {
        var briefing = new Briefing(
                request.schemaVersion(),
                request.briefingType(),
                writePayload(request)
        );

        return toResponse(briefingRepository.save(briefing));
    }

    @Override
    @Transactional(readOnly = true)
    public BriefingResponse findLatest() {
        return briefingRepository
                .findFirstByOrderByCreatedAtDescIdDesc()
                .map(this::toResponse)
                .orElseThrow(BriefingNotFoundException::new);
    }

    private String writePayload(CreateBriefingRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to serialize briefing payload", exception);
        }
    }

    private BriefingResponse toResponse(Briefing briefing) {
        var payload = readPayload(briefing.getPayload());

        return new BriefingResponse(
                briefing.getId(),
                briefing.getCreatedAt(),
                briefing.getSchemaVersion(),
                briefing.getBriefingType(),
                payload.get("analysisSelector"),
                payload.get("summary"),
                payload.get("items")
        );
    }

    private JsonNode readPayload(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to read briefing payload", exception);
        }
    }
}
