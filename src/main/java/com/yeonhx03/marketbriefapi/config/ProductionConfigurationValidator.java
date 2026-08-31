package com.yeonhx03.marketbriefapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
public class ProductionConfigurationValidator {

    public ProductionConfigurationValidator(
            @Value("${market-brief.write-api-key}") String writeApiKey,
            @Value("${market-brief.cors.allowed-origins}") String allowedOrigins,
            @Value("${market-brief.market-data.finnhub.api-key}") String finnhubApiKey,
            @Value("${market-brief.market-data.twelve-data.api-key}")
            String twelveDataApiKey,
            @Value("${market-brief.market-data.fmp.api-key}") String fmpApiKey
    ) {
        requireNonBlank(writeApiKey, "WRITE_API_KEY");
        requireNonBlank(allowedOrigins, "WEB_ORIGINS");
        requireNonBlank(finnhubApiKey, "FINNHUB_API_KEY");
        requireNonBlank(twelveDataApiKey, "TWELVE_DATA_API_KEY");
        requireNonBlank(fmpApiKey, "FMP_API_KEY");
    }

    private void requireNonBlank(String value, String environmentVariable) {
        if (value.isBlank()) {
            throw new IllegalStateException(
                    environmentVariable + " must not be blank in the prod profile"
            );
        }
    }
}
