package com.example.uc1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = UpdateWhenActiveView.class)
class UpdateWhenActiveViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithExpectedHeadings() {
        navigate(UpdateWhenActiveView.class);

        assertTrue($view(H1.class).all().stream()
                .anyMatch(h -> "UC1 — Update when active".equals(h.getText())));
        assertTrue($view(H2.class).all().stream()
                .anyMatch(h -> "Status".equals(h.getText())));
    }
}
