package com.yeonhx03.marketbriefapi.symbol.application;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FinnhubTickerSearchServiceTests {

    @Test
    void mapsRankedProviderResultsAndSendsApiKeyInHeader() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var service = new FinnhubTickerSearchService(
                builder,
                "https://finnhub.io/api/v1",
                "test-secret"
        );

        server.expect(once(), requestTo(
                        "https://finnhub.io/api/v1/search?q=apple&exchange=US"
                ))
                .andExpect(header(
                        FinnhubTickerSearchService.API_KEY_HEADER,
                        "test-secret"
                ))
                .andRespond(withSuccess("""
                        {
                          "count": 3,
                          "result": [
                            {
                              "description": "APPLE INC",
                              "displaySymbol": "AAPL",
                              "symbol": "AAPL",
                              "type": "Common Stock"
                            },
                            {
                              "description": "APPLE HOSPITALITY REIT INC",
                              "displaySymbol": "APLE",
                              "symbol": "APLE",
                              "type": "Common Stock"
                            },
                            {
                              "description": "DUPLICATE",
                              "displaySymbol": "AAPL",
                              "symbol": "AAPL",
                              "type": "Common Stock"
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        var results = service.search("apple", 2);

        assertThat(results)
                .extracting("ticker", "companyName", "exchange")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(
                                "AAPL", "APPLE INC", "US"
                        ),
                        org.assertj.core.groups.Tuple.tuple(
                                "APLE", "APPLE HOSPITALITY REIT INC", "US"
                        )
                );
        server.verify();
    }

    @Test
    void returnsEmptyListWhenProviderOmitsResults() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var service = new FinnhubTickerSearchService(
                builder,
                "https://finnhub.io/api/v1",
                "test-secret"
        );

        server.expect(requestTo(
                        "https://finnhub.io/api/v1/search?q=unknown&exchange=US"
                ))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThat(service.search("unknown", 10)).isEmpty();
        server.verify();
    }

    @Test
    void reportsUnavailableWhenApiKeyIsMissing() {
        var service = new FinnhubTickerSearchService(
                RestClient.builder(),
                "https://finnhub.io/api/v1",
                " "
        );

        assertThatThrownBy(() -> service.search("apple", 10))
                .isInstanceOf(TickerSearchUnavailableException.class);
    }

    @Test
    void reportsUnavailableWhenProviderFails() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var service = new FinnhubTickerSearchService(
                builder,
                "https://finnhub.io/api/v1",
                "test-secret"
        );

        server.expect(requestTo(
                        "https://finnhub.io/api/v1/search?q=apple&exchange=US"
                ))
                .andRespond(withServerError());

        assertThatThrownBy(() -> service.search("apple", 10))
                .isInstanceOf(TickerSearchUnavailableException.class);
        server.verify();
    }
}
