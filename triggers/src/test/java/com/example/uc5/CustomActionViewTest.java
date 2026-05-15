package com.example.uc5;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.trigger.ClickTrigger;
import com.vaadin.flow.component.trigger.internal.TriggerSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = CustomActionView.class)
class CustomActionViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersTargetAndTriggerButton() {
        navigate(CustomActionView.class);

        assertTrue($view(H1.class).all().stream()
                .anyMatch(h -> "UC5 — Custom action via @JsModule"
                        .equals(h.getText())));
        assertNotNull($view(Div.class).id("target"));
        assertNotNull($view(Button.class).id("trigger"));
    }

    @Test
    void clickIsBoundToCustomActionTypeId() {
        navigate(CustomActionView.class);

        Button trigger = $view(Button.class).id("trigger");
        ObjectNode snapshot = TriggerSupport.on(trigger).snapshotForTest();

        assertEquals(ClickTrigger.TYPE_ID,
                snapshot.get("triggers").get("0").get("type").asString());

        JsonNode action = snapshot.get("actions").get("0");
        assertEquals(FlashAction.TYPE_ID, action.get("type").asString(),
                "the action's namespaced type id is what the client factory looks up");
        assertTrue(action.get("config").get("element").asInt() >= 1,
                "the target Div is a non-host element, so it gets an extra-parameter index");

        // No outputs needed for this action.
        assertEquals(0, snapshot.get("outputs").size());

        JsonNode bindings = snapshot.get("bindings");
        assertEquals(1, bindings.size());
        assertEquals(0, bindings.get(0).get("trigger").asInt());
        assertEquals(0, bindings.get(0).get("actions").get(0).asInt());
    }
}
