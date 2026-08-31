package com.yeonhx03.marketbriefapi.index.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yeonhx03.marketbriefapi.config.MarketDataCacheConfiguration;
import com.yeonhx03.marketbriefapi.index.api.MarketIndexResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProviderMarketIndexService implements MarketIndexService {

    static final String TWELVE_DATA_AUTHORIZATION_HEADER = "Authorization";
    private static final List<IndexDefinition> FMP_INDEXES = List.of(
            new IndexDefinition("^GSPC", "SPX", "S&P 500"),
            new IndexDefinition("^IXIC", "IXIC", "NASDAQ"),
            new IndexDefinition("^DJI", "DJI", "DOW")
    );

    private final RestClient fmpRestClient;
    private final RestClient twelveDataRestClient;
    private final String fmpApiKey;
    private final String twelveDataApiKey;

    @Autowired
    public ProviderMarketIndexService(
            RestClient.Builder restClientBuilder,
            @Value("${market-brief.market-data.fmp.base-url:https://financialmodelingprep.com}")
            String fmpBaseUrl,
            @Value("${market-brief.market-data.fmp.api-key:}") String fmpApiKey,
            @Value("${market-brief.market-data.twelve-data.base-url:https://api.twelvedata.com}")
            String twelveDataBaseUrl,
            @Value("${market-brief.market-data.twelve-data.api-key:}")
            String twelveDataApiKey
    ) {
        this(
                restClientBuilder.clone().baseUrl(fmpBaseUrl).build(),
                restClientBuilder.clone().baseUrl(twelveDataBaseUrl).build(),
                fmpApiKey,
                twelveDataApiKey
        );
    }

    ProviderMarketIndexService(
            RestClient fmpRestClient,
            RestClient twelveDataRestClient,
            String fmpApiKey,
            String twelveDataApiKey
    ) {
        this.fmpRestClient = fmpRestClient;
        this.twelveDataRestClient = twelveDataRestClient;
        this.fmpApiKey = fmpApiKey;
        this.twelveDataApiKey = twelveDataApiKey;
    }

    @Override
    @Cacheable(
            cacheNames = MarketDataCacheConfiguration.MARKET_INDEX_CACHE,
            key = "'current'"
    )
    public List<MarketIndexResponse> getIndexes() {
        if (fmpApiKey.isBlank() || twelveDataApiKey.isBlank()) {
            throw new MarketIndexUnavailableException();
        }

        try {
            var indexes = new ArrayList<MarketIndexResponse>();
            FMP_INDEXES.stream()
                    .map(this::getFmpIndex)
                    .forEach(indexes::add);
            indexes.add(getSoxx());
            return List.copyOf(indexes);
        } catch (RestClientException | NumberFormatException exception) {
            throw new MarketIndexUnavailableException(exception);
        }
    }

    private MarketIndexResponse getFmpIndex(IndexDefinition index) {
        var quotes = fmpRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stable/quote")
                        .queryParam("symbol", index.providerSymbol())
                        .queryParam("apikey", fmpApiKey)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<FmpQuoteResponse>>() {
                });

        if (quotes == null || quotes.size() != 1) {
            throw new MarketIndexUnavailableException();
        }

        var quote = quotes.getFirst();
        if (quote.price() == null || quote.changePercentage() == null) {
            throw new MarketIndexUnavailableException();
        }

        return new MarketIndexResponse(
                index.ticker(),
                index.name(),
                quote.price(),
                quote.changePercentage()
        );
    }

    private MarketIndexResponse getSoxx() {
        var quote = twelveDataRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/quote")
                        .queryParam("symbol", "SOXX")
                        .build())
                .header(
                        TWELVE_DATA_AUTHORIZATION_HEADER,
                        "apikey " + twelveDataApiKey
                )
                .retrieve()
                .body(TwelveDataQuoteResponse.class);

        if (quote == null || isBlank(quote.close()) || isBlank(quote.percentChange())) {
            throw new MarketIndexUnavailableException();
        }

        return new MarketIndexResponse(
                "SOXX",
                "SOXX",
                Double.parseDouble(quote.close()),
                Double.parseDouble(quote.percentChange())
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    record IndexDefinition(String providerSymbol, String ticker, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record FmpQuoteResponse(
            String symbol,
            String name,
            Double price,
            Double changePercentage
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record TwelveDataQuoteResponse(
            String symbol,
            String name,
            String close,
            @JsonProperty("percent_change") String percentChange
    ) {
    }
}
