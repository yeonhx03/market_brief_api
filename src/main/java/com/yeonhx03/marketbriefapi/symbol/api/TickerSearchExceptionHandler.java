package com.yeonhx03.marketbriefapi.symbol.api;

import com.yeonhx03.marketbriefapi.symbol.application.TickerSearchUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = TickerSearchController.class)
public class TickerSearchExceptionHandler {

    @ExceptionHandler(TickerSearchUnavailableException.class)
    ResponseEntity<TickerSearchErrorResponse> handleUnavailable() {
        var error = new TickerSearchErrorResponse(
                "TICKER_SEARCH_UNAVAILABLE",
                "Ticker search is temporarily unavailable"
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
}
