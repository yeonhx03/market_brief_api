package com.yeonhx03.marketbriefapi.briefing.application;

import com.yeonhx03.marketbriefapi.briefing.api.CreateBriefingRequest;
import com.yeonhx03.marketbriefapi.briefing.persistence.BriefingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class BriefingServicePersistenceTests {

    @Autowired
    private BriefingService briefingService;

    @Autowired
    private BriefingRepository briefingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void persistsAndReturnsLatestBriefing() throws Exception {
        var first = briefingService.create(request("revision-1", "첫 번째 브리핑"));
        var second = briefingService.create(request("revision-2", "두 번째 브리핑"));

        assertThat(first.briefingId()).isNotNull();
        assertThat(second.briefingId()).isNotNull();
        assertThat(briefingRepository.count()).isEqualTo(2);

        var latest = briefingService.findLatest();
        assertThat(latest.briefingId()).isEqualTo(second.briefingId());
        assertThat(latest.analysisSelector().get("analyzerVersion").stringValue())
                .isEqualTo("revision-2");
        assertThat(latest.items().get(0).get("title").stringValue())
                .isEqualTo("두 번째 브리핑");
        assertThat(latest.createdAt()).isNotNull();
    }

    @Test
    void preservesCompleteJsonPayload() throws Exception {
        var saved = briefingService.create(request("revision-1", "한국어 제목"));

        var storedPayload = briefingRepository
                .findById(saved.briefingId())
                .orElseThrow()
                .getPayload();

        var payload = objectMapper.readTree(storedPayload);
        assertThat(payload.get("summary").get("labelCounts").get("neutral").asInt())
                .isEqualTo(1);
        assertThat(payload.get("items").get(0).get("title").stringValue())
                .isEqualTo("한국어 제목");
    }

    @Test
    void rejectsLatestLookupWhenNoBriefingExists() {
        assertThatThrownBy(briefingService::findLatest)
                .isInstanceOf(BriefingNotFoundException.class)
                .hasMessage("Briefing not found");
    }

    private CreateBriefingRequest request(
            String analyzerVersion,
            String title
    ) throws Exception {
        return new CreateBriefingRequest(
                1,
                "text_sentiment",
                objectMapper.readTree("""
                        {
                          "analysisType": "text_sentiment",
                          "analyzerName": "ProsusAI/finbert",
                          "analyzerVersion": "%s"
                        }
                        """.formatted(analyzerVersion)),
                objectMapper.readTree("""
                        {
                          "articleCount": 1,
                          "analyzedCount": 1,
                          "missingAnalysisCount": 0,
                          "labelCounts": {
                            "positive": 0,
                            "neutral": 1,
                            "negative": 0
                          }
                        }
                        """),
                objectMapper.readTree("""
                        [
                          {
                            "articleId": 42,
                            "title": "%s",
                            "analysisStatus": "available"
                          }
                        ]
                        """.formatted(title))
        );
    }
}
