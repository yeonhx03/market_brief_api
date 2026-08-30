package com.yeonhx03.marketbriefapi.analysis.application;

public class DuplicateArticleAnalysisException extends RuntimeException {

    private final Long existingAnalysisId;

    public DuplicateArticleAnalysisException(Long existingAnalysisId) {
        super("Article analysis already exists");
        this.existingAnalysisId = existingAnalysisId;
    }

    public Long getExistingAnalysisId() {
        return existingAnalysisId;
    }
}
