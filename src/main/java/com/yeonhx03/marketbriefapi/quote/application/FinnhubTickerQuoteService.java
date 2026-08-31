package com.yeonhx03.marketbriefapi.quote.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yeonhx03.marketbriefapi.config.MarketDataCacheConfiguration;
import com.yeonhx03.marketbriefapi.quote.api.TickerQuoteResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.DateTimeException;
import java.time.Instant;
import java.util.Locale;

@Service
public class FinnhubTickerQuoteService implements TickerQuoteService {

    static final String API_KEY_HEADER = "X-Finnhub-Token";

    private final RestClient restClient;
    private final String apiKey;

    public FinnhubTickerQuoteService(
            RestClient.Builder restClientBuilder,
            @Value("${market-brief.market-data.finnhub.base-url:https://finnhub.io/api/v1}")
            String baseUrl,
            @Value("${market-brief.market-data.finnhub.api-key:}") String apiKey
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    @Override
    @Cacheable(
            cacheNames = MarketDataCacheConfiguration.TICKER_QUOTE_CACHE,
            key = "#p0.toUpperCase()"
    )
    public TickerQuoteResponse getQuote(String ticker) {
        if (apiKey.isBlank()) {
            throw new TickerQuoteUnavailableException();
        }

        var normalizedTicker = ticker.toUpperCase(Locale.ROOT);

        try {
            var quote = requestQuote(normalizedTicker);

            if (quote == null || quote.c() == null || quote.c() <= 0) {
                throw new TickerQuoteNotFoundException();
            }
            if (quote.dp() == null || quote.t() == null || quote.t() <= 0) {
                throw new TickerQuoteUnavailableException();
            }

            var profile = requestProfile(normalizedTicker);
            var companyName = profile == null || isBlank(profile.name())
                    ? normalizedTicker
                    : profile.name().trim();
            var currency = profile == null || isBlank(profile.currency())
                    ? "USD"
                    : profile.currency().trim();

            return new TickerQuoteResponse(
                    normalizedTicker,
                    companyName,
                    quote.c(),
                    quote.dp(),
                    null,
                    currency,
                    Instant.ofEpochSecond(quote.t())
            );
        } catch (RestClientException | DateTimeException exception) {
            throw new TickerQuoteUnavailableException(exception);
        }
    }

    private FinnhubQuoteResponse requestQuote(String ticker) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/quote")
                        .queryParam("symbol", ticker)
                        .build())
                .header(API_KEY_HEADER, apiKey)
                .retrieve()
                .body(FinnhubQuoteResponse.class);
    }

    private FinnhubCompanyProfileResponse requestProfile(String ticker) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stock/profile2")
                        .queryParam("symbol", ticker)
                        .build())
                .header(API_KEY_HEADER, apiKey)
                .retrieve()
                .body(FinnhubCompanyProfileResponse.class);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record FinnhubQuoteResponse(Double c, Double dp, Long t) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record FinnhubCompanyProfileResponse(
            String ticker,
            String name,
            String currency,
            String exchange
    ) {
    }
}
