package com.example.muc02;

import jakarta.annotation.security.PermitAll;

import com.example.security.CurrentUserSignal;
import com.example.signals.SessionIdHelper;
import com.example.signals.UserSessionRegistry;
import com.example.views.ActiveUsersDisplay;
import com.example.views.MainLayout;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.shared.SharedValueSignal;

/**
 * Multi-User Case 2: Collaborative Cursor Positions
 *
 * Demonstrates showing all users' cursor positions in real-time: - Each user
 * updates their own cursor position signal - All users read all cursor signals
 * - Map<UserId, Signal<CursorPosition>> - Real-time updates
 *
 * Key Patterns: - Per-user writable signals shared via static Map - All users
 * observe all signals - Efficient multi-signal updates - Collaborative
 * awareness indicators
 */
@Route(value = "muc-02", layout = MainLayout.class)
@PageTitle("Multi-User Case 2: Collaborative Cursors")
@Menu(order = 51, title = "MUC 2: Collaborative Cursors")
@PermitAll
public class MUC02View extends VerticalLayout {

    private final String currentUser;
    private SharedValueSignal<MUC02Signals.CursorPosition> myCursorSignal;
    private final MUC02Signals muc02Signals;
    private final UserSessionRegistry userSessionRegistry;
    private String sessionId;
    private final java.util.IdentityHashMap<SharedValueSignal<MUC02Signals.CursorPosition>, String> cursorKeyMap = new java.util.IdentityHashMap<>();

    public MUC02View(CurrentUserSignal currentUserSignal,
            MUC02Signals muc02Signals,
            UserSessionRegistry userSessionRegistry) {
        CurrentUserSignal.UserInfo userInfo = currentUserSignal.getUserSignal()
                .get();
        if (userInfo == null || !userInfo.isAuthenticated()) {
            throw new IllegalStateException(
                    "User must be authenticated to access this view");
        }
        this.currentUser = userInfo.getUsername();
        this.muc02Signals = muc02Signals;
        this.userSessionRegistry = userSessionRegistry;

        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Multi-User Case 2: Collaborative Cursor Positions");

        Paragraph description = new Paragraph(
                "This use case demonstrates showing all users' cursor positions in a shared canvas area. "
                        + "Each user's cursor position is stored in their own signal, and all signals are read by all users. "
                        + "Try opening in multiple windows to see collaborative cursor awareness.");

        // Canvas area for cursor tracking
        Div canvas = new Div();
        canvas.setId("cursor-canvas");
        canvas.setWidthFull();
        canvas.getStyle().set("position", "relative")
                .set("background-color", "#f5f5f5")
                .set("border", "2px solid #e0e0e0").set("border-radius", "4px")
                .set("height", "400px").set("margin", "1em 0")
                .set("cursor", "crosshair");

        // Add cursor indicators for all users
        Div cursorsContainer = new Div();
        cursorsContainer.getStyle().set("position", "relative");
        canvas.add(cursorsContainer);

        // Render cursor indicators for all users
        renderAllCursors(cursorsContainer);

        // Track mouse movement
        canvas.getElement().addEventListener("mousemove", event -> {
            // Only update if attached (sessionId and myCursorSignal are set)
            if (myCursorSignal != null) {
                // Get mouse position relative to canvas
                double clientX = event.getEventData().get("event.offsetX")
                        .asDouble();
                double clientY = event.getEventData().get("event.offsetY")
                        .asDouble();
                myCursorSignal.set(new MUC02Signals.CursorPosition(
                        (int) clientX, (int) clientY));
            }
        }).addEventData("event.offsetX").addEventData("event.offsetY");

        // Active sessions display
        ActiveUsersDisplay activeSessionsBox = new ActiveUsersDisplay(
                userSessionRegistry, "muc-02");

        // Current users list (cursor tracking)
        H3 usersTitle = new H3("Cursor Tracking");
        Div usersList = new Div();
        usersList.getStyle().set("background-color", "#e3f2fd")
                .set("padding", "1em").set("border-radius", "4px");

        // Display cursor positions per session - reactive
        usersList.bindChildren(
                muc02Signals.getSessionCursorsSignal().map(cursors -> {
                    cursorKeyMap.clear();
                    cursors.forEach(
                            (key, signal) -> cursorKeyMap.put(signal, key));
                    return new java.util.ArrayList<>(cursors.values());
                }), this::createCursorListItem);

        // Info box
        Div infoBox = new Div();
        infoBox.getStyle().set("background-color", "#fff3e0")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin-top", "1em").set("font-style", "italic");
        infoBox.add(new Paragraph(
                "💡 Each user's cursor position is stored in a SharedValueSignal in a shared Map. "
                        + "All users read all signals to display cursor indicators. This pattern enables "
                        + "real-time collaborative awareness without complex synchronization code. "
                        + "With Vaadin Push, updates propagate automatically to all connected clients."));

        add(title, description, activeSessionsBox, canvas, usersTitle,
                usersList, infoBox);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        this.sessionId = SessionIdHelper.getCurrentSessionId();
        this.myCursorSignal = muc02Signals.getCursorSignalForUser(currentUser,
                sessionId);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        muc02Signals.unregisterCursor(currentUser, sessionId);
    }

    private void renderAllCursors(Div container) {
        // Reactive rendering of cursor indicators
        container.bindChildren(Signal.computed(() -> {
            var cursors = muc02Signals.getSessionCursorsSignal().get();
            cursorKeyMap.clear();
            cursors.forEach((key, signal) -> cursorKeyMap.put(signal, key));
            String ownKey = currentUser + ":" + sessionId;
            return cursors.entrySet().stream()
                    .filter(entry -> sessionId == null
                            || !entry.getKey().equals(ownKey))
                    .map(java.util.Map.Entry::getValue).toList();
        }), this::createCursorIndicator);
    }

    private String lookupSessionKey(
            SharedValueSignal<MUC02Signals.CursorPosition> positionSignal) {
        return cursorKeyMap.getOrDefault(positionSignal, "");
    }

    private java.util.Map<String, String> buildDisplayNameMap() {
        var users = userSessionRegistry.getActiveUsersSignal().get();
        var displayNames = userSessionRegistry.getDisplayNamesSignal().get();
        java.util.Map<String, String> map = new java.util.HashMap<>();
        for (int i = 0; i < users.size() && i < displayNames.size(); i++) {
            map.put(users.get(i).get().getCompositeKey(), displayNames.get(i));
        }
        return map;
    }

    private HorizontalLayout createCursorListItem(
            SharedValueSignal<MUC02Signals.CursorPosition> positionSignal) {
        String sessionKey = lookupSessionKey(positionSignal);
        String displayName = buildDisplayNameMap().getOrDefault(sessionKey,
                "[" + sessionKey + "]");
        String username = sessionKey.split(":")[0];

        HorizontalLayout userItem = new HorizontalLayout();
        userItem.setSpacing(true);
        userItem.setAlignItems(
                com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        userItem.setWidthFull();
        userItem.getStyle().set("margin-bottom", "0.5em");

        Image avatar = new Image(MainLayout.getProfilePicturePath(username),
                "");
        avatar.setWidth("32px");
        avatar.setHeight("32px");
        avatar.getStyle().set("border-radius", "50%").set("object-fit",
                "cover");

        Div userLabel = new Div();
        userLabel.setText(displayName);
        userLabel.getStyle().set("font-weight", "500");

        Div positionLabel = new Div(
                positionSignal.map(MUC02Signals.CursorPosition::toString));
        positionLabel.getStyle().set("font-family", "monospace")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-left", "auto");

        userItem.add(avatar, userLabel, positionLabel);
        return userItem;
    }

    private Div createCursorIndicator(
            SharedValueSignal<MUC02Signals.CursorPosition> positionSignal) {
        String sessionKey = lookupSessionKey(positionSignal);
        String displayName = buildDisplayNameMap().getOrDefault(sessionKey,
                "[" + sessionKey + "]");

        Div cursorIndicator = new Div();
        cursorIndicator.getStyle().set("position", "absolute")
                .set("width", "20px").set("height", "20px")
                .set("background-color", "var(--lumo-primary-color)")
                .set("border-radius", "50%").set("border", "2px solid white")
                .set("pointer-events", "none")
                .set("transform", "translate(-50%, -50%)")
                .set("z-index", "1000");

        cursorIndicator.getStyle().bind("left", positionSignal
                .map(pos -> pos != null ? pos.x() + "px" : "0px"));
        cursorIndicator.getStyle().bind("top", positionSignal
                .map(pos -> pos != null ? pos.y() + "px" : "0px"));

        Div label = new Div();
        label.setText(displayName);
        label.getStyle().set("position", "absolute").set("top", "25px")
                .set("left", "0").set("white-space", "nowrap")
                .set("background-color", "rgba(0, 0, 0, 0.7)")
                .set("color", "white").set("padding", "2px 6px")
                .set("border-radius", "3px").set("font-size", "0.75em");

        cursorIndicator.add(label);
        return cursorIndicator;
    }
}
