package com.yeonhx03.marketbriefapi.briefing.application;

import com.yeonhx03.marketbriefapi.briefing.api.BriefingResponse;
import com.yeonhx03.marketbriefapi.briefing.api.CreateBriefingRequest;

public interface BriefingService {

    BriefingResponse create(CreateBriefingRequest request);

    BriefingResponse findLatest();
}
