package com.nandubodra.sitesentinel.monitoring;

import com.nandubodra.sitesentinel.common.MonitoringStatus;
import com.nandubodra.sitesentinel.config.SiteSentinelProperties;
import com.nandubodra.sitesentinel.incident.IncidentService;
import com.nandubodra.sitesentinel.alert.AlertService;
import com.nandubodra.sitesentinel.website.Website;
import com.nandubodra.sitesentinel.website.WebsiteRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Service
public class MonitoringService {

    private final WebsiteRepository websiteRepository;
    private final CheckResultRepository checkResultRepository;
    private final HttpCheckService httpCheckService;
    private final SiteSentinelProperties properties;
    private final ExecutorService monitorExecutor;
    private final IncidentService incidentService;
    private final AlertService alertService;
    private final Set<Long> inFlight = ConcurrentHashMap.newKeySet();

    public MonitoringService(WebsiteRepository websiteRepository,
                             CheckResultRepository checkResultRepository,
                             HttpCheckService httpCheckService,
                             SiteSentinelProperties properties,
                             ExecutorService monitorExecutor,
                             IncidentService incidentService,
                             AlertService alertService) {
        this.websiteRepository = websiteRepository;
        this.checkResultRepository = checkResultRepository;
        this.httpCheckService = httpCheckService;
        this.properties = properties;
        this.monitorExecutor = monitorExecutor;
        this.incidentService = incidentService;
        this.alertService = alertService;
    }

    @Scheduled(fixedDelayString = "${sitesentinel.monitoring.scheduler-delay-ms:5000}")
    public void scheduleDueChecks() {
        Instant now = Instant.now();
        var candidates = websiteRepository.findDueCandidates(now, PageRequest.of(0, properties.getMonitoring().getThreadPoolSize() * 4));
        for (Website candidate : candidates) {
            if (!inFlight.add(candidate.getId())) {
                continue;
            }

            boolean claimed = claim(candidate.getId(), now);
            if (!claimed) {
                inFlight.remove(candidate.getId());
                continue;
            }

            monitorExecutor.submit(() -> {
                try {
                    runCheck(candidate.getId());
                } finally {
                    inFlight.remove(candidate.getId());
                }
            });
        }
    }

    @Transactional
    public boolean claim(Long websiteId, Instant now) {
        int updated = websiteRepository.claimIfDue(websiteId, now, now.plusSeconds(properties.getMonitoring().getClaimSeconds()));
        return updated == 1;
    }

    @Transactional
    public void runCheck(Long websiteId) {
        Website website = websiteRepository.findById(websiteId).orElse(null);
        if (website == null) {
            return;
        }

        HttpCheckOutcome outcome = httpCheckService.check(website);
        CheckResult result = new CheckResult();
        result.setWebsite(website);
        result.setSuccess(outcome.success());
        result.setHttpStatus(outcome.httpStatus());
        result.setDurationMs(outcome.durationMs());
        result.setFailureCategory(outcome.failureCategory());
        result.setFailureSummary(outcome.failureSummary());
        result.setRetryNumber(0);
        result.setCheckedAt(Instant.now());
        checkResultRepository.save(result);

        website.setLastCheckAt(result.getCheckedAt());
        website.setNextCheckAt(result.getCheckedAt().plusSeconds(website.getIntervalSeconds()));
        website.setClaimUntil(null);

        if (outcome.success()) {
            website.setConsecutiveSuccesses(website.getConsecutiveSuccesses() + 1);
            website.setConsecutiveFailures(0);

            if (website.getConsecutiveSuccesses() >= website.getRecoveryThreshold()) {
                incidentService.onRecovery(website, result);
            } else if (website.getStatus() == MonitoringStatus.UNKNOWN) {
                website.setStatus(MonitoringStatus.HEALTHY);
            }
        } else {
            website.setConsecutiveFailures(website.getConsecutiveFailures() + 1);
            website.setConsecutiveSuccesses(0);
            if (website.getConsecutiveFailures() >= website.getFailureThreshold()) {
                incidentService.onConfirmedFailure(website, result);
            }
        }
        websiteRepository.save(website);
    }

    @Scheduled(fixedDelay = 60000)
    public void retryPendingAlerts() {
        alertService.retryPending();
    }
}
