package com.example.uc5;

import java.util.Base64;

import com.example.views.MainLayout;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Clipboard;
import com.vaadin.flow.component.page.ClipboardFile;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC5 — Paste images and files.
 * <p>
 * Listens for paste on a drop-zone {@code Div}. Pasted files (e.g. a
 * screenshot copied from another app) are transferred via the standard upload
 * mechanism, so the bytes arrive on the server and are rendered as an
 * inline preview when the file is an image.
 */
@Route(value = "uc5", layout = MainLayout.class)
@PageTitle("UC5 — Paste files")
@Menu(order = 5, title = "UC5 — Paste files")
public class PasteFilesView extends VerticalLayout {

    public PasteFilesView() {
        add(new H1("UC5 — Paste images and files"));
        add(new Paragraph(
                "Click into the drop zone, then paste a screenshot or any "
                        + "file with Ctrl+V / Cmd+V. The server receives the "
                        + "bytes via the upload mechanism."));

        Div dropZone = new Div();
        dropZone.setText("Paste a file here (Ctrl+V / Cmd+V)");
        dropZone.setWidthFull();
        dropZone.getElement().setAttribute("tabindex", "0");
        dropZone.getStyle()
                .set("border", "2px dashed var(--aura-contrast-30pct)")
                .set("padding", "var(--aura-space-xl)")
                .set("text-align", "center")
                .set("border-radius", "var(--aura-border-radius-l)")
                .set("min-height", "150px").set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");

        Span log = new Span("File log will appear here…");
        log.getStyle().set("font-family", "monospace")
                .set("white-space", "pre-wrap")
                .set("padding", "var(--aura-space-s)")
                .set("background", "var(--aura-contrast-5pct)")
                .set("display", "block")
                .set("border-radius", "var(--aura-border-radius-m)");

        ProgressBar uploadProgress = new ProgressBar();
        uploadProgress.setIndeterminate(true);
        uploadProgress.getStyle().set("visibility", "hidden");
        Span uploadLabel = new Span("Uploading pasted file…");
        uploadLabel.getStyle().set("visibility", "hidden");

        VerticalLayout previews = new VerticalLayout();
        previews.setPadding(false);
        previews.setSpacing(true);

        // The framework's paste handler uploads files before firing the
        // server-side listener, so a server-driven spinner would only
        // appear after the upload finishes. Toggle visibility from a
        // capture-phase JS listener that runs the moment the paste fires.
        dropZone.getElement().executeJs(
                "this.addEventListener('paste', e => {"
                        + "  for (const item of e.clipboardData.items) {"
                        + "    if (item.kind === 'file') {"
                        + "      $0.style.visibility = 'visible';"
                        + "      $1.style.visibility = 'visible';"
                        + "      return;"
                        + "    }"
                        + "  }"
                        + "}, true);",
                uploadProgress.getElement(), uploadLabel.getElement());

        Clipboard.addPasteListener(dropZone, event -> {
            uploadProgress.getStyle().set("visibility", "hidden");
            uploadLabel.getStyle().set("visibility", "hidden");
            if (!event.hasFiles()) {
                log.setText("Paste contained no files.");
                return;
            }
            StringBuilder sb = new StringBuilder();
            previews.removeAll();
            for (ClipboardFile file : event.getFiles()) {
                sb.append(file.getName()).append(" — ")
                        .append(file.getMimeType()).append(" (")
                        .append(file.getSize()).append(" bytes)\n");
                if (file.getMimeType().startsWith("image/")) {
                    previews.add(imagePreview(file));
                }
            }
            log.setText(sb.toString());
        });

        add(dropZone, uploadLabel, uploadProgress, log, previews);
    }

    private static Image imagePreview(ClipboardFile file) {
        String dataUrl = "data:" + file.getMimeType() + ";base64,"
                + Base64.getEncoder().encodeToString(file.getData());
        Image img = new Image(dataUrl, file.getName());
        img.setMaxWidth("400px");
        return img;
    }
}
