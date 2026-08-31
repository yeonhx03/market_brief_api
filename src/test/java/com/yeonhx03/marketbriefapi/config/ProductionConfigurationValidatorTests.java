package com.yeonhx03.marketbriefapi.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionConfigurationValidatorTests {

    @Test
    void acceptsRequiredProductionValues() {
        assertThatCode(() -> new ProductionConfigurationValidator(
                "secret",
                "https://brief.example.com",
                "finnhub-secret",
                "twelve-data-secret",
                "fmp-secret"
        )).doesNotThrowAnyException();
    }

    @Test
    void rejectsBlankWriteApiKey() {
        assertThatThrownBy(() -> new ProductionConfigurationValidator(
                " ",
                "https://brief.example.com",
                "finnhub-secret",
                "twelve-data-secret",
                "fmp-secret"
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("WRITE_API_KEY");
    }

    @Test
    void rejectsBlankWebOrigins() {
        assertThatThrownBy(() -> new ProductionConfigurationValidator(
                "secret",
                " ",
                "finnhub-secret",
                "twelve-data-secret",
                "fmp-secret"
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("WEB_ORIGINS");
    }

    @Test
    void rejectsBlankFinnhubApiKey() {
        assertThatThrownBy(() -> new ProductionConfigurationValidator(
                "secret",
                "https://brief.example.com",
                " ",
                "twelve-data-secret",
                "fmp-secret"
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FINNHUB_API_KEY");
    }

    @Test
    void rejectsBlankTwelveDataApiKey() {
        assertThatThrownBy(() -> new ProductionConfigurationValidator(
                "secret",
                "https://brief.example.com",
                "finnhub-secret",
                " ",
                "fmp-secret"
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TWELVE_DATA_API_KEY");
    }

    @Test
    void rejectsBlankFmpApiKey() {
        assertThatThrownBy(() -> new ProductionConfigurationValidator(
                "secret",
                "https://brief.example.com",
                "finnhub-secret",
                "twelve-data-secret",
                " "
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FMP_API_KEY");
    }
}
