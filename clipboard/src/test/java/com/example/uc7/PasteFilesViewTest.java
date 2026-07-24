package com.example.uc7;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.clipboard.ClipboardSimulator;
import com.vaadin.flow.component.clipboard.PastedFile;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = PasteFilesView.class)
class PasteFilesViewTest extends SpringBrowserlessTest {

    @Test
    void pastingImageFile_logsItAndRendersPreview() {
        navigate(PasteFilesView.class);
        Div dropZone = findInView(Div.class).withClassName("drop-zone")
                .single();

        ClipboardSimulator.current().pasteFilesInto(dropZone, PastedFile.of(
                "chart.png", "image/png",
                "not-a-real-png".getBytes(StandardCharsets.UTF_8)));

        // onFile logged the file's name and MIME type.
        Span log = findInView(Span.class).withClassName("file-log").single();
        assertTrue(log.getText().contains("chart.png"), log.getText());
        assertTrue(log.getText().contains("image/png"), log.getText());

        // image MIME types are rendered as an inline preview.
        assertTrue(findInView(Image.class).all().stream()
                .anyMatch(img -> "chart.png"
                        .equals(img.getElement().getAttribute("alt"))));
    }
}
