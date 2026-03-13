package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * App-level smoke test that loads the full Spring context. Catches wiring
 * issues, circular dependencies, and missing config that per-view tests (with
 * isolated @ViewPackages contexts) would miss.
 */
@SpringBootTest
@WithMockUser
class ApplicationSmokeTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
    }
}
