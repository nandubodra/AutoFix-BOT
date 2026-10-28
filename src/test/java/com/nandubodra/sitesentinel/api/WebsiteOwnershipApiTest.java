package com.nandubodra.sitesentinel.api;

import com.nandubodra.sitesentinel.auth.UserAccount;
import com.nandubodra.sitesentinel.auth.UserRepository;
import com.nandubodra.sitesentinel.website.Website;
import com.nandubodra.sitesentinel.website.WebsiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WebsiteOwnershipApiTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    WebsiteRepository websiteRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private UserAccount owner;
    private UserAccount other;
    private Website ownerWebsite;

    @BeforeEach
    void setup() {
        websiteRepository.deleteAll();
        userRepository.deleteAll();

        owner = new UserAccount();
        owner.setEmail("owner@example.com");
        owner.setPasswordHash(passwordEncoder.encode("StrongPassword12"));
        owner = userRepository.save(owner);

        other = new UserAccount();
        other.setEmail("other@example.com");
        other.setPasswordHash(passwordEncoder.encode("StrongPassword12"));
        other = userRepository.save(other);

        ownerWebsite = new Website();
        ownerWebsite.setOwner(owner);
        ownerWebsite.setName("Owner Site");
        ownerWebsite.setNormalizedUrl("https://example.com/");
        ownerWebsite.setVerified(true);
        ownerWebsite.setVerificationToken("token");
        ownerWebsite.setMonitoringEnabled(true);
        ownerWebsite.setIntervalSeconds(60);
        ownerWebsite.setTimeoutMs(5000);
        ownerWebsite.setExpectedStatuses("200");
        ownerWebsite.setFailureThreshold(2);
        ownerWebsite.setRecoveryThreshold(2);
        ownerWebsite.setNextCheckAt(Instant.now());
        ownerWebsite = websiteRepository.save(ownerWebsite);
    }

    @Test
    void listReturnsOnlyOwnedWebsites() throws Exception {
        mockMvc.perform(get("/api/v1/websites").with(SecurityMockMvcRequestPostProcessors.user("other@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void updateOtherUsersWebsiteFails() throws Exception {
        String body = """
                {
                  "name":"x",
                  "url":"https://example.com",
                  "intervalSeconds":60,
                  "timeoutMs":5000,
                  "expectedStatuses":"200",
                  "failureThreshold":2,
                  "recoveryThreshold":2
                }
                """;

        mockMvc.perform(put("/api/v1/websites/{id}", ownerWebsite.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(SecurityMockMvcRequestPostProcessors.user("other@example.com").roles("USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());
    }
}
