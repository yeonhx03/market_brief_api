package com.yeonhx03.marketbriefapi.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionConfigurationValidatorTests {

    @Test
    void acceptsRequiredProductionValues() {
        assertThatCode(() -> new ProductionConfigurationValidator(
                "secret",
                "https://brief.example.com"
        )).doesNotThrowAnyException();
    }

    @Test
    void rejectsBlankWriteApiKey() {
        assertThatThrownBy(() -> new ProductionConfigurationValidator(
                " ",
                "https://brief.example.com"
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("WRITE_API_KEY");
    }

    @Test
    void rejectsBlankWebOrigins() {
        assertThatThrownBy(() -> new ProductionConfigurationValidator(
                "secret",
                " "
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("WEB_ORIGINS");
    }
}
