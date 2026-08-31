package com.yeonhx03.marketbriefapi.index.api;

import com.yeonhx03.marketbriefapi.index.application.MarketIndexUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = MarketIndexController.class)
public class MarketIndexExceptionHandler {

    @ExceptionHandler(MarketIndexUnavailableException.class)
    ResponseEntity<MarketIndexErrorResponse> handleUnavailable() {
        var error = new MarketIndexErrorResponse(
                "MARKET_INDEX_UNAVAILABLE",
                "Market indexes are temporarily unavailable"
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
}
