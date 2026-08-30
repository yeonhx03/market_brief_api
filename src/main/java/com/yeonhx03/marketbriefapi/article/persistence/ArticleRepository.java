package com.yeonhx03.marketbriefapi.article.persistence;

import com.yeonhx03.marketbriefapi.article.domain.Article;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    Optional<Article> findBySourceAndSourceArticleId(String source, String sourceArticleId);

    Optional<Article> findByCanonicalUrl(String canonicalUrl);

    Optional<Article> findByContentHash(String contentHash);

    Optional<Article> findByUrl(String url);

    List<Article> findAllByOrderByCollectedAtDescIdDesc(Pageable pageable);
}
