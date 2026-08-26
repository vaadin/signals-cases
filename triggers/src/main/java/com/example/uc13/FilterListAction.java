package com.example.uc13;

import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.trigger.internal.Action;
import com.vaadin.flow.component.trigger.internal.Trigger;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.JsFunction;

/**
 * Custom {@link Action} that hides children of a list container whose
 * {@code textContent} doesn't contain the query produced by an
 * {@link Action.Input}. Filtering is entirely client-side — no server
 * round-trip per keystroke.
 * <p>
 * The query source is an ordinary {@code Action.Input<String>}, the same shape
 * the built-in {@code SetPropertyAction(target, name, source)} takes: a
 * {@code PropertyInput} on the search field, a handler-scoped input, or a
 * literal all work. Composing it is a single {@code source.toJs(trigger)} call
 * — {@code Action.Input#toJs} is public since 25.2 (it used to be reachable
 * only from inside the framework package; see API-GAPS.md).
 */
public class FilterListAction extends Action {

    private final Element listContainer;
    private final Action.Input<String> query;

    public FilterListAction(Component listContainer,
            Action.Input<String> query) {
        this(Objects.requireNonNull(listContainer).getElement(), query);
    }

    public FilterListAction(Element listContainer, Action.Input<String> query) {
        this.listContainer = Objects.requireNonNull(listContainer);
        this.query = Objects.requireNonNull(query);
    }

    @Override
    protected JsFunction toJs(Trigger trigger) {
        // $0 = list container element (captured), $1 = the query JsFunction
        // (invoked with event so handler-scoped inputs work too).
        return JsFunction
                .of("""
                        const q = ($1(event) || '').toLowerCase();
                        for (const row of $0.children) {
                            row.hidden = q && !row.textContent.toLowerCase().includes(q);
                        }""",
                        listContainer, query.toJs(trigger))
                .withArguments("event");
    }
}
