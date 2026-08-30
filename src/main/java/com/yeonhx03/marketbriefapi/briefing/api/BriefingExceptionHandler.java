package com.yeonhx03.marketbriefapi.briefing.api;

import com.yeonhx03.marketbriefapi.briefing.application.BriefingNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = BriefingController.class)
public class BriefingExceptionHandler {

    @ExceptionHandler(BriefingNotFoundException.class)
    ResponseEntity<BriefingErrorResponse> handleBriefingNotFound(
            BriefingNotFoundException exception
    ) {
        var error = new BriefingErrorResponse(
                "BRIEFING_NOT_FOUND",
                exception.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
