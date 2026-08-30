package com.yeonhx03.marketbriefapi.analysis.api;

import com.yeonhx03.marketbriefapi.analysis.application.ArticleNotFoundException;
import com.yeonhx03.marketbriefapi.analysis.application.DuplicateArticleAnalysisException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ArticleAnalysisController.class)
public class ArticleAnalysisExceptionHandler {

    @ExceptionHandler(ArticleNotFoundException.class)
    ResponseEntity<ArticleAnalysisErrorResponse> handleArticleNotFound(
            ArticleNotFoundException exception
    ) {
        var error = new ArticleAnalysisErrorResponse(
                "ARTICLE_NOT_FOUND",
                exception.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DuplicateArticleAnalysisException.class)
    ResponseEntity<ArticleAnalysisErrorResponse> handleDuplicateAnalysis(
            DuplicateArticleAnalysisException exception
    ) {
        return duplicateResponse(exception.getExistingAnalysisId());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ArticleAnalysisErrorResponse> handleConcurrentDuplicate() {
        return duplicateResponse(null);
    }

    private ResponseEntity<ArticleAnalysisErrorResponse> duplicateResponse(
            Long existingAnalysisId
    ) {
        var error = new ArticleAnalysisErrorResponse(
                "ARTICLE_ANALYSIS_DUPLICATE",
                "Article analysis already exists",
                existingAnalysisId
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
