package com.yeonhx03.marketbriefapi.briefing.api;

import com.yeonhx03.marketbriefapi.briefing.application.BriefingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/briefings")
public class BriefingController {

    private final BriefingService briefingService;

    public BriefingController(BriefingService briefingService) {
        this.briefingService = briefingService;
    }

    @PostMapping
    ResponseEntity<BriefingResponse> create(
            @Valid @RequestBody CreateBriefingRequest request
    ) {
        var briefing = briefingService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(briefing);
    }

    @GetMapping("/latest")
    BriefingResponse findLatest() {
        return briefingService.findLatest();
    }
}
