package com.example.uc3;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.trigger.ClickTrigger;
import com.vaadin.flow.component.trigger.ClipboardCopyAction;
import com.vaadin.flow.component.trigger.PropertyOutput;
import com.vaadin.flow.component.trigger.internal.TriggerSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = CopySelectValueView.class)
class CopySelectValueViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersHeadingAndCopyButton() {
        navigate(CopySelectValueView.class);

        assertTrue($view(H1.class).all().stream()
                .anyMatch(h -> "UC3 — Copy the currently selected option"
                        .equals(h.getText())));
        assertNotNull($view(Button.class).id("copy"));
    }

    @Test
    void outputReadsValueOfNativeSelect() {
        navigate(CopySelectValueView.class);

        Button copy = $view(Button.class).id("copy");
        ObjectNode snapshot = TriggerSupport.on(copy).snapshotForTest();

        JsonNode action = snapshot.get("actions").get("0");
        assertEquals(ClipboardCopyAction.TYPE_ID,
                action.get("type").asString());

        int outputId = action.get("config").get("textOutput").asInt();
        JsonNode output = snapshot.get("outputs")
                .get(Integer.toString(outputId));
        assertEquals(PropertyOutput.TYPE_ID, output.get("type").asString());
        assertEquals("value", output.get("config").get("property").asString());
        assertTrue(output.get("config").get("element").asInt() >= 1,
                "select is a non-host element, should get an extra index");

        assertEquals(ClickTrigger.TYPE_ID,
                snapshot.get("triggers").get("0").get("type").asString());
    }
}
