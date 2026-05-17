package com.example.uc2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.trigger.ClickTrigger;
import com.vaadin.flow.component.trigger.ClipboardCopyAction;
import com.vaadin.flow.component.trigger.PropertyOutput;
import com.vaadin.flow.component.trigger.internal.TriggerSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = CopyCodeSnippetView.class)
class CopyCodeSnippetViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithSnippetAndCopyButton() {
        navigate(CopyCodeSnippetView.class);

        assertTrue(findInView(H1.class).all().stream()
                .anyMatch(h -> "UC2 — Copy a code snippet".equals(h.getText())));
        assertNotNull(findInView(Pre.class).id("snippet"));
        assertNotNull(findInView(Button.class).id("copy"));
    }

    @Test
    void outputReadsTextContentOfPreElement() {
        navigate(CopyCodeSnippetView.class);

        Button copy = findInView(Button.class).id("copy");
        ObjectNode snapshot = TriggerSupport.on(copy).snapshotForTest();

        JsonNode actionEntry = snapshot.get("actions").get("0");
        assertEquals(ClipboardCopyAction.TYPE_ID,
                actionEntry.get("type").asString());

        int outputId = actionEntry.get("config").get("textOutput").asInt();
        JsonNode outputEntry = snapshot.get("outputs")
                .get(Integer.toString(outputId));
        assertEquals(PropertyOutput.TYPE_ID,
                outputEntry.get("type").asString());
        assertEquals("textContent",
                outputEntry.get("config").get("property").asString());

        // The pre element is not the host of the trigger, so it shows up as
        // an extra element parameter (index >= 1).
        assertTrue(outputEntry.get("config").get("element").asInt() >= 1,
                "non-host element should get an extra-parameter index");

        JsonNode triggers = snapshot.get("triggers");
        assertEquals(ClickTrigger.TYPE_ID,
                triggers.get("0").get("type").asString());
    }
}
