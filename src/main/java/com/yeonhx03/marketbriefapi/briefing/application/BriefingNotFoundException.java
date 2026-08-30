package com.yeonhx03.marketbriefapi.briefing.application;

public class BriefingNotFoundException extends RuntimeException {

    public BriefingNotFoundException() {
        super("Briefing not found");
    }
}
