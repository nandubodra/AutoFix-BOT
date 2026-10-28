package com.nandubodra.sitesentinel.monitoring;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HostSafetyServiceTest {

    private final HostSafetyService service = new HostSafetyService();

    @Test
    void rejectsLocalhostAndPrivateHosts() {
        assertThrows(IllegalArgumentException.class, () -> service.assertSafeHost("localhost"));
        assertThrows(IllegalArgumentException.class, () -> service.assertSafeHost("127.0.0.1"));
    }

    @Test
    void allowsPublicHost() {
        assertDoesNotThrow(() -> service.assertSafeHost("1.1.1.1"));
    }
}
