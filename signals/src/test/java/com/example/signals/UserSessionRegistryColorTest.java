package com.example.signals;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext
class UserSessionRegistryColorTest {

    @Autowired
    private UserSessionRegistry registry;

    @Test
    void thirtySessionsGetDistinctColorIndices() {
        Set<Integer> indices = new HashSet<>();
        for (int i = 0; i < 30; i++) {
            registry.registerUser("user" + i, "session" + i);
            int colorIndex = registry.getUserColorIndex("user" + i,
                    "session" + i);
            assertTrue(colorIndex >= 0 && colorIndex < UserInfo.COLOR_COUNT,
                    "Color index should be in range [0, " + UserInfo.COLOR_COUNT
                            + ") but was " + colorIndex);
            indices.add(colorIndex);
        }
        assertEquals(30, indices.size(),
                "All 30 sessions should have distinct color indices");
    }

    @Test
    void colorIndicesWrapAfterPaletteExhausted() {
        for (int i = 0; i < 31; i++) {
            registry.registerUser("wrap" + i, "s" + i);
        }
        int first = registry.getUserColorIndex("wrap0", "s0");
        int thirtyFirst = registry.getUserColorIndex("wrap30", "s30");
        assertEquals(first, thirtyFirst,
                "Color index should wrap around after palette is exhausted");
    }
}
