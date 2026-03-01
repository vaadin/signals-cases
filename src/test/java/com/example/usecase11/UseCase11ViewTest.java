package com.example.usecase11;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = UseCase11View.class)
@WithMockUser
class UseCase11ViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithSplitLayout() {
        navigate(UseCase11View.class);
        assertEquals(1, $view(SplitLayout.class).all().size());
    }

    @Test
    void initialContainerSizeIsMedium() {
        navigate(UseCase11View.class);
        runPendingSignalsTasks();

        // Default container size is 600x400 which is medium (400-700px)
        // The medium layout section should be visible, small and large should not
        assertTrue($view(H3.class).all().stream()
                .anyMatch(h -> h.getText() != null
                        && h.getText().contains("Medium Width Layout")));
        assertFalse($view(H3.class).all().stream()
                .anyMatch(h -> h.getText() != null
                        && h.getText().contains("Small Width Layout")));
        assertFalse($view(H3.class).all().stream()
                .anyMatch(h -> h.getText() != null
                        && h.getText().contains("Large Width Layout")));
    }
}
