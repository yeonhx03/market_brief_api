package com.yeonhx03.marketbriefapi.market.application;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ProviderMarketStatusServiceTests {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-08-31T00:30:00Z"),
            ZoneOffset.UTC
    );

    @Test
    void mapsKoreanAndUnitedStatesMarketStatus() {
        var finnhubBuilder = RestClient.builder().baseUrl("https://finnhub.io/api/v1");
        var finnhubServer = MockRestServiceServer.bindTo(finnhubBuilder).build();
        var twelveDataBuilder = RestClient.builder().baseUrl("https://api.twelvedata.com");
        var twelveDataServer = MockRestServiceServer.bindTo(twelveDataBuilder).build();
        var service = new ProviderMarketStatusService(
                finnhubBuilder.build(),
                twelveDataBuilder.build(),
                "finnhub-secret",
                "twelve-data-secret",
                FIXED_CLOCK
        );
        var timestamp = FIXED_CLOCK.instant().getEpochSecond();

        twelveDataServer.expect(requestTo(
                        "https://api.twelvedata.com/market_state?country=South%20Korea"
                ))
                .andExpect(header(
                        ProviderMarketStatusService.TWELVE_DATA_AUTHORIZATION_HEADER,
                        "apikey twelve-data-secret"
                ))
                .andRespond(withSuccess("""
                        [
                          {
                            "name": "KRX",
                            "code": "XKRX",
                            "country": "South Korea",
                            "is_market_open": true
                          },
                          {
                            "name": "KOSDAQ",
                            "code": "XKOS",
                            "country": "South Korea",
                            "is_market_open": true
                          }
                        ]
                        """, MediaType.APPLICATION_JSON));

        finnhubServer.expect(requestTo(
                        "https://finnhub.io/api/v1/stock/market-status?exchange=US"
                ))
                .andExpect(header(
                        ProviderMarketStatusService.FINNHUB_API_KEY_HEADER,
                        "finnhub-secret"
                ))
                .andRespond(withSuccess("""
                        {
                          "exchange": "US",
                          "holiday": null,
                          "isOpen": false,
                          "session": null,
                          "timezone": "America/New_York",
                          "t": %d
                        }
                        """.formatted(timestamp), MediaType.APPLICATION_JSON));

        var results = service.getStatus();

        assertThat(results)
                .extracting("city", "timeZone", "displayTime", "status")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(
                                "서울", "Asia/Seoul", "09:30", "정규장"
                        ),
                        org.assertj.core.groups.Tuple.tuple(
                                "뉴욕", "America/New_York", "20:30", "장 마감"
                        )
                );
        twelveDataServer.verify();
        finnhubServer.verify();
    }

    @Test
    void reportsUnavailableWhenEitherApiKeyIsMissing() {
        var service = new ProviderMarketStatusService(
                RestClient.builder().build(),
                RestClient.builder().build(),
                "finnhub-secret",
                " ",
                FIXED_CLOCK
        );

        assertThatThrownBy(service::getStatus)
                .isInstanceOf(MarketStatusUnavailableException.class);
    }

    @Test
    void reportsUnavailableWhenKrxStateIsAbsent() {
        var finnhubBuilder = RestClient.builder().baseUrl("https://finnhub.io/api/v1");
        var twelveDataBuilder = RestClient.builder().baseUrl("https://api.twelvedata.com");
        var twelveDataServer = MockRestServiceServer.bindTo(twelveDataBuilder).build();
        var service = new ProviderMarketStatusService(
                finnhubBuilder.build(),
                twelveDataBuilder.build(),
                "finnhub-secret",
                "twelve-data-secret",
                FIXED_CLOCK
        );

        twelveDataServer.expect(requestTo(
                        "https://api.twelvedata.com/market_state?country=South%20Korea"
                ))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertThatThrownBy(service::getStatus)
                .isInstanceOf(MarketStatusUnavailableException.class);
        twelveDataServer.verify();
    }

    @Test
    void reportsUnavailableWhenProviderFails() {
        var finnhubBuilder = RestClient.builder().baseUrl("https://finnhub.io/api/v1");
        var twelveDataBuilder = RestClient.builder().baseUrl("https://api.twelvedata.com");
        var twelveDataServer = MockRestServiceServer.bindTo(twelveDataBuilder).build();
        var service = new ProviderMarketStatusService(
                finnhubBuilder.build(),
                twelveDataBuilder.build(),
                "finnhub-secret",
                "twelve-data-secret",
                FIXED_CLOCK
        );

        twelveDataServer.expect(requestTo(
                        "https://api.twelvedata.com/market_state?country=South%20Korea"
                ))
                .andRespond(withServerError());

        assertThatThrownBy(service::getStatus)
                .isInstanceOf(MarketStatusUnavailableException.class);
        twelveDataServer.verify();
    }
}
