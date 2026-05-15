package com.example.uc5;

import java.util.Objects;

import tools.jackson.databind.node.ObjectNode;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.trigger.AbstractAction;
import com.vaadin.flow.component.trigger.internal.ConfigContext;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.JacksonUtils;

/**
 * Custom action that briefly flashes the target element's background.
 * <p>
 * Demonstrates the extension SPI: namespaced type id, an
 * {@link com.vaadin.flow.component.trigger.AbstractAction} subclass on the
 * server, and a matching factory registered against
 * {@code window.Vaadin.Flow.triggers} by the {@code flash-action.ts} module
 * that ships with this view.
 */
public class FlashAction extends AbstractAction {

    public static final String TYPE_ID = "demo:flash";

    private final Element target;

    public FlashAction(Component target) {
        this(Objects.requireNonNull(target).getElement());
    }

    public FlashAction(Element target) {
        super(TYPE_ID);
        this.target = Objects.requireNonNull(target);
    }

    public Element getTarget() {
        return target;
    }

    @Override
    public ObjectNode buildClientConfig(ConfigContext context) {
        ObjectNode node = JacksonUtils.createObjectNode();
        node.put("element", context.referenceElement(target));
        return node;
    }
}
