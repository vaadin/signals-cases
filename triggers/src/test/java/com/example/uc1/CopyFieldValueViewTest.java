package com.example.uc1;

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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = CopyFieldValueView.class)
class CopyFieldValueViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithExpectedComponents() {
        navigate(CopyFieldValueView.class);

        assertTrue(findInView(H1.class).all().stream()
                .anyMatch(h -> "UC1 — Copy field value on click"
                        .equals(h.getText())));
        assertNotNull(findInView(TextField.class).id("source"));
        assertNotNull(findInView(Button.class).id("copy"));
    }

    @Test
    void clickTriggerIsWiredToClipboardCopyActionReadingFieldValue() {
        navigate(CopyFieldValueView.class);

        Button copy = findInView(Button.class).id("copy");
        ObjectNode snapshot = TriggerSupport.on(copy).snapshotForTest();

        JsonNode triggers = snapshot.get("triggers");
        assertEquals(1, triggers.size(), "exactly one trigger");
        assertEquals(ClickTrigger.TYPE_ID,
                triggers.get("0").get("type").asString());

        JsonNode actions = snapshot.get("actions");
        assertEquals(1, actions.size(), "exactly one action");
        JsonNode actionEntry = actions.get("0");
        assertEquals(ClipboardCopyAction.TYPE_ID,
                actionEntry.get("type").asString());

        int outputId = actionEntry.get("config").get("textOutput").asInt();
        JsonNode outputs = snapshot.get("outputs");
        JsonNode outputEntry = outputs.get(Integer.toString(outputId));
        assertEquals(PropertyOutput.TYPE_ID,
                outputEntry.get("type").asString());
        assertEquals("value",
                outputEntry.get("config").get("property").asString());

        JsonNode bindings = snapshot.get("bindings");
        assertEquals(1, bindings.size());
        assertEquals(0, bindings.get(0).get("trigger").asInt());
        assertEquals(1, bindings.get(0).get("actions").size());
        assertEquals(0, bindings.get(0).get("actions").get(0).asInt());
    }
}
