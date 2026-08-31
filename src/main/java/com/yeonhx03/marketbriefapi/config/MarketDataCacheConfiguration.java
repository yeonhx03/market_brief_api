package com.yeonhx03.marketbriefapi.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class MarketDataCacheConfiguration {

    public static final String TICKER_SEARCH_CACHE = "ticker-search";
    public static final String MARKET_STATUS_CACHE = "market-status";
    public static final String TICKER_QUOTE_CACHE = "ticker-quote";
    public static final String MARKET_INDEX_CACHE = "market-index";
    public static final String MARKET_HEATMAP_CACHE = "market-heatmap";

    @Bean
    CacheManager marketDataCacheManager() {
        var tickerSearchCache = new CaffeineCache(
                TICKER_SEARCH_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(1_000)
                        .expireAfterWrite(Duration.ofMinutes(5))
                        .build()
        );
        var marketStatusCache = new CaffeineCache(
                MARKET_STATUS_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(1)
                        .expireAfterWrite(Duration.ofSeconds(30))
                        .build()
        );
        var tickerQuoteCache = new CaffeineCache(
                TICKER_QUOTE_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(Duration.ofSeconds(15))
                        .build()
        );
        var marketIndexCache = new CaffeineCache(
                MARKET_INDEX_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(1)
                        .expireAfterWrite(Duration.ofSeconds(60))
                        .build()
        );
        var marketHeatmapCache = new CaffeineCache(
                MARKET_HEATMAP_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(1)
                        .expireAfterWrite(Duration.ofSeconds(60))
                        .build()
        );

        var cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
                tickerSearchCache,
                marketStatusCache,
                tickerQuoteCache,
                marketIndexCache,
                marketHeatmapCache
        ));
        return cacheManager;
    }
}
