package com.example.muc04;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;

import jakarta.annotation.security.PermitAll;

import com.example.security.CurrentUserSignal;
import com.example.signals.SessionIdHelper;
import com.example.signals.UserSessionRegistry;
import com.example.views.ActiveUsersDisplay;
import com.example.views.MainLayout;
import org.jspecify.annotations.Nullable;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
 *
 * Demonstrates collaborative editing with field-highlighter and optional
 * field-level locking:
 * <ul>
 * <li>Show who is editing which field via colored outlines and user tags</li>
 * <li>Optionally lock fields when another user is editing</li>
 * <li>Real-time editing indicators</li>
 * </ul>
 */
@JsModule("@vaadin/field-highlighter/src/vaadin-field-highlighter.js")
@Route(value = "muc-04", layout = MainLayout.class)
@PageTitle("Multi-User Case 4: Collaborative Editing")
@Menu(order = 53, title = "MUC 4: Collaborative Editing")
@PermitAll
public class MUC04View extends VerticalLayout {

    private static final int USER_COLOR_COUNT = 7;

    private final String currentUser;
    private final MUC04Signals muc04Signals;
    private final ValueSignal<Boolean> lockingEnabledSignal =
            new ValueSignal<>(false);
    private @Nullable String sessionId;
    private final IdentityHashMap<SharedValueSignal<MUC04Signals.FieldLock>, String> lockKeyMap =
            new IdentityHashMap<>();

    public MUC04View(CurrentUserSignal currentUserSignal,
            MUC04Signals muc04Signals,
            UserSessionRegistry userSessionRegistry) {
        CurrentUserSignal.UserInfo userInfo =
                currentUserSignal.getUserSignal().peek();
        if (userInfo == null || !userInfo.isAuthenticated()) {
            throw new IllegalStateException(
                    "User must be authenticated to access this view");
        }
        this.currentUser = userInfo.getUsername();
        this.muc04Signals = muc04Signals;

        setSpacing(true);
        setPadding(true);

        Checkbox lockingCheckbox = new Checkbox("Enable field locking");
        lockingCheckbox.bindValue(lockingEnabledSignal,
                lockingEnabledSignal::set);

        TextField companyNameField = createCollaborativeField("companyName",
                "Company Name", muc04Signals.getCompanyNameSignal());
        TextField addressField = createCollaborativeField("address", "Address",
                muc04Signals.getAddressSignal());
        TextField phoneField = createCollaborativeField("phone",
                "Phone Number", muc04Signals.getPhoneSignal());

        ActiveUsersDisplay activeSessionsBox =
                new ActiveUsersDisplay(userSessionRegistry, "muc-04");

        Div editorsDiv = createEditorsPanel();

        ValueSignal<Boolean> showSaveSuccessSignal = new ValueSignal<>(false);
        Paragraph successMsg = new Paragraph("Changes saved successfully");
        successMsg.getStyle().set("color", "green");
        successMsg.bindVisible(showSaveSuccessSignal);

        Button saveButton = new Button("Save Changes", event -> {
            showSaveSuccessSignal.set(true);
            event.getSource().getUI().ifPresent(ui -> ui.access(() -> {
                showSaveSuccessSignal.set(false);
            }));
        });
        saveButton.addThemeName("primary");

        add(new H2("Multi-User Case 4: Collaborative Form Editing"),
                new Paragraph(
                        "This demonstrates collaborative form editing with "
                                + "field-highlighter. When you focus a field, "
                                + "other users see who is editing via colored "
                                + "outlines. Enable field locking to prevent "
                                + "concurrent edits."),
                activeSessionsBox, new H3("Shared Form Data"),
                lockingCheckbox, companyNameField, addressField, phoneField,
                saveButton, successMsg, new H3("Active Editors"), editorsDiv);
    }

    private Div createEditorsPanel() {
        Div editorsDiv = new Div();
        editorsDiv.getStyle().set("background-color", "#e3f2fd")
                .set("padding", "1em").set("border-radius", "4px");

        Span emptyMsg = new Span("No fields are currently being edited");
        emptyMsg.getStyle().set("font-style", "italic");
        emptyMsg.bindVisible(muc04Signals.getFieldLocksSignal()
                .map(Map::isEmpty));
        editorsDiv.add(emptyMsg);

        Div locksContainer = new Div();
        editorsDiv.add(locksContainer);

        locksContainer
                .bindChildren(muc04Signals.getFieldLocksSignal().map(locks -> {
                    lockKeyMap.clear();
                    locks.forEach(
                            (key, signal) -> lockKeyMap.put(signal, key));
                    return new ArrayList<>(locks.values());
                }), this::createEditorItem);

        return editorsDiv;
    }

    private TextField createCollaborativeField(String fieldName, String label,
            SharedValueSignal<String> signal) {
        TextField field = new TextField(label);
        field.setWidthFull();

        field.bindValue(signal, signal::set);

        field.addFocusListener(event -> {
            if (sessionId != null) {
                muc04Signals.startEditing(fieldName, currentUser, sessionId);
            }
        });

        field.addBlurListener(event -> {
            if (sessionId != null) {
                muc04Signals.stopEditing(fieldName, currentUser, sessionId);
            }
        });

        field.getElement().executeJs(
                "customElements.get('vaadin-field-highlighter').init(this)");

        Signal.effect(field, () -> updateFieldHighlighter(field, fieldName));

        Signal<Boolean> enabledSignal = lockingEnabledSignal.map(
                lockingEnabled -> {
                    if (!lockingEnabled) {
                        return true;
                    }
                    return !isEditedByOtherUser(fieldName);
                });
        field.bindEnabled(enabledSignal);

        return field;
    }

    private void updateFieldHighlighter(TextField field, String fieldName) {
        var locks = muc04Signals.getFieldLocksSignal().get();
        var lockSignal = locks.get(fieldName);

        if (lockSignal == null) {
            clearFieldHighlighter(field);
            return;
        }

        MUC04Signals.FieldLock lock = lockSignal.get();
        if (lock != null && !isCurrentSession(lock)) {
            int colorIndex =
                    Math.abs(lock.username().hashCode()) % USER_COLOR_COUNT;
            field.getElement().executeJs(
                    "customElements.get('vaadin-field-highlighter')"
                            + ".setUsers(this, [{id: $0, name: $1, colorIndex: $2}])",
                    lock.username().hashCode(), lock.username(), colorIndex);
        } else {
            clearFieldHighlighter(field);
        }
    }

    private void clearFieldHighlighter(TextField field) {
        field.getElement().executeJs(
                "customElements.get('vaadin-field-highlighter')"
                        + ".setUsers(this, [])");
    }

    private boolean isEditedByOtherUser(String fieldName) {
        var locks = muc04Signals.getFieldLocksSignal().get();
        var lockSignal = locks.get(fieldName);
        if (lockSignal == null) {
            return false;
        }
        MUC04Signals.FieldLock lock = lockSignal.get();
        return lock != null && !isCurrentSession(lock);
    }

    private boolean isCurrentSession(MUC04Signals.FieldLock lock) {
        return sessionId != null && lock.username().equals(currentUser)
                && lock.sessionId().equals(sessionId);
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

    private HorizontalLayout createEditorItem(
            SharedValueSignal<MUC04Signals.FieldLock> lockSignal) {
        String fieldName = lockKeyMap.getOrDefault(lockSignal, "");
        String fieldLabel = formatFieldName(fieldName);

        MUC04Signals.FieldLock lock = lockSignal.peek();
        boolean isCurrentUser = isCurrentSession(lock);

        HorizontalLayout item = new HorizontalLayout();
        item.setSpacing(true);
        item.setAlignItems(FlexComponent.Alignment.CENTER);
        item.getStyle().set("padding", "0.5em")
                .set("background-color",
                        isCurrentUser ? "#fff3e0" : "transparent")
                .set("border-radius", "4px");

        Image avatar = new Image(
                MainLayout.getProfilePicturePath(lock.username()), "");
        avatar.setWidth("32px");
        avatar.setHeight("32px");
        avatar.getStyle().set("border-radius", "50%").set("object-fit",
                "cover");

        Span label = new Span("%s: %s".formatted(fieldLabel, lock.username()));

        item.add(avatar, label);
        return item;
    }
}
