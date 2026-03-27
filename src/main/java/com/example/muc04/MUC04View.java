package com.example.muc04;

import com.example.muc04.SignalFieldHighlighter.User;
import com.example.security.CurrentUserSignal;
import com.example.security.CurrentUserSignal.UserInfo;
import com.example.signals.SessionIdHelper;
import com.example.signals.UserSessionRegistry;
import com.example.views.ActiveUsersDisplay;
import com.example.views.ColoredAvatar;
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
        String username = userInfo.getUsername();
        String sessionId = SessionIdHelper.getCurrentSessionId();
        int colorIndex = userSessionRegistry.getUserColorIndex(username,
                sessionId);
        this.currentUser = new User(
                (username + ":" + sessionId).hashCode(), username, colorIndex);
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

        var editorsDiv = createEditorsPanel();

        var saveButton = new Button("Save Changes",
                _ -> Notification.show("Changes saved successfully"));
        saveButton.addThemeName("primary");

        add(new H2("Multi-User Case 4: Collaborative Form Editing"),
                new Paragraph(
                        "This demonstrates collaborative form editing with "
                                + "field-highlighter. When you focus a field, "
                                + "other users see who is editing via colored "
                                + "outlines. Enable field locking to prevent "
                                + "concurrent edits."), activeSessionsBox,
                new H3("Shared Form Data"), lockingCheckbox, companyNameField,
                addressField, phoneField, saveButton, new H3("Active Editors"),
                editorsDiv);
    }

    private Div createEditorsPanel() {
        var editorsDiv = new Div();
        editorsDiv.getStyle().set("background-color", "#e3f2fd")
                .set("padding", "1em").set("border-radius", "4px");

        addFieldEditorList(editorsDiv, "companyName", "Company Name");
        addFieldEditorList(editorsDiv, "address", "Address");
        addFieldEditorList(editorsDiv, "phone", "Phone Number");

        return editorsDiv;
    }

    private void addFieldEditorList(Div container, String fieldName,
            String label) {
        var editors = muc04Signals.getFieldEditors(fieldName);

        var fieldDiv = new Div();
        fieldDiv.bindVisible(editors.map(list -> !list.isEmpty()));

        var fieldLabel = new Span(label + ": ");
        fieldLabel.getStyle().set("font-weight", "bold");
        fieldDiv.add(fieldLabel);

        var namesContainer = new Div();
        namesContainer.getStyle().set("display", "inline");
        namesContainer.bindChildren(editors, this::createEditorName);
        fieldDiv.add(namesContainer);

        container.add(fieldDiv);
    }

    private Span createEditorName(SharedValueSignal<User> userSignal) {
        var user = userSignal.peek();
        var name = new Span(user.name());
        name.getStyle().set("margin-right", "0.5em");
        return name;
    }

    private TextField createCollaborativeField(String fieldName, String label,
            SharedValueSignal<String> signal) {
        var field = new TextField(label);
        field.setWidthFull();
        field.bindValue(signal, signal::set);
        field.addFocusListener(
                _ -> muc04Signals.startEditing(fieldName, currentUser));
        field.addBlurListener(
                _ -> muc04Signals.stopEditing(fieldName, currentUser));

        var editors = muc04Signals.getFieldEditors(fieldName);
        SignalFieldHighlighter.bind(field, editors, currentUser);

        // Disable field if locking is enabled and another user is editing
        var enabledSignal = lockingEnabledSignal.map(lockingEnabled -> {
            if (!lockingEnabled) {
                return true;
            }
            return editors.get().stream().map(Signal::get)
                    .noneMatch(u -> u != null && u.id() != currentUser.id());
        });
        field.bindEnabled(enabledSignal);

        return field;
    }

    private String formatFieldName(String fieldName) {
        return switch (fieldName) {
        case "companyName" -> "Company Name";
        case "address" -> "Address";
        case "phone" -> "Phone Number";
        default -> fieldName;
        };
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        this.sessionId = SessionIdHelper.getCurrentSessionId();
    }

    private HorizontalLayout createLockItem(
            com.vaadin.flow.signals.shared.SharedValueSignal<MUC04Signals.FieldLock> lockSignal) {
        String fieldName = lockKeyMap.getOrDefault(lockSignal, "");
        String fieldLabel = formatFieldName(fieldName);

        MUC04Signals.FieldLock lock = lockSignal.peek();
        boolean isCurrentSession = sessionId != null
                && lock.username().equals(currentUser)
                && lock.sessionId().equals(sessionId);

        String userColor = userSessionRegistry.getUserColor(lock.username(),
                lock.sessionId());

        HorizontalLayout item = new HorizontalLayout();
        item.setSpacing(true);
        item.setAlignItems(
                com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        item.getStyle().set("padding", "0.5em")
                .set("background-color",
                        isCurrentSession ? "#fff3e0" : "transparent")
                .set("border-left", "3px solid " + userColor)
                .set("border-radius", "4px");

        ColoredAvatar avatar = new ColoredAvatar(lock.username(), userColor,
                32);

        Span label = new Span(
                String.format("🔒 %s: %s", fieldLabel, lock.username()));

        item.add(avatar, label);
        return item;
    }
}
