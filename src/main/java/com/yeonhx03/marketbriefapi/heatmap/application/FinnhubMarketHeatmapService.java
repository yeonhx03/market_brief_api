package com.yeonhx03.marketbriefapi.heatmap.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yeonhx03.marketbriefapi.config.MarketDataCacheConfiguration;
import com.yeonhx03.marketbriefapi.heatmap.api.MarketHeatmapSectorResponse;
import com.yeonhx03.marketbriefapi.heatmap.api.MarketHeatmapStockResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;

@Service
public class FinnhubMarketHeatmapService implements MarketHeatmapService {

    static final String API_KEY_HEADER = "X-Finnhub-Token";
    private static final List<SectorDefinition> SECTORS = List.of(
            new SectorDefinition(
                    "technology",
                    "기술",
                    List.of("NVDA", "AAPL", "MSFT")
            ),
            new SectorDefinition(
                    "communication",
                    "커뮤니케이션",
                    List.of("GOOGL", "META")
            ),
            new SectorDefinition(
                    "consumer",
                    "소비재",
                    List.of("AMZN", "TSLA")
            )
    );

    private final RestClient restClient;
    private final String apiKey;

    public FinnhubMarketHeatmapService(
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
            cacheNames = MarketDataCacheConfiguration.MARKET_HEATMAP_CACHE,
            key = "'current'"
    )
    public List<MarketHeatmapSectorResponse> getHeatmap() {
        if (apiKey.isBlank()) {
            throw new MarketHeatmapUnavailableException();
        }

        try {
            var stocks = requestStocks();
            var totalMarketCap = stocks.stream()
                    .mapToDouble(HeatmapStock::marketCapitalization)
                    .sum();

            if (totalMarketCap <= 0) {
                throw new MarketHeatmapUnavailableException();
            }

            var stockIndex = 0;
            var sectors = new ArrayList<MarketHeatmapSectorResponse>();
            for (var sector : SECTORS) {
                var sectorStocks = new ArrayList<MarketHeatmapStockResponse>();
                for (int i = 0; i < sector.tickers().size(); i++) {
                    var stock = stocks.get(stockIndex++);
                    sectorStocks.add(new MarketHeatmapStockResponse(
                            stock.ticker(),
                            stock.companyName(),
                            stock.marketCapitalization() / totalMarketCap * 100,
                            stock.changePercent()
                    ));
                }
                sectors.add(new MarketHeatmapSectorResponse(
                        sector.sectorId(),
                        sector.sectorName(),
                        List.copyOf(sectorStocks)
                ));
            }
            return List.copyOf(sectors);
        } catch (RestClientException exception) {
            throw new MarketHeatmapUnavailableException(exception);
        }
    }

    private List<HeatmapStock> requestStocks() {
        var stocks = new ArrayList<HeatmapStock>();
        for (var sector : SECTORS) {
            for (var ticker : sector.tickers()) {
                var quote = requestQuote(ticker);
                var profile = requestProfile(ticker);

                if (quote == null || quote.dp() == null
                        || profile == null || isBlank(profile.name())
                        || profile.marketCapitalization() == null
                        || profile.marketCapitalization() <= 0) {
                    throw new MarketHeatmapUnavailableException();
                }

                stocks.add(new HeatmapStock(
                        ticker,
                        profile.name().trim(),
                        profile.marketCapitalization(),
                        quote.dp()
                ));
            }
        }
        return stocks;
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

    record SectorDefinition(String sectorId, String sectorName, List<String> tickers) {
    }

    record HeatmapStock(
            String ticker,
            String companyName,
            double marketCapitalization,
            double changePercent
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record FinnhubQuoteResponse(Double dp) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record FinnhubCompanyProfileResponse(
            String ticker,
            String name,
            Double marketCapitalization
    ) {
    }
}
