package com.yeonhx03.marketbriefapi.heatmap.application;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FinnhubMarketHeatmapServiceTests {

    @Test
    void mapsProviderDataIntoSectorHierarchyAndCalculatesRelativeWeights() {
        var builder = RestClient.builder().baseUrl("https://finnhub.io/api/v1");
        var server = MockRestServiceServer.bindTo(builder).build();
        var service = new FinnhubMarketHeatmapService(
                builder,
                "https://finnhub.io/api/v1",
                "finnhub-secret"
        );

        expectStock(server, "NVDA", "NVIDIA", 700, 2.41);
        expectStock(server, "AAPL", "Apple", 600, -0.84);
        expectStock(server, "MSFT", "Microsoft", 500, 0.37);
        expectStock(server, "GOOGL", "Alphabet", 400, -0.26);
        expectStock(server, "META", "Meta Platforms", 300, 1.03);
        expectStock(server, "AMZN", "Amazon", 200, -1.12);
        expectStock(server, "TSLA", "Tesla", 100, 0.08);

        var sectors = service.getHeatmap();

        assertThat(sectors)
                .extracting("sectorId", "sectorName")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("technology", "기술"),
                        org.assertj.core.groups.Tuple.tuple("communication", "커뮤니케이션"),
                        org.assertj.core.groups.Tuple.tuple("consumer", "소비재")
                );
        assertThat(sectors.getFirst().stocks())
                .extracting("ticker", "companyName", "changePercent")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("NVDA", "NVIDIA", 2.41),
                        org.assertj.core.groups.Tuple.tuple("AAPL", "Apple", -0.84),
                        org.assertj.core.groups.Tuple.tuple("MSFT", "Microsoft", 0.37)
                );
        assertThat(sectors.getFirst().stocks().getFirst().marketCapWeight())
                .isCloseTo(25.0, within(0.000001));
        assertThat(sectors.stream()
                .flatMap(sector -> sector.stocks().stream())
                .mapToDouble(stock -> stock.marketCapWeight())
                .sum()).isCloseTo(100.0, within(0.000001));
        server.verify();
    }

    @Test
    void reportsUnavailableWhenApiKeyIsMissing() {
        var service = new FinnhubMarketHeatmapService(
                RestClient.builder(),
                "https://finnhub.io/api/v1",
                " "
        );

        assertThatThrownBy(service::getHeatmap)
                .isInstanceOf(MarketHeatmapUnavailableException.class);
    }

    @Test
    void reportsUnavailableWhenProviderOmitsRequiredMarketCap() {
        var builder = RestClient.builder().baseUrl("https://finnhub.io/api/v1");
        var server = MockRestServiceServer.bindTo(builder).build();
        var service = new FinnhubMarketHeatmapService(
                builder,
                "https://finnhub.io/api/v1",
                "finnhub-secret"
        );

        expectQuote(server, "NVDA", 2.41);
        server.expect(requestTo(
                        "https://finnhub.io/api/v1/stock/profile2?symbol=NVDA"
                ))
                .andExpect(header(
                        FinnhubMarketHeatmapService.API_KEY_HEADER,
                        "finnhub-secret"
                ))
                .andRespond(withSuccess("""
                        {"ticker":"NVDA","name":"NVIDIA"}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(service::getHeatmap)
                .isInstanceOf(MarketHeatmapUnavailableException.class);
        server.verify();
    }

    @Test
    void reportsUnavailableWhenProviderFails() {
        var builder = RestClient.builder().baseUrl("https://finnhub.io/api/v1");
        var server = MockRestServiceServer.bindTo(builder).build();
        var service = new FinnhubMarketHeatmapService(
                builder,
                "https://finnhub.io/api/v1",
                "finnhub-secret"
        );

        server.expect(requestTo("https://finnhub.io/api/v1/quote?symbol=NVDA"))
                .andExpect(header(
                        FinnhubMarketHeatmapService.API_KEY_HEADER,
                        "finnhub-secret"
                ))
                .andRespond(withServerError());

        assertThatThrownBy(service::getHeatmap)
                .isInstanceOf(MarketHeatmapUnavailableException.class);
        server.verify();
    }

    private void expectStock(
            MockRestServiceServer server,
            String ticker,
            String companyName,
            double marketCapitalization,
            double changePercent
    ) {
        expectQuote(server, ticker, changePercent);
        server.expect(requestTo(
                        "https://finnhub.io/api/v1/stock/profile2?symbol=" + ticker
                ))
                .andExpect(header(
                        FinnhubMarketHeatmapService.API_KEY_HEADER,
                        "finnhub-secret"
                ))
                .andRespond(withSuccess("""
                        {
                          "ticker": "%s",
                          "name": "%s",
                          "marketCapitalization": %s
                        }
                        """.formatted(
                        ticker,
                        companyName,
                        marketCapitalization
                ), MediaType.APPLICATION_JSON));
    }

    private void expectQuote(
            MockRestServiceServer server,
            String ticker,
            double changePercent
    ) {
        server.expect(requestTo(
                        "https://finnhub.io/api/v1/quote?symbol=" + ticker
                ))
                .andExpect(header(
                        FinnhubMarketHeatmapService.API_KEY_HEADER,
                        "finnhub-secret"
                ))
                .andRespond(withSuccess("""
                        {"dp": %s}
                        """.formatted(changePercent), MediaType.APPLICATION_JSON));
    }
}
