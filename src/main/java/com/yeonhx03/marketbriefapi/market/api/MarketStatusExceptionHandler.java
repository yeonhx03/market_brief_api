package com.yeonhx03.marketbriefapi.market.api;

import com.yeonhx03.marketbriefapi.market.application.MarketStatusUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = MarketStatusController.class)
public class MarketStatusExceptionHandler {

    @ExceptionHandler(MarketStatusUnavailableException.class)
    ResponseEntity<MarketStatusErrorResponse> handleUnavailable() {
        var error = new MarketStatusErrorResponse(
                "MARKET_STATUS_UNAVAILABLE",
                "Market status is temporarily unavailable"
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
}
