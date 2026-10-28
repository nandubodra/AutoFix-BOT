package com.nandubodra.sitesentinel.dashboard;

import com.nandubodra.sitesentinel.alert.AlertDeliveryRepository;
import com.nandubodra.sitesentinel.alert.DeliveryStatus;
import com.nandubodra.sitesentinel.common.MonitoringStatus;
import com.nandubodra.sitesentinel.incident.IncidentRepository;
import com.nandubodra.sitesentinel.incident.IncidentStatus;
import com.nandubodra.sitesentinel.security.CurrentUserService;
import com.nandubodra.sitesentinel.website.Website;
import com.nandubodra.sitesentinel.website.WebsiteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final CurrentUserService currentUserService;
    private final WebsiteService websiteService;
    private final IncidentRepository incidentRepository;
    private final AlertDeliveryRepository alertDeliveryRepository;

    public DashboardController(CurrentUserService currentUserService,
                               WebsiteService websiteService,
                               IncidentRepository incidentRepository,
                               AlertDeliveryRepository alertDeliveryRepository) {
        this.currentUserService = currentUserService;
        this.websiteService = websiteService;
        this.incidentRepository = incidentRepository;
        this.alertDeliveryRepository = alertDeliveryRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        var user = currentUserService.requireCurrentUser();
        List<Website> websites = websiteService.list(user);
        long paused = websites.stream().filter(w -> !w.isMonitoringEnabled() || w.getStatus() == MonitoringStatus.PAUSED).count();
        long healthy = websites.stream().filter(w -> w.getStatus() == MonitoringStatus.HEALTHY).count();
        long activeIncidents = incidentRepository.countByWebsiteOwnerAndStatusIn(user, List.of(IncidentStatus.OPEN, IncidentStatus.ACKNOWLEDGED));

        model.addAttribute("totalSites", websites.size());
        model.addAttribute("pausedSites", paused);
        model.addAttribute("healthySites", healthy);
        model.addAttribute("incidentSites", activeIncidents);
        model.addAttribute("sites", websites.stream().limit(10).toList());
        model.addAttribute("failedAlerts", alertDeliveryRepository.findTop10ByStatusOrderByCreatedAtDesc(DeliveryStatus.FAILED));
        return "dashboard";
    }
}
