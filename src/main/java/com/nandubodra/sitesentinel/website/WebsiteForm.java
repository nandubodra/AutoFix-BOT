package com.nandubodra.sitesentinel.website;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class WebsiteForm {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 2048)
    private String url;

    @Min(30)
    @Max(3600)
    private int intervalSeconds = 60;

    @Min(1000)
    @Max(15000)
    private int timeoutMs = 5000;

    @Pattern(regexp = "^[0-9, ]+$", message = "Expected statuses must be comma-separated codes")
    private String expectedStatuses = "200";

    @Min(1)
    @Max(10)
    private int failureThreshold = 2;

    @Min(1)
    @Max(10)
    private int recoveryThreshold = 2;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public int getIntervalSeconds() { return intervalSeconds; }
    public void setIntervalSeconds(int intervalSeconds) { this.intervalSeconds = intervalSeconds; }
    public int getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
    public String getExpectedStatuses() { return expectedStatuses; }
    public void setExpectedStatuses(String expectedStatuses) { this.expectedStatuses = expectedStatuses; }
    public int getFailureThreshold() { return failureThreshold; }
    public void setFailureThreshold(int failureThreshold) { this.failureThreshold = failureThreshold; }
    public int getRecoveryThreshold() { return recoveryThreshold; }
    public void setRecoveryThreshold(int recoveryThreshold) { this.recoveryThreshold = recoveryThreshold; }
}
