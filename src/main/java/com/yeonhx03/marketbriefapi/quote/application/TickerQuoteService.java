package com.yeonhx03.marketbriefapi.quote.application;

import com.yeonhx03.marketbriefapi.quote.api.TickerQuoteResponse;

public interface TickerQuoteService {

    TickerQuoteResponse getQuote(String ticker);
}
