package com.yeonhx03.marketbriefapi.analysis.application;

import com.yeonhx03.marketbriefapi.analysis.api.ArticleAnalysisResponse;
import com.yeonhx03.marketbriefapi.analysis.api.CreateArticleAnalysisRequest;
import com.yeonhx03.marketbriefapi.analysis.domain.ArticleAnalysis;
import com.yeonhx03.marketbriefapi.analysis.persistence.ArticleAnalysisRepository;
import com.yeonhx03.marketbriefapi.article.persistence.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
class JpaArticleAnalysisService implements ArticleAnalysisService {

    private final ArticleRepository articleRepository;
    private final ArticleAnalysisRepository analysisRepository;

    JpaArticleAnalysisService(
            ArticleRepository articleRepository,
            ArticleAnalysisRepository analysisRepository
    ) {
        this.articleRepository = articleRepository;
        this.analysisRepository = analysisRepository;
    }

    @Override
    @Transactional
    public ArticleAnalysisResponse create(
            Long articleId,
            CreateArticleAnalysisRequest request
    ) {
        var article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException(articleId));

        analysisRepository
                .findByArticleIdAndAnalysisTypeAndAnalyzerNameAndAnalyzerVersion(
                        articleId,
                        request.analysisType(),
                        request.analyzerName(),
                        request.analyzerVersion()
                )
                .ifPresent(this::throwDuplicate);

        var analysis = new ArticleAnalysis(
                article,
                request.analysisType(),
                request.analyzerName(),
                request.analyzerVersion(),
                request.analyzedAt(),
                request.textSentiment(),
                request.positiveScore(),
                request.neutralScore(),
                request.negativeScore(),
                request.confidence()
        );

        return toResponse(analysisRepository.save(analysis));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleAnalysisResponse> findByArticleId(Long articleId) {
        if (!articleRepository.existsById(articleId)) {
            throw new ArticleNotFoundException(articleId);
        }

        return analysisRepository
                .findByArticleIdOrderByAnalyzedAtDescIdDesc(articleId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void throwDuplicate(ArticleAnalysis analysis) {
        throw new DuplicateArticleAnalysisException(analysis.getId());
    }

    private ArticleAnalysisResponse toResponse(ArticleAnalysis analysis) {
        return new ArticleAnalysisResponse(
                analysis.getId(),
                analysis.getArticle().getId(),
                analysis.getAnalysisType(),
                analysis.getAnalyzerName(),
                analysis.getAnalyzerVersion(),
                analysis.getAnalyzedAt(),
                analysis.getTextSentiment(),
                analysis.getPositiveScore(),
                analysis.getNeutralScore(),
                analysis.getNegativeScore(),
                analysis.getConfidence()
        );
    }
}
