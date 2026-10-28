package com.nandubodra.sitesentinel.api;

import com.nandubodra.sitesentinel.security.CurrentUserService;
import com.nandubodra.sitesentinel.monitoring.CheckResultRepository;
import com.nandubodra.sitesentinel.website.Website;
import com.nandubodra.sitesentinel.website.WebsiteForm;
import com.nandubodra.sitesentinel.website.WebsiteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/websites")
public class WebsiteApiController {

    private final WebsiteService websiteService;
    private final CurrentUserService currentUserService;
    private final CheckResultRepository checkResultRepository;

    public WebsiteApiController(WebsiteService websiteService,
                                CurrentUserService currentUserService,
                                CheckResultRepository checkResultRepository) {
        this.websiteService = websiteService;
        this.currentUserService = currentUserService;
        this.checkResultRepository = checkResultRepository;
    }

    @GetMapping
    public List<WebsiteDto> list() {
        return websiteService.list(currentUserService.requireCurrentUser()).stream().map(WebsiteDto::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WebsiteDto create(@Valid @RequestBody WebsiteForm form) {
        Website website = websiteService.create(currentUserService.requireCurrentUser(), form);
        return WebsiteDto.from(website);
    }

    @PutMapping("/{id}")
    public WebsiteDto update(@PathVariable Long id, @Valid @RequestBody WebsiteForm form) {
        Website website = websiteService.getOwned(id, currentUserService.requireCurrentUser());
        return WebsiteDto.from(websiteService.update(website, form));
    }

    @PostMapping("/{id}/pause")
    public WebsiteDto pause(@PathVariable Long id) {
        Website website = websiteService.getOwned(id, currentUserService.requireCurrentUser());
        websiteService.setMonitoring(website, false);
        return WebsiteDto.from(website);
    }

    @PostMapping("/{id}/resume")
    public WebsiteDto resume(@PathVariable Long id) {
        Website website = websiteService.getOwned(id, currentUserService.requireCurrentUser());
        websiteService.setMonitoring(website, true);
        return WebsiteDto.from(website);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Website website = websiteService.getOwned(id, currentUserService.requireCurrentUser());
        websiteService.delete(website);
    }

    @GetMapping("/{id}/checks")
    public Page<CheckDto> checks(@PathVariable Long id,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "20") int size) {
        Website website = websiteService.getOwned(id, currentUserService.requireCurrentUser());
        return checkResultRepository.findByWebsiteOrderByCheckedAtDesc(website, PageRequest.of(page, size)).map(CheckDto::from);
    }

    public record WebsiteDto(Long id, String name, String url, boolean verified, boolean monitoringEnabled,
                             String status, Instant lastCheckAt, Instant nextCheckAt) {
        static WebsiteDto from(Website website) {
            return new WebsiteDto(
                    website.getId(),
                    website.getName(),
                    website.getNormalizedUrl(),
                    website.isVerified(),
                    website.isMonitoringEnabled(),
                    website.getStatus().name(),
                    website.getLastCheckAt(),
                    website.getNextCheckAt()
            );
        }
    }

    public record CheckDto(Long id, boolean success, Integer httpStatus, long durationMs, String failureCategory,
                           String failureSummary, int retryNumber, Instant checkedAt) {
        static CheckDto from(com.nandubodra.sitesentinel.monitoring.CheckResult result) {
            return new CheckDto(
                    result.getId(),
                    result.isSuccess(),
                    result.getHttpStatus(),
                    result.getDurationMs(),
                    result.getFailureCategory(),
                    result.getFailureSummary(),
                    result.getRetryNumber(),
                    result.getCheckedAt());
        }
    }
}
