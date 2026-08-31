package com.yeonhx03.marketbriefapi.insight.api;

import com.yeonhx03.marketbriefapi.insight.application.TickerInsightNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = TickerInsightController.class)
public class TickerInsightExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<TickerInsightErrorResponse> handleInvalidRequest() {
        var error = new TickerInsightErrorResponse(
                "INVALID_TICKER_INSIGHT_REQUEST",
                "Ticker insight request is invalid"
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(TickerInsightNotFoundException.class)
    ResponseEntity<TickerInsightErrorResponse> handleNotFound() {
        var error = new TickerInsightErrorResponse(
                "TICKER_INSIGHT_NOT_FOUND",
                "Ticker insight was not found"
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
