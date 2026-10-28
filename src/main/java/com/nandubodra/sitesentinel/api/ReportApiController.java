package com.nandubodra.sitesentinel.api;

import com.nandubodra.sitesentinel.monitoring.CheckResult;
import com.nandubodra.sitesentinel.monitoring.CheckResultRepository;
import com.nandubodra.sitesentinel.security.CurrentUserService;
import com.nandubodra.sitesentinel.website.Website;
import com.nandubodra.sitesentinel.website.WebsiteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportApiController {

    private final WebsiteService websiteService;
    private final CurrentUserService currentUserService;
    private final CheckResultRepository checkResultRepository;

    public ReportApiController(WebsiteService websiteService,
                               CurrentUserService currentUserService,
                               CheckResultRepository checkResultRepository) {
        this.websiteService = websiteService;
        this.currentUserService = currentUserService;
        this.checkResultRepository = checkResultRepository;
    }

    @GetMapping("/websites/{websiteId}/summary")
    public ReportSummary summary(@PathVariable Long websiteId,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        Website website = websiteService.getOwned(websiteId, currentUserService.requireCurrentUser());
        List<CheckResult> checks = checkResultRepository.findByWebsiteAndCheckedAtBetween(website, from, to);
        long total = checks.size();
        long success = checks.stream().filter(CheckResult::isSuccess).count();
        double uptime = total == 0 ? 0.0 : ((double) success / (double) total) * 100.0;
        double avgResponse = checks.stream().mapToLong(CheckResult::getDurationMs).average().orElse(0.0);
        return new ReportSummary(websiteId, from, to, total, success, uptime, avgResponse);
    }

    public record ReportSummary(Long websiteId, Instant from, Instant to, long totalChecks,
                                long successfulChecks, double uptimePercent, double averageResponseMs) {}
}
