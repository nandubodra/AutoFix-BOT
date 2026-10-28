package com.nandubodra.sitesentinel.monitoring;

import com.nandubodra.sitesentinel.website.Website;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "check_results")
public class CheckResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "website_id")
    private Website website;

    @Column(nullable = false)
    private boolean success;

    private Integer httpStatus;

    @Column(nullable = false)
    private long durationMs;

    @Column(nullable = false, length = 64)
    private String failureCategory;

    @Column(nullable = false, length = 512)
    private String failureSummary;

    @Column(nullable = false)
    private int retryNumber;

    @Column(nullable = false)
    private Instant checkedAt;

    public Long getId() { return id; }
    public Website getWebsite() { return website; }
    public void setWebsite(Website website) { this.website = website; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    public String getFailureCategory() { return failureCategory; }
    public void setFailureCategory(String failureCategory) { this.failureCategory = failureCategory; }
    public String getFailureSummary() { return failureSummary; }
    public void setFailureSummary(String failureSummary) { this.failureSummary = failureSummary; }
    public int getRetryNumber() { return retryNumber; }
    public void setRetryNumber(int retryNumber) { this.retryNumber = retryNumber; }
    public Instant getCheckedAt() { return checkedAt; }
    public void setCheckedAt(Instant checkedAt) { this.checkedAt = checkedAt; }
}
