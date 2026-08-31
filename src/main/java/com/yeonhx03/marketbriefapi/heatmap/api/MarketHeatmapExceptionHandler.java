package com.yeonhx03.marketbriefapi.heatmap.api;

import com.yeonhx03.marketbriefapi.heatmap.application.MarketHeatmapUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = MarketHeatmapController.class)
public class MarketHeatmapExceptionHandler {

    @ExceptionHandler(MarketHeatmapUnavailableException.class)
    ResponseEntity<MarketHeatmapErrorResponse> handleUnavailable() {
        var error = new MarketHeatmapErrorResponse(
                "MARKET_HEATMAP_UNAVAILABLE",
                "Market heatmap is temporarily unavailable"
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
}
