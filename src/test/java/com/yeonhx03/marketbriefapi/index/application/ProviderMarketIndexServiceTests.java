package com.yeonhx03.marketbriefapi.index.application;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ProviderMarketIndexServiceTests {

    @Test
    void mapsFmpIndexesAndTwelveDataSoxxInDashboardOrder() {
        var fmpBuilder = RestClient.builder().baseUrl("https://financialmodelingprep.com");
        var fmpServer = MockRestServiceServer.bindTo(fmpBuilder).build();
        var twelveDataBuilder = RestClient.builder().baseUrl("https://api.twelvedata.com");
        var twelveDataServer = MockRestServiceServer.bindTo(twelveDataBuilder).build();
        var service = new ProviderMarketIndexService(
                fmpBuilder.build(),
                twelveDataBuilder.build(),
                "fmp-secret",
                "twelve-data-secret"
        );

        expectFmpQuote(fmpServer, "%5EGSPC", "^GSPC", "7711.76", "-0.24874");
        expectFmpQuote(fmpServer, "%5EIXIC", "^IXIC", "26402.424", "-0.52344");
        expectFmpQuote(fmpServer, "%5EDJI", "^DJI", "53559.99", "0.18");

        twelveDataServer.expect(requestTo(
                        "https://api.twelvedata.com/quote?symbol=SOXX"
                ))
                .andExpect(header(
                        ProviderMarketIndexService.TWELVE_DATA_AUTHORIZATION_HEADER,
                        "apikey twelve-data-secret"
                ))
                .andRespond(withSuccess("""
                        {
                          "symbol": "SOXX",
                          "close": "508.62000",
                          "percent_change": "-3.19928"
                        }
                        """, MediaType.APPLICATION_JSON));

        var indexes = service.getIndexes();

        assertThat(indexes)
                .extracting("ticker", "name", "value", "changePercent")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(
                                "SPX", "S&P 500", 7711.76, -0.24874
                        ),
                        org.assertj.core.groups.Tuple.tuple(
                                "IXIC", "NASDAQ", 26402.424, -0.52344
                        ),
                        org.assertj.core.groups.Tuple.tuple(
                                "DJI", "DOW", 53559.99, 0.18
                        ),
                        org.assertj.core.groups.Tuple.tuple(
                                "SOXX", "SOXX", 508.62, -3.19928
                        )
                );
        fmpServer.verify();
        twelveDataServer.verify();
    }

    @Test
    void reportsUnavailableWhenEitherApiKeyIsMissing() {
        var service = new ProviderMarketIndexService(
                RestClient.builder().build(),
                RestClient.builder().build(),
                "fmp-secret",
                " "
        );

        assertThatThrownBy(service::getIndexes)
                .isInstanceOf(MarketIndexUnavailableException.class);
    }

    @Test
    void reportsUnavailableWhenFmpReturnsNoQuote() {
        var fmpBuilder = RestClient.builder().baseUrl("https://financialmodelingprep.com");
        var fmpServer = MockRestServiceServer.bindTo(fmpBuilder).build();
        var service = new ProviderMarketIndexService(
                fmpBuilder.build(),
                RestClient.builder().build(),
                "fmp-secret",
                "twelve-data-secret"
        );

        fmpServer.expect(requestTo(
                        "https://financialmodelingprep.com/stable/quote"
                                + "?symbol=%5EGSPC&apikey=fmp-secret"
                ))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertThatThrownBy(service::getIndexes)
                .isInstanceOf(MarketIndexUnavailableException.class);
        fmpServer.verify();
    }

    @Test
    void reportsUnavailableWhenFmpFails() {
        var fmpBuilder = RestClient.builder().baseUrl("https://financialmodelingprep.com");
        var fmpServer = MockRestServiceServer.bindTo(fmpBuilder).build();
        var service = new ProviderMarketIndexService(
                fmpBuilder.build(),
                RestClient.builder().build(),
                "fmp-secret",
                "twelve-data-secret"
        );

        fmpServer.expect(requestTo(
                        "https://financialmodelingprep.com/stable/quote"
                                + "?symbol=%5EGSPC&apikey=fmp-secret"
                ))
                .andRespond(withServerError());

        assertThatThrownBy(service::getIndexes)
                .isInstanceOf(MarketIndexUnavailableException.class);
        fmpServer.verify();
    }

    private void expectFmpQuote(
            MockRestServiceServer server,
            String encodedProviderSymbol,
            String providerSymbol,
            String price,
            String changePercentage
    ) {
        server.expect(requestTo(
                        "https://financialmodelingprep.com/stable/quote"
                                + "?symbol=" + encodedProviderSymbol
                                + "&apikey=fmp-secret"
                ))
                .andRespond(withSuccess("""
                        [{
                          "symbol": "%s",
                          "price": %s,
                          "changePercentage": %s
                        }]
                        """.formatted(
                        providerSymbol,
                        price,
                        changePercentage
                ), MediaType.APPLICATION_JSON));
    }
}
