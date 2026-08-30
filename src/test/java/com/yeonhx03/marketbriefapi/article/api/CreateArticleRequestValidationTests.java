package com.yeonhx03.marketbriefapi.article.api;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateArticleRequestValidationTests {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void acceptsValidRequest() {
        var request = new CreateArticleRequest(
                "Reuters",
                "article-123",
                "Market closes higher",
                "https://example.com/articles/123",
                null,
                null,
                OffsetDateTime.parse("2026-08-30T09:00:00+09:00"),
                "raw content",
                "cleaned content",
                "a".repeat(64)
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsMissingRequiredFields() {
        var request = new CreateArticleRequest(
                " ",
                null,
                "",
                "\t",
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertEquals(
                Set.of("source", "title", "url", "collectedAt"),
                violatedProperties(request)
        );
    }

    @Test
    void rejectsFieldsThatExceedMaximumLength() {
        var request = new CreateArticleRequest(
                "a".repeat(256),
                "a".repeat(256),
                "a".repeat(1001),
                "a".repeat(2049),
                "a".repeat(2049),
                null,
                OffsetDateTime.parse("2026-08-30T09:00:00+09:00"),
                null,
                null,
                "a".repeat(65)
        );

        assertEquals(
                Set.of(
                        "source",
                        "sourceArticleId",
                        "title",
                        "url",
                        "canonicalUrl",
                        "contentHash"
                ),
                violatedProperties(request)
        );
    }

    private Set<String> violatedProperties(CreateArticleRequest request) {
        return validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
