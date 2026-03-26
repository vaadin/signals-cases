package com.example.signals;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest
@DirtiesContext
class UserSessionRegistryColorTest {

    @Autowired
    private UserSessionRegistry registry;

    @Test
    void thirtySessionsGetDistinctColors() {
        Set<String> colors = new HashSet<>();
        for (int i = 0; i < 30; i++) {
            registry.registerUser("user" + i, "session" + i);
            String color = registry.getUserColor("user" + i, "session" + i);
            assertNotEquals("#9E9E9E", color,
                    "Session " + i + " should have an assigned color");
            colors.add(color);
        }
        assertEquals(30, colors.size(),
                "All 30 sessions should have distinct colors");
    }

    @Test
    void colorsWrapAfterPaletteExhausted() {
        for (int i = 0; i < 31; i++) {
            registry.registerUser("wrap" + i, "s" + i);
        }
        String first = registry.getUserColor("wrap0", "s0");
        String thirtyFirst = registry.getUserColor("wrap30", "s30");
        assertEquals(first, thirtyFirst,
                "Color should wrap around after palette is exhausted");
    }
}
