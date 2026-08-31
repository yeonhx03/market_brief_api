package com.yeonhx03.marketbriefapi.article.api;

import com.yeonhx03.marketbriefapi.article.application.ArticleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping
    ResponseEntity<ArticleResponse> create(@Valid @RequestBody CreateArticleRequest request) {
        var article = articleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(article);
    }

    @GetMapping("/latest")
    List<ArticleResponse> findLatest(
            @RequestParam(defaultValue = "10")
            @Min(1)
            @Max(100)
            int limit
    ) {
        return articleService.findLatest(limit);
    }
}
