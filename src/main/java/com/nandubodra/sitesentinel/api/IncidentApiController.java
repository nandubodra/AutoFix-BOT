package com.nandubodra.sitesentinel.api;

import com.nandubodra.sitesentinel.incident.Incident;
import com.nandubodra.sitesentinel.incident.IncidentService;
import com.nandubodra.sitesentinel.security.CurrentUserService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentApiController {

    private final IncidentService incidentService;
    private final CurrentUserService currentUserService;

    public IncidentApiController(IncidentService incidentService, CurrentUserService currentUserService) {
        this.incidentService = incidentService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public Page<IncidentDto> list(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        return incidentService.list(currentUserService.requireCurrentUser(), page, size).map(IncidentDto::from);
    }

    @PostMapping("/{id}/ack")
    public IncidentDto acknowledge(@PathVariable Long id) {
        Incident incident = incidentService.requireOwned(id, currentUserService.requireCurrentUser());
        incidentService.acknowledge(incident);
        return IncidentDto.from(incident);
    }

    public record IncidentDto(Long id, Long websiteId, String websiteName, String status, String severity,
                              String description, long occurrences, Instant firstDetectedAt, Instant lastSeenAt,
                              Instant resolvedAt) {
        static IncidentDto from(Incident incident) {
            return new IncidentDto(
                    incident.getId(),
                    incident.getWebsite().getId(),
                    incident.getWebsite().getName(),
                    incident.getStatus().name(),
                    incident.getSeverity(),
                    incident.getDescription(),
                    incident.getOccurrences(),
                    incident.getFirstDetectedAt(),
                    incident.getLastSeenAt(),
                    incident.getResolvedAt());
        }
    }
}
