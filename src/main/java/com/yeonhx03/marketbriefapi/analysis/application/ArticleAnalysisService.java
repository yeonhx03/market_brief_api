package com.yeonhx03.marketbriefapi.analysis.application;

import com.yeonhx03.marketbriefapi.analysis.api.ArticleAnalysisResponse;
import com.yeonhx03.marketbriefapi.analysis.api.CreateArticleAnalysisRequest;

import java.util.List;

public interface ArticleAnalysisService {

    ArticleAnalysisResponse create(Long articleId, CreateArticleAnalysisRequest request);

    List<ArticleAnalysisResponse> findByArticleId(Long articleId);
}
