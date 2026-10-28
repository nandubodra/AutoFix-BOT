package com.nandubodra.sitesentinel.website;

import com.nandubodra.sitesentinel.auth.UserAccount;
import com.nandubodra.sitesentinel.common.MonitoringStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class WebsiteService {

    private final WebsiteRepository websiteRepository;
    private final UrlValidationService urlValidationService;
    private final VerificationService verificationService;

    public WebsiteService(WebsiteRepository websiteRepository,
                          UrlValidationService urlValidationService,
                          VerificationService verificationService) {
        this.websiteRepository = websiteRepository;
        this.urlValidationService = urlValidationService;
        this.verificationService = verificationService;
    }

    public List<Website> list(UserAccount owner) {
        return websiteRepository.findByOwnerOrderByCreatedAtDesc(owner);
    }

    public Page<Website> listPage(UserAccount owner, int page, int size) {
        return websiteRepository.findByOwner(owner, PageRequest.of(page, size));
    }

    public Website getOwned(Long id, UserAccount owner) {
        return websiteRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new IllegalArgumentException("Website not found"));
    }

    @Transactional
    public Website create(UserAccount owner, WebsiteForm form) {
        Website website = new Website();
        website.setOwner(owner);
        apply(website, form);
        website.setVerificationToken(verificationService.createToken());
        website.setMonitoringEnabled(true);
        website.setStatus(MonitoringStatus.UNKNOWN);
        website.setNextCheckAt(Instant.now());
        return websiteRepository.save(website);
    }

    @Transactional
    public Website update(Website website, WebsiteForm form) {
        String old = website.getNormalizedUrl();
        apply(website, form);
        if (!old.equals(website.getNormalizedUrl())) {
            website.setVerified(false);
            website.setVerificationToken(verificationService.createToken());
        }
        return websiteRepository.save(website);
    }

    @Transactional
    public void setMonitoring(Website website, boolean enabled) {
        website.setMonitoringEnabled(enabled);
        website.setStatus(enabled ? MonitoringStatus.UNKNOWN : MonitoringStatus.PAUSED);
        website.setNextCheckAt(Instant.now());
        websiteRepository.save(website);
    }

    @Transactional
    public void delete(Website website) {
        websiteRepository.delete(website);
    }

    @Transactional
    public boolean verifyOwnership(Website website) {
        boolean verified = verificationService.verify(website);
        website.setVerified(verified);
        websiteRepository.save(website);
        return verified;
    }

    private void apply(Website website, WebsiteForm form) {
        website.setName(form.getName().trim());
        website.setNormalizedUrl(urlValidationService.normalizeHttpsUrl(form.getUrl()));
        website.setIntervalSeconds(form.getIntervalSeconds());
        website.setTimeoutMs(form.getTimeoutMs());
        website.setExpectedStatuses(form.getExpectedStatuses().replace(" ", ""));
        website.setFailureThreshold(form.getFailureThreshold());
        website.setRecoveryThreshold(form.getRecoveryThreshold());
    }
}
