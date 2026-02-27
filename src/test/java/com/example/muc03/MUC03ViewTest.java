package com.example.muc03;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = MUC03View.class)
@WithMockUser
class MUC03ViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithGameControls() {
        navigate(MUC03View.class);

        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> "START ROUND".equals(b.getText())));
        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> "Reset Scores".equals(b.getText())));
    }

    @Test
    void initialStatusShowsClickStart() {
        navigate(MUC03View.class);
        runPendingSignalsTasks();

        assertTrue($view(Div.class).all().stream()
                .anyMatch(d -> "Click START to begin".equals(d.getText())));
    }
}
