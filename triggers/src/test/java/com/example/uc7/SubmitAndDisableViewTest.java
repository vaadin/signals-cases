package com.example.uc7;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = SubmitAndDisableView.class)
class SubmitAndDisableViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersFieldSendButtonAndEcho() {
        navigate(SubmitAndDisableView.class);

        assertTrue(findInView(H1.class).all().stream()
                .anyMatch(h -> "UC7 — Submit on Enter, then disable"
                        .equals(h.getText())));
        assertNotNull(findInView(TextField.class).id("message"));
        assertNotNull(findInView(Button.class).id("send"));
        assertNotNull(findInView(Span.class).id("echo"));
    }

    @Test
    void clickingSendDisablesItServerSide() {
        navigate(SubmitAndDisableView.class);
        TextField field = findInView(TextField.class).id("message");
        Button send = findInView(Button.class).id("send");
        field.setValue("hello");
        test(send).click();
        assertTrue(!send.isEnabled(),
                "click listener disables the button server-side");
        assertEquals("Sent: hello",
                findInView(Span.class).id("echo").getText());
    }
}
