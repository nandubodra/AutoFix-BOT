package com.nandubodra.sitesentinel.incident;

import com.nandubodra.sitesentinel.alert.AlertService;
import com.nandubodra.sitesentinel.auth.UserAccount;
import com.nandubodra.sitesentinel.common.MonitoringStatus;
import com.nandubodra.sitesentinel.monitoring.CheckResult;
import com.nandubodra.sitesentinel.website.Website;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final AlertService alertService;

    public IncidentService(IncidentRepository incidentRepository, AlertService alertService) {
        this.incidentRepository = incidentRepository;
        this.alertService = alertService;
    }

    @Transactional
    public void onConfirmedFailure(Website website, CheckResult checkResult) {
        String fingerprint = fingerprint(checkResult);
        Incident incident = incidentRepository.findFirstByWebsiteAndStatusInAndFingerprintOrderByLastSeenAtDesc(
                        website, List.of(IncidentStatus.OPEN, IncidentStatus.ACKNOWLEDGED), fingerprint)
                .orElseGet(() -> {
                    Incident created = new Incident();
                    created.setWebsite(website);
                    created.setStatus(IncidentStatus.OPEN);
                    created.setFingerprint(fingerprint);
                    created.setSeverity("HIGH");
                    created.setDescription(sanitize(checkResult.getFailureSummary()));
                    created.setFirstDetectedAt(checkResult.getCheckedAt());
                    created.setOccurrences(0);
                    created.setFirstCheckResult(checkResult);
                    return created;
                });

        incident.setLastSeenAt(checkResult.getCheckedAt());
        incident.setOccurrences(incident.getOccurrences() + 1);
        incident.setLastCheckResult(checkResult);
        incidentRepository.save(incident);

        website.setStatus(MonitoringStatus.INCIDENT);

        if (incident.getOccurrences() == 1) {
            alertService.sendIncidentAlert(incident);
        }
    }

    @Transactional
    public void onRecovery(Website website, CheckResult checkResult) {
        List<IncidentStatus> activeStatuses = List.of(IncidentStatus.OPEN, IncidentStatus.ACKNOWLEDGED);
        Incident incident = incidentRepository.findFirstByWebsiteAndStatusInAndFingerprintOrderByLastSeenAtDesc(
                website, activeStatuses, fingerprint(checkResult)).orElse(null);

        if (incident == null) {
            incident = incidentRepository.findByWebsiteOwnerOrderByLastSeenAtDesc(website.getOwner(), PageRequest.of(0, 50))
                    .stream().filter(i -> i.getWebsite().getId().equals(website.getId()) && activeStatuses.contains(i.getStatus()))
                    .findFirst().orElse(null);
        }

        if (incident != null) {
            incident.setStatus(IncidentStatus.RESOLVED);
            incident.setResolvedAt(checkResult.getCheckedAt());
            incident.setLastSeenAt(checkResult.getCheckedAt());
            incident.setLastCheckResult(checkResult);
            incidentRepository.save(incident);
            alertService.sendRecoveryAlert(incident);
        }

        website.setStatus(MonitoringStatus.HEALTHY);
    }

    public Page<Incident> list(UserAccount owner, int page, int size) {
        return incidentRepository.findByWebsiteOwnerOrderByLastSeenAtDesc(owner, PageRequest.of(page, size));
    }

    public Incident requireOwned(Long id, UserAccount owner) {
        return incidentRepository.findByIdAndWebsiteOwner(id, owner)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found"));
    }

    @Transactional
    public void acknowledge(Incident incident) {
        if (incident.getStatus() == IncidentStatus.OPEN) {
            incident.setStatus(IncidentStatus.ACKNOWLEDGED);
            incident.setAcknowledgedAt(Instant.now());
            incidentRepository.save(incident);
        }
    }

    private String fingerprint(CheckResult result) {
        return result.getFailureCategory() + ":" + result.getFailureSummary();
    }

    private String sanitize(String message) {
        if (message == null) {
            return "Unknown failure";
        }
        String value = message.replaceAll("[\r\n]", " ").trim();
        if (value.length() > 500) {
            value = value.substring(0, 500);
        }
        return value;
    }
}
