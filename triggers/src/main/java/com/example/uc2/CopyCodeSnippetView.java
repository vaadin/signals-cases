package com.example.uc2;

import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.trigger.ClickTrigger;
import com.vaadin.flow.component.trigger.ClipboardCopyAction;
import com.vaadin.flow.component.trigger.Output;
import com.vaadin.flow.component.trigger.PropertyOutput;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC2 — Copy a code snippet's text on click.
 * <p>
 * Same wiring as UC1, but the {@link PropertyOutput} reads the
 * {@code textContent} property of a {@code <pre>} element instead of a form
 * field's {@code value}. Demonstrates that PropertyOutput is not specific to
 * form inputs — any DOM element with a readable property works.
 */
@Route(value = "uc2", layout = MainLayout.class)
@PageTitle("UC2 — Copy code snippet")
@Menu(order = 2, title = "UC2 — Copy code snippet")
public class CopyCodeSnippetView extends VerticalLayout {

    private static final String SNIPPET = """
            new ClickTrigger(button).triggers(
                new ClipboardCopyAction(
                    new PropertyOutput<>(field, "value", String.class)));""";

    public CopyCodeSnippetView() {
        add(new H1("UC2 — Copy a code snippet"));
        add(new Paragraph(
                "PropertyOutput can read any JS property from any element. "
                        + "Here it reads the textContent of the <pre> below."));

        Pre snippet = new Pre(SNIPPET);
        snippet.setId("snippet");
        snippet.addClassName("code-snippet");

        Button copy = new Button("Copy snippet");
        copy.setId("copy");

        Output<String> text = new PropertyOutput<>(snippet, "textContent",
                String.class);
        new ClickTrigger(copy).triggers(new ClipboardCopyAction(text));

        add(snippet, copy);
    }
}
