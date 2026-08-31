package com.yeonhx03.marketbriefapi.article.api;

import com.yeonhx03.marketbriefapi.article.application.DuplicateArticleException;
import com.yeonhx03.marketbriefapi.article.application.ArticleNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ArticleController.class)
public class ArticleExceptionHandler {

    @ExceptionHandler(ArticleNotFoundException.class)
    ResponseEntity<ArticleNotFoundErrorResponse> handleArticleNotFound(
            ArticleNotFoundException exception
    ) {
        var error = new ArticleNotFoundErrorResponse(
                "ARTICLE_NOT_FOUND",
                "Article not found",
                exception.getArticleId()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DuplicateArticleException.class)
    ResponseEntity<ArticleErrorResponse> handleDuplicateArticle(
            DuplicateArticleException exception
    ) {
        return duplicateResponse(exception.getExistingArticleId());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ArticleErrorResponse> handleConcurrentDuplicate() {
        return duplicateResponse(null);
    }

    private ResponseEntity<ArticleErrorResponse> duplicateResponse(Long existingArticleId) {
        var error = new ArticleErrorResponse(
                "ARTICLE_DUPLICATE",
                "Article already exists",
                existingArticleId
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
