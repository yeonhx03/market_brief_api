package com.yeonhx03.marketbriefapi.symbol.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yeonhx03.marketbriefapi.config.MarketDataCacheConfiguration;
import com.yeonhx03.marketbriefapi.symbol.api.TickerSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

@Service
public class FinnhubTickerSearchService implements TickerSearchService {

    static final String API_KEY_HEADER = "X-Finnhub-Token";
    private static final String US_EXCHANGE = "US";

    private final RestClient restClient;
    private final String apiKey;

    public FinnhubTickerSearchService(
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
            cacheNames = MarketDataCacheConfiguration.TICKER_SEARCH_CACHE,
            key = "#p0.toLowerCase() + ':' + #p1"
    )
    public List<TickerSearchResponse> search(String query, int limit) {
        if (apiKey.isBlank()) {
            throw new TickerSearchUnavailableException();
        }

        try {
            var providerResponse = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q", query)
                            .queryParam("exchange", US_EXCHANGE)
                            .build())
                    .header(API_KEY_HEADER, apiKey)
                    .retrieve()
                    .body(FinnhubSearchResponse.class);

            return mapResults(providerResponse, limit);
        } catch (RestClientException exception) {
            throw new TickerSearchUnavailableException(exception);
        }
    }

    private List<TickerSearchResponse> mapResults(
            FinnhubSearchResponse providerResponse,
            int limit
    ) {
        if (providerResponse == null || providerResponse.result() == null) {
            return List.of();
        }

        var resultsByTicker = new LinkedHashMap<String, TickerSearchResponse>();

        for (var result : providerResponse.result()) {
            if (result == null || isBlank(result.displaySymbol())) {
                continue;
            }

            var ticker = result.displaySymbol().trim().toUpperCase(Locale.ROOT);
            var companyName = isBlank(result.description())
                    ? ticker
                    : result.description().trim();

            resultsByTicker.putIfAbsent(
                    ticker,
                    new TickerSearchResponse(ticker, companyName, US_EXCHANGE)
            );

            if (resultsByTicker.size() == limit) {
                break;
            }
        }

        return List.copyOf(resultsByTicker.values());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record FinnhubSearchResponse(List<FinnhubSearchResult> result) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record FinnhubSearchResult(
            String description,
            String displaySymbol,
            String symbol,
            String type
    ) {
    }
}
