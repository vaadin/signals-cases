package com.example.uc4;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.trigger.ClickTrigger;
import com.vaadin.flow.component.trigger.ClipboardCopyAction;
import com.vaadin.flow.component.trigger.PropertyOutput;
import com.vaadin.flow.component.trigger.internal.TriggerSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = ShareUrlView.class)
class ShareUrlViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithServerGeneratedUrl() {
        navigate(ShareUrlView.class);

        assertTrue(findInView(H1.class).all().stream()
                .anyMatch(h -> "UC4 — Share URL widget".equals(h.getText())));

        TextField field = findInView(TextField.class).id("share-url");
        assertTrue(field.getValue().startsWith("https://example.com/share/"),
                "server should render the share URL into the field");
        assertTrue(field.isReadOnly(), "share URL is read-only");
    }

    @Test
    void copyButtonIsWiredToReadFieldValue() {
        navigate(ShareUrlView.class);

        Button copy = findInView(Button.class).id("copy");
        ObjectNode snapshot = TriggerSupport.on(copy).snapshotForTest();

        assertEquals(ClickTrigger.TYPE_ID,
                snapshot.get("triggers").get("0").get("type").asString());

        JsonNode action = snapshot.get("actions").get("0");
        assertEquals(ClipboardCopyAction.TYPE_ID,
                action.get("type").asString());

        int outputId = action.get("config").get("textOutput").asInt();
        JsonNode output = snapshot.get("outputs")
                .get(Integer.toString(outputId));
        assertEquals(PropertyOutput.TYPE_ID, output.get("type").asString());
        assertEquals("value", output.get("config").get("property").asString());
    }

    @Test
    void twoSessionsGetDifferentShareUrls() {
        ShareUrlView first = navigate(ShareUrlView.class);
        String firstUrl = findInView(TextField.class).id("share-url").getValue();

        cleanVaadinEnvironment();
        initVaadinEnvironment();

        ShareUrlView second = navigate(ShareUrlView.class);
        String secondUrl = findInView(TextField.class).id("share-url").getValue();

        assertTrue(!firstUrl.equals(secondUrl),
                "each session should generate its own share URL");
        // Touch the view instances so the compiler doesn't drop the
        // navigate calls' return values entirely.
        assertTrue(first != second);
    }
}
