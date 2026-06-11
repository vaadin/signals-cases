package com.example.uc2;

import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.trigger.internal.Action;
import com.vaadin.flow.component.trigger.internal.ClickTrigger;
import com.vaadin.flow.component.trigger.internal.PropertyInput;
import com.vaadin.flow.component.trigger.internal.WriteToClipboardAction;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC2 — Copy a code snippet's text on click.
 * <p>
 * Same wiring as UC1, but the {@link PropertyInput} reads the
 * {@code textContent} property of a {@code <pre>} element instead of a form
 * field's {@code value}. Demonstrates that PropertyInput is not specific to
 * form inputs — any DOM element with a readable property works.
 */
@Route(value = "uc2", layout = MainLayout.class)
@PageTitle("UC2 — Copy code snippet")
@Menu(order = 2, title = "UC2 — Copy code snippet")
@StyleSheet("uc2.css")
public class CopyCodeSnippetView extends VerticalLayout {

    private static final String SNIPPET = """
            new ClickTrigger(button).triggers(
                new WriteToClipboardAction(
                    new PropertyInput<>(field, "value", String.class), null));""";

    public CopyCodeSnippetView() {
        addClassName("uc2-view");
        add(new H1("UC2 — Copy a code snippet"));
        add(new Paragraph(
                "PropertyInput can read any JS property from any element. "
                        + "Here it reads the textContent of the <pre> below."));

        Pre snippet = new Pre(SNIPPET);
        snippet.setId("snippet");
        snippet.addClassName("code-snippet");

        Button copy = new Button("Copy snippet");
        copy.setId("copy");

        Action.Input<String> text = new PropertyInput<>(snippet, "textContent",
                String.class);
        new ClickTrigger(copy)
                .triggers(new WriteToClipboardAction(text, null));

        add(snippet, copy);
    }
}
