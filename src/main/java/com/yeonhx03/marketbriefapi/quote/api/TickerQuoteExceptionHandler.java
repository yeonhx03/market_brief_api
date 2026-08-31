package com.yeonhx03.marketbriefapi.quote.api;

import com.yeonhx03.marketbriefapi.quote.application.TickerQuoteNotFoundException;
import com.yeonhx03.marketbriefapi.quote.application.TickerQuoteUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = TickerQuoteController.class)
public class TickerQuoteExceptionHandler {

    @ExceptionHandler(TickerQuoteNotFoundException.class)
    ResponseEntity<TickerQuoteErrorResponse> handleNotFound() {
        var error = new TickerQuoteErrorResponse(
                "TICKER_QUOTE_NOT_FOUND",
                "Ticker quote was not found"
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(TickerQuoteUnavailableException.class)
    ResponseEntity<TickerQuoteErrorResponse> handleUnavailable() {
        var error = new TickerQuoteErrorResponse(
                "TICKER_QUOTE_UNAVAILABLE",
                "Ticker quote is temporarily unavailable"
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
}
