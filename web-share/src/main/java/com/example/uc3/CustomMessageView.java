package com.example.uc3;

import com.example.views.MainLayout;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.page.WebShareSupport;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;

/**
 * UC3 — Share a custom message.
 * <p>
 * A small form lets the user fill in any of the three payload fields
 * ({@code title}, {@code text}, {@code url}) and shows a live JSON preview of
 * the data that will be handed to {@link Page#share(String, String, String)}.
 * Empty fields are passed as {@code null} so the browser sees them as omitted
 * rather than empty strings — useful when sharing only a URL or only a text
 * snippet.
 */
@Route(value = "uc3", layout = MainLayout.class)
@Menu(order = 3, title = "UC3 — Share a custom message")
public class CustomMessageView extends VerticalLayout {

    private static final ObjectMapper JSON = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT).build();

    private final TextField titleField = new TextField("Title");
    private final TextArea textField = new TextArea("Text");
    private final TextField urlField = new TextField("URL");
    private final Div preview = new Div();
    private final Button shareButton = new Button("Share",
            VaadinIcon.SHARE.create());

    public CustomMessageView() {
        add(new H1("UC3 — Share a custom message"));
        add(new Paragraph("Fill any combination of the three fields. The "
                + "preview shows exactly what gets passed to "
                + "Page.share(title, text, url); empty fields are sent as "
                + "null so the browser treats them as omitted."));

        titleField.setPlaceholder("e.g. Look at this!");
        titleField.setValue("Look at this!");
        textField.setPlaceholder("Optional body text");
        textField.setValue("Found this while reading docs.");
        urlField.setPlaceholder("https://example.com/article/42");
        urlField.setValue("https://vaadin.com/");
        titleField.setWidthFull();
        textField.setWidthFull();
        urlField.setWidthFull();

        preview.addClassName("share-preview");
        shareButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        titleField.setValueChangeMode(
                com.vaadin.flow.data.value.ValueChangeMode.EAGER);
        textField.setValueChangeMode(
                com.vaadin.flow.data.value.ValueChangeMode.EAGER);
        urlField.setValueChangeMode(
                com.vaadin.flow.data.value.ValueChangeMode.EAGER);
        titleField.addValueChangeListener(e -> updatePreview());
        textField.addValueChangeListener(e -> updatePreview());
        urlField.addValueChangeListener(e -> updatePreview());

        add(titleField, textField, urlField, new H2("Payload preview"),
                preview, shareButton);
        updatePreview();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        Page page = attachEvent.getUI().getPage();
        Signal<WebShareSupport> support = page.shareSupportSignal();

        Signal.effect(this, () -> shareButton
                .setEnabled(support.get() == WebShareSupport.SUPPORTED));

        shareButton.addClickListener(e -> {
            String title = nullIfBlank(titleField.getValue());
            String text = nullIfBlank(textField.getValue());
            String url = nullIfBlank(urlField.getValue());
            if (title == null && text == null && url == null) {
                Notification.show("Fill at least one field before sharing",
                        2500, Notification.Position.BOTTOM_START);
                return;
            }
            page.share(title, text, url);
            Notification.show("Share invoked with the previewed payload",
                    2000, Notification.Position.BOTTOM_START);
        });
    }

    private void updatePreview() {
        try {
            ObjectNode node = JSON.createObjectNode();
            node.put("title", nullIfBlank(titleField.getValue()));
            node.put("text", nullIfBlank(textField.getValue()));
            node.put("url", nullIfBlank(urlField.getValue()));
            preview.setText(JSON.writeValueAsString(node));
        } catch (Exception ex) {
            preview.setText("(preview error: " + ex.getMessage() + ")");
        }
    }

    private static @Nullable String nullIfBlank(@Nullable String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
