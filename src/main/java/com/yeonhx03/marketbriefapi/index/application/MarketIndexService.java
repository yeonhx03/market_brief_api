package com.yeonhx03.marketbriefapi.index.application;

import com.yeonhx03.marketbriefapi.index.api.MarketIndexResponse;

import java.util.List;

public interface MarketIndexService {

    List<MarketIndexResponse> getIndexes();
}
