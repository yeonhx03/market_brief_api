package com.yeonhx03.marketbriefapi.symbol.application;

import com.yeonhx03.marketbriefapi.symbol.api.TickerSearchResponse;

import java.util.List;

public interface TickerSearchService {

    List<TickerSearchResponse> search(String query, int limit);
}
