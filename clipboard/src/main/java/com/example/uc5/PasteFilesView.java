package com.example.uc5;

import java.util.Base64;

import com.example.views.MainLayout;

import com.vaadin.flow.component.clipboard.Clipboard;
import com.vaadin.flow.component.clipboard.ClipboardFile;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
 * <p>
 * The progress bar is driven by {@link Clipboard#addPasteStartListener} (which
 * fires the moment the browser paste is observed, before any upload begins)
 * and hidden by {@link Clipboard#addPasteListener} on success or
 * {@link Clipboard#addPasteFailedListener} on failure.
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
        uploadProgress.setVisible(false);
        Span uploadLabel = new Span();
        uploadLabel.setVisible(false);

        VerticalLayout previews = new VerticalLayout();
        previews.setPadding(false);
        previews.setSpacing(true);

        Clipboard.addPasteStartListener(dropZone, event -> {
            if (event.getFiles().isEmpty()) {
                return;
            }
            int count = event.getFiles().size();
            uploadLabel.setText("Uploading "
                    + (count == 1 ? "pasted file…" : count + " pasted files…"));
            uploadLabel.setVisible(true);
            uploadProgress.setVisible(true);
        });

        Clipboard.addPasteListener(dropZone, event -> {
            uploadProgress.setVisible(false);
            uploadLabel.setVisible(false);
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

        Clipboard.addPasteFailedListener(dropZone, event -> {
            uploadProgress.setVisible(false);
            uploadLabel.setVisible(false);
            Notification.show("Upload failed for " + event.file().name()
                    + ": " + event.reason());
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
