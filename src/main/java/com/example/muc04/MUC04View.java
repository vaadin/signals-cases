package com.example.muc04;

import java.util.Map;

import com.example.muc04.SignalFieldHighlighter.User;
import com.example.security.CurrentUserSignal;
import com.example.security.CurrentUserSignal.UserInfo;
import com.example.signals.UserSessionRegistry;
import com.example.views.ActiveUsersDisplay;
import com.example.views.MainLayout;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.flow.signals.shared.SharedValueSignal;

/**
 * Multi-User Case 4: Collaborative Form Editing
 * <p>
 * Demonstrates collaborative editing with field-highlighter and optional
 * field-level locking:
 * <ul>
 * <li>Show who is editing which field via colored outlines and user tags</li>
 * <li>Optionally lock fields when another user is editing</li>
 * <li>Real-time editing indicators</li>
 * </ul>
 */
@Route(value = "muc-04", layout = MainLayout.class)
@PageTitle("Multi-User Case 4: Collaborative Editing")
@Menu(order = 53, title = "MUC 4: Collaborative Editing")
@PermitAll
public class MUC04View extends VerticalLayout {

    private final User currentUser;
    private final MUC04Signals muc04Signals;
    private final ValueSignal<Boolean> lockingEnabledSignal = new ValueSignal<>(
            false);

    public MUC04View(CurrentUserSignal currentUserSignal,
            MUC04Signals muc04Signals,
            UserSessionRegistry userSessionRegistry) {
        UserInfo userInfo = currentUserSignal.getUserSignal().peek();
        String sessionId = com.vaadin.flow.server.VaadinSession.getCurrent()
                .getSession().getId();
        this.currentUser = User.fromNameAndSession(userInfo.getUsername(),
                sessionId);
        this.muc04Signals = muc04Signals;

        setSpacing(true);
        setPadding(true);

        var lockingCheckbox = new Checkbox("Enable field locking");
        lockingCheckbox.bindValue(lockingEnabledSignal,
                lockingEnabledSignal::set);

        var companyNameField = createCollaborativeField("companyName",
                "Company Name", muc04Signals.getCompanyNameSignal());
        var addressField = createCollaborativeField("address", "Address",
                muc04Signals.getAddressSignal());
        var phoneField = createCollaborativeField("phone", "Phone Number",
                muc04Signals.getPhoneSignal());

        var activeSessionsBox = new ActiveUsersDisplay(userSessionRegistry,
                "muc-04");

        var typingStatusBanner = createTypingStatusBanner();

        var saveButton = new Button("Save Changes",
                _ -> Notification.show("Changes saved successfully"));
        saveButton.addThemeName("primary");

        add(new H2("Multi-User Case 4: Collaborative Form Editing"),
                new Paragraph(
                        "This demonstrates collaborative form editing with "
                                + "field-highlighter. When you type in a field, "
                                + "other users see who is editing via colored "
                                + "outlines. Enable field locking to prevent "
                                + "concurrent edits."), activeSessionsBox,
                new H3("Shared Form Data"), lockingCheckbox, companyNameField,
                addressField, phoneField, saveButton, typingStatusBanner);
    }

    private Div createTypingStatusBanner() {
        var banner = new Div();
        banner.getStyle()
                .set("position", "fixed")
                .set("bottom", "20px")
                .set("left", "50%")
                .set("transform", "translateX(-50%)")
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("color", "white")
                .set("padding", "12px 24px")
                .set("border-radius", "24px")
                .set("font-size", "14px")
                .set("font-weight", "500")
                .set("z-index", "1000")
                .set("box-shadow", "0 4px 12px rgba(0,0,0,0.3)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "8px")
                .set("animation", "slideUp 0.3s ease-out");

        // Add CSS animation
        banner.getElement().executeJs(
            "const style = document.createElement('style');" +
            "style.textContent = '@keyframes slideUp { from { transform: translate(-50%, 20px); opacity: 0; } to { transform: translate(-50%, 0); opacity: 1; } }';" +
            "document.head.appendChild(style);");

        // Map to track field name to display name
        var fieldLabels = Map.of(
                "companyName", "Company Name",
                "address", "Address",
                "phone", "Phone Number"
        );

        // Typing indicator icon (animated dots)
        var typingIcon = new Span("✍️");
        typingIcon.getStyle()
                .set("font-size", "16px")
                .set("animation", "pulse 1.5s ease-in-out infinite");

        banner.getElement().executeJs(
            "const style = document.createElement('style');" +
            "style.textContent = '@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }';" +
            "document.head.appendChild(style);");

        var statusText = new Span();
        statusText.getStyle().set("line-height", "1.4");

        // Combine all editors into a single signal with improved formatting
        var allEditorsSignal = Signal.computed(() -> {
            var messages = new java.util.ArrayList<String>();

            for (var entry : fieldLabels.entrySet()) {
                var fieldName = entry.getKey();
                var fieldLabel = entry.getValue();
                var editors = muc04Signals.getFieldEditors(fieldName).get();

                for (var editorSignal : editors) {
                    var user = editorSignal.get();
                    if (user != null && !user.sessionId().equals(currentUser.sessionId())) {
                        messages.add(user.name() + " is editing \"" + fieldLabel + "\"");
                    }
                }
            }

            if (messages.isEmpty()) {
                return "";
            } else if (messages.size() == 1) {
                return messages.get(0);
            } else {
                return String.join(" | ", messages);
            }
        });

        statusText.bindText(allEditorsSignal);
        banner.bindVisible(allEditorsSignal.map(text -> !text.isEmpty()));
        banner.add(typingIcon, statusText);

        return banner;
    }

    private TextField createCollaborativeField(String fieldName, String label,
            SharedValueSignal<String> signal) {
        var field = new TextField(label);
        field.setWidthFull();

        // Enable eager value synchronization for immediate updates
        field.setValueChangeMode(
                com.vaadin.flow.data.value.ValueChangeMode.EAGER);

        field.bindValue(signal, signal::set);

        // Start editing on value change (immediate feedback while typing)
        field.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                muc04Signals.startEditing(fieldName, currentUser);
            }
        });

        field.addBlurListener(
                _ -> muc04Signals.stopEditing(fieldName, currentUser));

        var editors = muc04Signals.getFieldEditors(fieldName);
        SignalFieldHighlighter.bind(field, editors, currentUser);

        // Add visual highlighting when others are editing
        Signal.effect(field, () -> {
            var otherEditors = editors.get().stream()
                    .map(Signal::get)
                    .filter(u -> u != null && !u.sessionId().equals(currentUser.sessionId()))
                    .toList();

            if (!otherEditors.isEmpty()) {
                var firstEditor = otherEditors.get(0);
                var colors = new String[] {
                    "#e91e63", "#9c27b0", "#673ab7",
                    "#3f51b5", "#2196f3", "#009688", "#ff9800"
                };
                var color = colors[firstEditor.colorIndex() % colors.length];

                // Apply light background and subtle border to the input
                field.getElement().executeJs(
                    "const input = this.shadowRoot ? this.shadowRoot.querySelector('input, textarea') : null;" +
                    "if (input) {" +
                    "  input.style.setProperty('border', '1px solid " + color + "', 'important');" +
                    "  input.style.setProperty('background-color', '" + color + "08', 'important');" +
                    "}");
            } else {
                field.getElement().executeJs(
                    "const input = this.shadowRoot ? this.shadowRoot.querySelector('input, textarea') : null;" +
                    "if (input) {" +
                    "  input.style.removeProperty('border');" +
                    "  input.style.removeProperty('background-color');" +
                    "}");
            }
        });

        // Disable field if locking is enabled and another session is editing
        var enabledSignal = lockingEnabledSignal.map(lockingEnabled -> {
            if (!lockingEnabled) {
                return true;
            }
            return editors.get().stream().map(Signal::get)
                    .noneMatch(u -> u != null && !u.sessionId().equals(currentUser.sessionId()));
        });
        field.bindEnabled(enabledSignal);

        return field;
    }
}
