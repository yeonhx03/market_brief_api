package com.yeonhx03.marketbriefapi.quote.application;

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

class FinnhubTickerQuoteServiceTests {

    @Test
    void combinesQuoteAndCompanyProfile() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var service = new FinnhubTickerQuoteService(
                builder,
                "https://finnhub.io/api/v1",
                "test-secret"
        );

        server.expect(requestTo(
                        "https://finnhub.io/api/v1/quote?symbol=AAPL"
                ))
                .andExpect(header(
                        FinnhubTickerQuoteService.API_KEY_HEADER,
                        "test-secret"
                ))
                .andRespond(withSuccess("""
                        {
                          "c": 231.42,
                          "d": 2.18,
                          "dp": 0.95,
                          "h": 233.00,
                          "l": 228.50,
                          "o": 229.10,
                          "pc": 229.24,
                          "t": 1788186600
                        }
                        """, MediaType.APPLICATION_JSON));

        server.expect(requestTo(
                        "https://finnhub.io/api/v1/stock/profile2?symbol=AAPL"
                ))
                .andExpect(header(
                        FinnhubTickerQuoteService.API_KEY_HEADER,
                        "test-secret"
                ))
                .andRespond(withSuccess("""
                        {
                          "ticker": "AAPL",
                          "name": "Apple Inc",
                          "currency": "USD",
                          "exchange": "NASDAQ NMS - GLOBAL MARKET"
                        }
                        """, MediaType.APPLICATION_JSON));

        var response = service.getQuote("aapl");

        assertThat(response.ticker()).isEqualTo("AAPL");
        assertThat(response.companyName()).isEqualTo("Apple Inc");
        assertThat(response.currentPrice()).isEqualTo(231.42);
        assertThat(response.changePercent()).isEqualTo(0.95);
        assertThat(response.volume()).isNull();
        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.dataAsOf())
                .isEqualTo("2026-08-31T14:30:00Z");
        server.verify();
    }

    @Test
    void reportsNotFoundWhenProviderHasNoQuote() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var service = new FinnhubTickerQuoteService(
                builder,
                "https://finnhub.io/api/v1",
                "test-secret"
        );

        server.expect(requestTo(
                        "https://finnhub.io/api/v1/quote?symbol=UNKNOWN"
                ))
                .andRespond(withSuccess("{\"c\":0}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> service.getQuote("unknown"))
                .isInstanceOf(TickerQuoteNotFoundException.class);
        server.verify();
    }

    @Test
    void reportsUnavailableWhenApiKeyIsMissing() {
        var service = new FinnhubTickerQuoteService(
                RestClient.builder(),
                "https://finnhub.io/api/v1",
                " "
        );

        assertThatThrownBy(() -> service.getQuote("AAPL"))
                .isInstanceOf(TickerQuoteUnavailableException.class);
    }

    @Test
    void reportsUnavailableWhenProviderFails() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var service = new FinnhubTickerQuoteService(
                builder,
                "https://finnhub.io/api/v1",
                "test-secret"
        );

        server.expect(requestTo(
                        "https://finnhub.io/api/v1/quote?symbol=AAPL"
                ))
                .andRespond(withServerError());

        assertThatThrownBy(() -> service.getQuote("AAPL"))
                .isInstanceOf(TickerQuoteUnavailableException.class);
        server.verify();
    }
}
