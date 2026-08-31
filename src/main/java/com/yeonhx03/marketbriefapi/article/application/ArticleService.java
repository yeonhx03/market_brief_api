package com.yeonhx03.marketbriefapi.article.application;

import com.yeonhx03.marketbriefapi.article.api.ArticleResponse;
import com.yeonhx03.marketbriefapi.article.api.CreateArticleRequest;

import java.util.List;

public interface ArticleService {

    ArticleResponse create(CreateArticleRequest request);

    void addTicker(Long articleId, String ticker);

    List<ArticleResponse> findLatest(int limit);
}
