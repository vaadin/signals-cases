package com.example.uc4;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Code;
import com.vaadin.flow.component.html.H1;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = ShareUrlView.class)
class ShareUrlViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithServerGeneratedUrl() {
        navigate(ShareUrlView.class);

        assertTrue(findInView(H1.class).all().stream()
                .anyMatch(h -> "UC4 — Share URL widget".equals(h.getText())));
        Code display = findInView(Code.class).id("share-url");
        assertTrue(display.getText().startsWith("https://example.com/share/"));
        assertNotNull(findInView(Button.class).id("copy"));
    }

    @Test
    void twoSessionsGetDifferentShareUrls() {
        navigate(ShareUrlView.class);
        String firstUrl = findInView(Code.class).id("share-url").getText();

        cleanVaadinEnvironment();
        initVaadinEnvironment();

        navigate(ShareUrlView.class);
        String secondUrl = findInView(Code.class).id("share-url").getText();

        assertNotEquals(firstUrl, secondUrl);
    }
}
