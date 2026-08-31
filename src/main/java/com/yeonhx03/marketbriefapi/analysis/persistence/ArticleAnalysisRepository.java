package com.yeonhx03.marketbriefapi.analysis.persistence;

import com.yeonhx03.marketbriefapi.analysis.domain.ArticleAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleAnalysisRepository extends JpaRepository<ArticleAnalysis, Long> {

    Optional<ArticleAnalysis> findByArticleIdAndAnalysisTypeAndAnalyzerNameAndAnalyzerVersion(
            Long articleId,
            String analysisType,
            String analyzerName,
            String analyzerVersion
    );

    List<ArticleAnalysis> findByArticleIdOrderByAnalyzedAtDescIdDesc(Long articleId);

    @Query("""
            select analysis
            from ArticleAnalysis analysis
            join fetch analysis.article article
            join article.tickers ticker
            where ticker = :ticker
              and analysis.analysisType = :analysisType
              and not exists (
                  select newer.id
                  from ArticleAnalysis newer
                  where newer.article = analysis.article
                    and newer.analysisType = analysis.analysisType
                    and (
                        newer.analyzedAt > analysis.analyzedAt
                        or (
                            newer.analyzedAt = analysis.analyzedAt
                            and newer.id > analysis.id
                        )
                    )
              )
            order by coalesce(article.publishedAt, article.collectedAt) desc,
                     article.id desc
            """)
    List<ArticleAnalysis> findLatestByTickerAndAnalysisType(
            @Param("ticker") String ticker,
            @Param("analysisType") String analysisType,
            Pageable pageable
    );
}
