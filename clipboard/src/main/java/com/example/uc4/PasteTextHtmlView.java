package com.example.uc4;

import com.example.views.MainLayout;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.clipboard.Clipboard;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC4 — Paste text and HTML.
 * <p>
 * Listens for paste (Ctrl/Cmd-V) on a text area and shows both the plain-text
 * and HTML payloads. When something is pasted from a rich source (a web page,
 * a document) {@link com.vaadin.flow.component.clipboard.ClipboardEvent#hasHtml}
 * returns {@code true} and the HTML preview lights up; plain pastes only
 * update the text view.
 */
@Route(value = "uc4", layout = MainLayout.class)
@PageTitle("UC4 — Paste text and HTML")
@Menu(order = 4, title = "UC4 — Paste text/HTML")
public class PasteTextHtmlView extends VerticalLayout {

    public PasteTextHtmlView() {
        add(new H1("UC4 — Paste text and HTML"));
        add(new Paragraph(
                "Paste plain text or rich content (try copying from a web "
                        + "page) into the text area below. The plain-text and "
                        + "HTML branches of the paste event are shown separately."));

        TextArea editor = new TextArea("Paste here");
        editor.setWidthFull();
        editor.setHeight("150px");
        editor.setPlaceholder("Try Ctrl+V / Cmd+V");

        Span textOut = monospaceBlock("Plain text will appear here…");
        Span htmlSource = monospaceBlock("HTML source will appear here…");

        IFrame htmlPreview = new IFrame();
        htmlPreview.setSandbox(IFrame.SandboxType.RESTRICT_ALL);
        htmlPreview.setWidthFull();
        htmlPreview.setHeight("150px");
        htmlPreview.getStyle()
                .set("border", "1px solid var(--aura-contrast-20pct)")
                .set("border-radius", "var(--aura-border-radius-m)")
                .set("background", "var(--aura-base-color)");

        Clipboard.addPasteListener(editor, event -> {
            if (event.hasText()) {
                textOut.setText("Pasted text:\n" + event.getText());
            }
            if (event.hasHtml()) {
                htmlSource.setText(event.getHtml());
                htmlPreview.getElement().setAttribute("srcdoc",
                        event.getHtml());
            } else if (event.hasText()) {
                htmlSource.setText("(no HTML branch — plain text only)");
                htmlPreview.getElement().setAttribute("srcdoc", "");
            }
        });

        add(editor, new Paragraph("Plain text:"), textOut,
                new Paragraph("HTML source:"), htmlSource,
                new Paragraph("HTML preview (sandboxed):"), htmlPreview);
    }

    private static Span monospaceBlock(String initial) {
        Span span = new Span(initial);
        span.getStyle().set("font-family", "monospace")
                .set("white-space", "pre-wrap")
                .set("padding", "var(--aura-space-s)")
                .set("background", "var(--aura-contrast-5pct)")
                .set("display", "block")
                .set("border-radius", "var(--aura-border-radius-m)");
        return span;
    }
}
