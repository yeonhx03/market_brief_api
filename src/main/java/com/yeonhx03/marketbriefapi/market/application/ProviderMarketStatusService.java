package com.yeonhx03.marketbriefapi.market.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yeonhx03.marketbriefapi.config.MarketDataCacheConfiguration;
import com.yeonhx03.marketbriefapi.market.api.MarketClockResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class ProviderMarketStatusService implements MarketStatusService {

    static final String FINNHUB_API_KEY_HEADER = "X-Finnhub-Token";
    static final String TWELVE_DATA_AUTHORIZATION_HEADER = "Authorization";
    private static final String KOREA_EXCHANGE_CODE = "XKRX";
    private static final String SEOUL_TIME_ZONE = "Asia/Seoul";
    private static final String NEW_YORK_TIME_ZONE = "America/New_York";
    private static final DateTimeFormatter DISPLAY_TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private final RestClient finnhubRestClient;
    private final RestClient twelveDataRestClient;
    private final String finnhubApiKey;
    private final String twelveDataApiKey;
    private final Clock clock;

    @Autowired
    public ProviderMarketStatusService(
            RestClient.Builder restClientBuilder,
            @Value("${market-brief.market-data.finnhub.base-url:https://finnhub.io/api/v1}")
            String finnhubBaseUrl,
            @Value("${market-brief.market-data.finnhub.api-key:}") String finnhubApiKey,
            @Value("${market-brief.market-data.twelve-data.base-url:https://api.twelvedata.com}")
            String twelveDataBaseUrl,
            @Value("${market-brief.market-data.twelve-data.api-key:}") String twelveDataApiKey,
            Clock clock
    ) {
        this(
                restClientBuilder.clone().baseUrl(finnhubBaseUrl).build(),
                restClientBuilder.clone().baseUrl(twelveDataBaseUrl).build(),
                finnhubApiKey,
                twelveDataApiKey,
                clock
        );
    }

    ProviderMarketStatusService(
            RestClient finnhubRestClient,
            RestClient twelveDataRestClient,
            String finnhubApiKey,
            String twelveDataApiKey,
            Clock clock
    ) {
        this.finnhubRestClient = finnhubRestClient;
        this.twelveDataRestClient = twelveDataRestClient;
        this.finnhubApiKey = finnhubApiKey;
        this.twelveDataApiKey = twelveDataApiKey;
        this.clock = clock;
    }

    @Override
    @Cacheable(
            cacheNames = MarketDataCacheConfiguration.MARKET_STATUS_CACHE,
            key = "'current'"
    )
    public List<MarketClockResponse> getStatus() {
        if (finnhubApiKey.isBlank() || twelveDataApiKey.isBlank()) {
            throw new MarketStatusUnavailableException();
        }

        try {
            return List.of(getSeoulMarketClock(), getNewYorkMarketClock());
        } catch (RestClientException | DateTimeException exception) {
            throw new MarketStatusUnavailableException(exception);
        }
    }

    private MarketClockResponse getSeoulMarketClock() {
        var providerResponse = twelveDataRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/market_state")
                        .queryParam("country", "South Korea")
                        .build())
                .header(TWELVE_DATA_AUTHORIZATION_HEADER, "apikey " + twelveDataApiKey)
                .retrieve()
                .body(TwelveDataMarketStateResponse[].class);

        var krxState = providerResponse == null
                ? null
                : Arrays.stream(providerResponse)
                        .filter(state -> KOREA_EXCHANGE_CODE.equals(state.code()))
                        .findFirst()
                        .orElse(null);

        if (krxState == null || krxState.isMarketOpen() == null) {
            throw new MarketStatusUnavailableException();
        }

        return new MarketClockResponse(
                "서울",
                SEOUL_TIME_ZONE,
                clock.instant()
                        .atZone(ZoneId.of(SEOUL_TIME_ZONE))
                        .format(DISPLAY_TIME_FORMAT),
                krxState.isMarketOpen() ? "정규장" : "장 마감"
        );
    }

    private MarketClockResponse getNewYorkMarketClock() {
        var providerResponse = finnhubRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stock/market-status")
                        .queryParam("exchange", "US")
                        .build())
                .header(FINNHUB_API_KEY_HEADER, finnhubApiKey)
                .retrieve()
                .body(FinnhubMarketStatusResponse.class);

        if (providerResponse == null || providerResponse.t() == null) {
            throw new MarketStatusUnavailableException();
        }

        var timeZone = isBlank(providerResponse.timezone())
                ? NEW_YORK_TIME_ZONE
                : providerResponse.timezone();
        var displayTime = Instant.ofEpochSecond(providerResponse.t())
                .atZone(ZoneId.of(timeZone))
                .format(DISPLAY_TIME_FORMAT);

        return new MarketClockResponse(
                "뉴욕",
                timeZone,
                displayTime,
                translateStatus(providerResponse.session())
        );
    }

    private String translateStatus(String session) {
        if (session == null) {
            return "장 마감";
        }

        return switch (session) {
            case "pre-market" -> "프리마켓";
            case "regular" -> "정규장";
            case "post-market" -> "애프터마켓";
            default -> "장 마감";
        };
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TwelveDataMarketStateResponse(
            String name,
            String code,
            String country,
            @JsonProperty("is_market_open") Boolean isMarketOpen
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record FinnhubMarketStatusResponse(
            String exchange,
            String holiday,
            Boolean isOpen,
            String session,
            String timezone,
            Long t
    ) {
    }
}
