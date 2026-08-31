package com.yeonhx03.marketbriefapi.market.application;

import com.yeonhx03.marketbriefapi.market.api.MarketClockResponse;

import java.util.List;

public interface MarketStatusService {

    List<MarketClockResponse> getStatus();
}
