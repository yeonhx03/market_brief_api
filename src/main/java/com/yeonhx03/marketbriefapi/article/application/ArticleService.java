package com.yeonhx03.marketbriefapi.article.application;

import com.yeonhx03.marketbriefapi.article.api.ArticleResponse;
import com.yeonhx03.marketbriefapi.article.api.CreateArticleRequest;

import java.util.List;

public interface ArticleService {

    ArticleResponse create(CreateArticleRequest request);

    List<ArticleResponse> findLatest(int limit);
}
