package com.nandubodra.sitesentinel.incident;

import com.nandubodra.sitesentinel.alert.AlertService;
import com.nandubodra.sitesentinel.auth.UserAccount;
import com.nandubodra.sitesentinel.common.MonitoringStatus;
import com.nandubodra.sitesentinel.monitoring.CheckResult;
import com.nandubodra.sitesentinel.website.Website;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IncidentServiceTest {

    @Test
    void groupsRepeatedFailuresByFingerprint() {
        IncidentRepository repository = mock(IncidentRepository.class);
        AlertService alertService = mock(AlertService.class);
        IncidentService service = new IncidentService(repository, alertService);

        Website website = website();
        CheckResult result = failure(website, "STATUS", "Unexpected HTTP status");

        when(repository.findFirstByWebsiteAndStatusInAndFingerprintOrderByLastSeenAtDesc(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.onConfirmedFailure(website, result);

        ArgumentCaptor<Incident> captor = ArgumentCaptor.forClass(Incident.class);
        verify(repository).save(captor.capture());
        assertEquals(1, captor.getValue().getOccurrences());
        assertEquals(MonitoringStatus.INCIDENT, website.getStatus());
    }

    @Test
    void resolvesIncidentOnRecovery() {
        IncidentRepository repository = mock(IncidentRepository.class);
        AlertService alertService = mock(AlertService.class);
        IncidentService service = new IncidentService(repository, alertService);

        Website website = website();
        Incident incident = new Incident();
        incident.setWebsite(website);
        incident.setStatus(IncidentStatus.OPEN);

        CheckResult recovery = failure(website, "NONE", "OK");

        when(repository.findFirstByWebsiteAndStatusInAndFingerprintOrderByLastSeenAtDesc(any(), any(), any()))
                .thenReturn(Optional.of(incident));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.onRecovery(website, recovery);

        assertEquals(IncidentStatus.RESOLVED, incident.getStatus());
        assertEquals(MonitoringStatus.HEALTHY, website.getStatus());
    }

    private Website website() {
        UserAccount owner = new UserAccount();
        owner.setEmail("a@example.com");

        Website website = new Website();
        website.setOwner(owner);
        website.setName("Site");
        website.setNormalizedUrl("https://example.com/");
        website.setVerificationToken("x");
        website.setVerified(true);
        website.setStatus(MonitoringStatus.UNKNOWN);
        return website;
    }

    private CheckResult failure(Website website, String category, String summary) {
        CheckResult result = new CheckResult();
        result.setWebsite(website);
        result.setFailureCategory(category);
        result.setFailureSummary(summary);
        result.setCheckedAt(Instant.now());
        return result;
    }
}
