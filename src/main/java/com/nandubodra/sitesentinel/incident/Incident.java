package com.nandubodra.sitesentinel.incident;

import com.nandubodra.sitesentinel.monitoring.CheckResult;
import com.nandubodra.sitesentinel.website.Website;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "website_id")
    private Website website;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private IncidentStatus status;

    @Column(nullable = false, length = 128)
    private String fingerprint;

    @Column(nullable = false, length = 16)
    private String severity;

    @Column(nullable = false, length = 512)
    private String description;

    @Column(nullable = false)
    private Instant firstDetectedAt;

    @Column(nullable = false)
    private Instant lastSeenAt;

    private Instant acknowledgedAt;

    private Instant resolvedAt;

    @Column(nullable = false)
    private long occurrences;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_check_result_id")
    private CheckResult firstCheckResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_check_result_id")
    private CheckResult lastCheckResult;

    public Long getId() { return id; }
    public Website getWebsite() { return website; }
    public void setWebsite(Website website) { this.website = website; }
    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }
    public String getFingerprint() { return fingerprint; }
    public void setFingerprint(String fingerprint) { this.fingerprint = fingerprint; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getFirstDetectedAt() { return firstDetectedAt; }
    public void setFirstDetectedAt(Instant firstDetectedAt) { this.firstDetectedAt = firstDetectedAt; }
    public Instant getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(Instant lastSeenAt) { this.lastSeenAt = lastSeenAt; }
    public Instant getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(Instant acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
    public long getOccurrences() { return occurrences; }
    public void setOccurrences(long occurrences) { this.occurrences = occurrences; }
    public CheckResult getFirstCheckResult() { return firstCheckResult; }
    public void setFirstCheckResult(CheckResult firstCheckResult) { this.firstCheckResult = firstCheckResult; }
    public CheckResult getLastCheckResult() { return lastCheckResult; }
    public void setLastCheckResult(CheckResult lastCheckResult) { this.lastCheckResult = lastCheckResult; }
}
