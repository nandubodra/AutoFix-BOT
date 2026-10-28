package com.nandubodra.sitesentinel.website;

import com.nandubodra.sitesentinel.monitoring.CheckResultRepository;
import com.nandubodra.sitesentinel.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/websites")
public class WebsiteController {

    private final WebsiteService websiteService;
    private final CurrentUserService currentUserService;
    private final CheckResultRepository checkResultRepository;

    public WebsiteController(WebsiteService websiteService,
                             CurrentUserService currentUserService,
                             CheckResultRepository checkResultRepository) {
        this.websiteService = websiteService;
        this.currentUserService = currentUserService;
        this.checkResultRepository = checkResultRepository;
    }

    @GetMapping
    public String list(Model model) {
        var user = currentUserService.requireCurrentUser();
        model.addAttribute("websites", websiteService.list(user));
        model.addAttribute("form", new WebsiteForm());
        return "websites/list";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") WebsiteForm form, BindingResult bindingResult, Model model) {
        var user = currentUserService.requireCurrentUser();
        if (bindingResult.hasErrors()) {
            model.addAttribute("websites", websiteService.list(user));
            return "websites/list";
        }
        try {
            websiteService.create(user, form);
            return "redirect:/websites";
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("website.create", ex.getMessage());
            model.addAttribute("websites", websiteService.list(user));
            return "websites/list";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var user = currentUserService.requireCurrentUser();
        Website website = websiteService.getOwned(id, user);
        model.addAttribute("website", website);
        model.addAttribute("history", checkResultRepository.findTop20ByWebsiteOrderByCheckedAtDesc(website));
        return "websites/detail";
    }

    @PostMapping("/{id}/verify")
    public String verify(@PathVariable Long id) {
        var user = currentUserService.requireCurrentUser();
        Website website = websiteService.getOwned(id, user);
        websiteService.verifyOwnership(website);
        return "redirect:/websites/" + id;
    }

    @PostMapping("/{id}/pause")
    public String pause(@PathVariable Long id) {
        var user = currentUserService.requireCurrentUser();
        Website website = websiteService.getOwned(id, user);
        websiteService.setMonitoring(website, false);
        return "redirect:/websites/" + id;
    }

    @PostMapping("/{id}/resume")
    public String resume(@PathVariable Long id) {
        var user = currentUserService.requireCurrentUser();
        Website website = websiteService.getOwned(id, user);
        websiteService.setMonitoring(website, true);
        return "redirect:/websites/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        var user = currentUserService.requireCurrentUser();
        Website website = websiteService.getOwned(id, user);
        websiteService.delete(website);
        return "redirect:/websites";
    }
}
