package com.nandubodra.sitesentinel.website;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UrlValidationServiceTest {

    private final UrlValidationService service = new UrlValidationService();

    @Test
    void normalizesHttpsUrl() {
        String normalized = service.normalizeHttpsUrl("https://Example.com/path?q=1");
        assertEquals("https://example.com/path?q=1", normalized);
    }

    @Test
    void rejectsNonHttps() {
        assertThrows(IllegalArgumentException.class, () -> service.normalizeHttpsUrl("http://example.com"));
    }

    @Test
    void rejectsUserInfoAndFragments() {
        assertThrows(IllegalArgumentException.class, () -> service.normalizeHttpsUrl("https://user@example.com"));
        assertThrows(IllegalArgumentException.class, () -> service.normalizeHttpsUrl("https://example.com/#frag"));
    }
}
