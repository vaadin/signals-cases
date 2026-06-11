package com.example.uc5;

import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.trigger.internal.Action;
import com.vaadin.flow.component.trigger.internal.Trigger;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.JsFunction;

/**
 * Custom action that briefly flashes the target element's background.
 * <p>
 * Demonstrates the extension SPI: subclass {@link Action} and emit the JS
 * that runs when the trigger fires by returning a {@link JsFunction} from
 * {@link #toJs(Trigger)}. The target element is captured as {@code $0} so it
 * arrives on the client as a DOM reference — no {@code @JsModule} or
 * client-side registry needed.
 */
public class FlashAction extends Action {

    private static final String BODY = """
            const t=$0;
            const o=t.style.backgroundColor;
            t.style.transition='background-color 200ms';
            t.style.backgroundColor='var(--aura-yellow, gold)';
            window.setTimeout(()=>{t.style.backgroundColor=o;},220);""";

    private final Element target;

    public FlashAction(Component target) {
        this(Objects.requireNonNull(target).getElement());
    }

    public FlashAction(Element target) {
        this.target = Objects.requireNonNull(target);
    }

    public Element getTarget() {
        return target;
    }

    @Override
    protected JsFunction toJs(Trigger trigger) {
        return JsFunction.of(BODY, target);
    }
}
