package com.nandubodra.sitesentinel.monitoring;

public record HttpCheckOutcome(boolean success, String failureCategory, String failureSummary, long durationMs, Integer httpStatus) {

    public static HttpCheckOutcome success(long durationMs, Integer httpStatus) {
        return new HttpCheckOutcome(true, "NONE", "OK", durationMs, httpStatus);
    }

    public static HttpCheckOutcome failed(String category, String summary, long durationMs, Integer httpStatus) {
        return new HttpCheckOutcome(false, category, summary, durationMs, httpStatus);
    }
}
