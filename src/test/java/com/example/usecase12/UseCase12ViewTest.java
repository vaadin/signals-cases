package com.example.usecase12;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.select.Select;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = UseCase12View.class)
@WithMockUser
class UseCase12ViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithSelectAndParagraphs() {
        navigate(UseCase12View.class);

        assertEquals(1, $view(Select.class).all().size());
        assertTrue($view(Paragraph.class).all().size() >= 4);
    }

    @Test
    void initialTitleIsDocumentViewer() {
        navigate(UseCase12View.class);
        runPendingSignalsTasks();

        // The title display Paragraph uses bindText(viewTitleSignal)
        // It's the 3rd paragraph (after description and "Current Title Signal:" label)
        Paragraph titleDisplay = $view(Paragraph.class).all().get(2);
        assertEquals("Document Viewer", titleDisplay.getText());
    }

    @Test
    void fullTitleShowsAppNameAndViewTitle() {
        navigate(UseCase12View.class);
        runPendingSignalsTasks();

        // The browser title display Paragraph uses bindText(fullTitleSignal)
        // It's the 4th paragraph (after browser title label)
        Paragraph browserTitleDisplay = $view(Paragraph.class).all().get(4);
        assertEquals("Signal API Demo - Document Viewer",
                browserTitleDisplay.getText());
    }

    @SuppressWarnings("unchecked")
    @Test
    void changingSelectUpdatesTitleDisplay() {
        navigate(UseCase12View.class);

        Select<String> select = $view(Select.class).single();
        test(select).selectItem("Code Editor");
        runPendingSignalsTasks();

        Paragraph titleDisplay = $view(Paragraph.class).all().get(2);
        assertEquals("Code Editor", titleDisplay.getText());
    }

    @SuppressWarnings("unchecked")
    @Test
    void changingSelectUpdatesFullTitle() {
        navigate(UseCase12View.class);

        Select<String> select = $view(Select.class).single();
        test(select).selectItem("PDF Reader");
        runPendingSignalsTasks();

        Paragraph browserTitleDisplay = $view(Paragraph.class).all().get(4);
        assertEquals("Signal API Demo - PDF Reader",
                browserTitleDisplay.getText());
    }
}
