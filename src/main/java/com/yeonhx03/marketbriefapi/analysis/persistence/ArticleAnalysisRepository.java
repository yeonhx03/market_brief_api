package com.yeonhx03.marketbriefapi.analysis.persistence;

import com.yeonhx03.marketbriefapi.analysis.domain.ArticleAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
