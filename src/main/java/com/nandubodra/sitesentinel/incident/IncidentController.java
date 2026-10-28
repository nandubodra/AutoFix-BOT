package com.nandubodra.sitesentinel.incident;

import com.nandubodra.sitesentinel.security.CurrentUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/incidents")
public class IncidentController {

    private final IncidentService incidentService;
    private final CurrentUserService currentUserService;

    public IncidentController(IncidentService incidentService, CurrentUserService currentUserService) {
        this.incidentService = incidentService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        var user = currentUserService.requireCurrentUser();
        model.addAttribute("incidents", incidentService.list(user, page, 20));
        return "incidents/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var user = currentUserService.requireCurrentUser();
        model.addAttribute("incident", incidentService.requireOwned(id, user));
        return "incidents/detail";
    }

    @PostMapping("/{id}/ack")
    public String acknowledge(@PathVariable Long id) {
        var user = currentUserService.requireCurrentUser();
        incidentService.acknowledge(incidentService.requireOwned(id, user));
        return "redirect:/incidents/" + id;
    }
}
