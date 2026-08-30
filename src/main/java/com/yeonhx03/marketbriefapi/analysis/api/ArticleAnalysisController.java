package com.yeonhx03.marketbriefapi.analysis.api;

import com.yeonhx03.marketbriefapi.analysis.application.ArticleAnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/articles/{articleId}/analyses")
public class ArticleAnalysisController {

    private final ArticleAnalysisService analysisService;

    public ArticleAnalysisController(ArticleAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping
    ResponseEntity<ArticleAnalysisResponse> create(
            @PathVariable Long articleId,
            @Valid @RequestBody CreateArticleAnalysisRequest request
    ) {
        var analysis = analysisService.create(articleId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(analysis);
    }

    @GetMapping
    List<ArticleAnalysisResponse> findByArticleId(@PathVariable Long articleId) {
        return analysisService.findByArticleId(articleId);
    }
}
