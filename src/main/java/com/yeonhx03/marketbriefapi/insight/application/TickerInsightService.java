package com.yeonhx03.marketbriefapi.insight.application;

import com.yeonhx03.marketbriefapi.insight.api.TickerInsightResponse;

public interface TickerInsightService {

    TickerInsightResponse getInsight(String ticker, int limit);
}
