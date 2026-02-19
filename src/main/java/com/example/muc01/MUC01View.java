package com.example.muc01;

import jakarta.annotation.security.PermitAll;

import com.example.security.CurrentUserSignal;
import com.example.signals.SessionIdHelper;
import com.example.signals.UserSessionRegistry;
import com.example.views.ActiveUsersDisplay;
import com.example.views.MainLayout;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Multi-User Case 1: Shared Chat/Message List
 *
 * Demonstrates a collaborative chat interface with: - Append-only shared
 * message list signal - Multiple users adding messages - Server-side signal
 * source with Push updates - Each user can only append, not modify history
 *
 * Key Patterns: - Application-scoped SharedListSignal&lt;Message&gt; across
 * sessions - Append-only operations (insertLast) - Server-side signal
 * coordination via Spring @Component - Push-based real-time updates to all
 * connected clients
 */
@Route(value = "muc-01", layout = MainLayout.class)
@PageTitle("MUC 1: Shared Chat")
@Menu(order = 50, title = "MUC 1: Shared Chat")
@PermitAll
public class MUC01View extends VerticalLayout {

    private final String currentUser;
    private final MUC01Signals muc01Signals;
    private final UserSessionRegistry userSessionRegistry;
    private String sessionId;

    public MUC01View(CurrentUserSignal currentUserSignal,
            MUC01Signals muc01Signals,
            UserSessionRegistry userSessionRegistry) {
        CurrentUserSignal.UserInfo userInfo = currentUserSignal.getUserSignal()
                .get();
        if (userInfo == null || !userInfo.isAuthenticated()) {
            throw new IllegalStateException(
                    "User must be authenticated to access this view");
        }
        this.currentUser = userInfo.getUsername();
        this.muc01Signals = muc01Signals;
        this.userSessionRegistry = userSessionRegistry;

        setSpacing(true);
        setPadding(true);
        setHeight("100%");

        H2 title = new H2("Multi-User Case 1: Shared Chat/Message List");

        Paragraph description = new Paragraph(
                "This use case demonstrates a collaborative chat with an append-only shared message list. "
                        + "Messages are stored in an application-scoped SharedListSignal and broadcast to all users via Push. "
                        + "Try opening this page in multiple browser windows (different users) to see real-time collaboration.");

        // Active users display
        ActiveUsersDisplay activeUsersDisplay = new ActiveUsersDisplay(
                userSessionRegistry, "muc-01");

        // Message display area
        Div messagesContainer = new Div();
        messagesContainer.setWidthFull();
        messagesContainer.getStyle().set("background-color", "#f5f5f5")
                .set("border", "1px solid #e0e0e0").set("border-radius", "4px")
                .set("padding", "1em").set("min-height", "200px")
                .set("overflow-y", "auto").set("max-height", "400px");

        // Bind message list to UI
        messagesContainer.bindChildren(muc01Signals.getMessagesSignal(),
                msgSignal -> createMessageComponent(msgSignal.get()));

        // Message input
        TextField messageInput = new TextField();
        messageInput.setPlaceholder("Type your message and press Enter...");
        messageInput.setWidthFull();
        messageInput.setMaxLength(500);

        Runnable sendMessage = () -> {
            String text = messageInput.getValue();
            if (text != null && !text.trim().isEmpty() && sessionId != null) {
                String displayName = getCurrentDisplayName();
                muc01Signals.appendMessage(new MUC01Signals.Message(currentUser,
                        displayName, text.trim()));
                messageInput.clear();
            }
        };

        messageInput.addKeyPressListener(Key.ENTER, event -> sendMessage.run());

        Button sendButton = new Button("Send Message",
                event -> sendMessage.run());
        sendButton.addThemeName("primary");

        Button clearButton = new Button("Clear All Messages", event -> {
            muc01Signals.clearMessages();
        });
        clearButton.addThemeName("error");
        clearButton.addThemeName("small");

        // Message count
        Div messageCount = new Div();
        messageCount.bindText(muc01Signals.getMessagesSignal()
                .map(messages -> "Total messages: " + messages.size()));
        messageCount.getStyle().set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        add(title, description, activeUsersDisplay, new H3("Messages"),
                messagesContainer, messageCount, messageInput, sendButton,
                clearButton);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        this.sessionId = SessionIdHelper.getCurrentSessionId();
    }

    private Div createMessageComponent(MUC01Signals.Message message) {
        Div messageDiv = new Div();
        messageDiv.getStyle().set("background-color", "#ffffff")
                .set("border-left", "3px solid var(--lumo-primary-color)")
                .set("padding", "0.75em").set("margin-bottom", "0.5em")
                .set("border-radius", "4px").set("display", "flex")
                .set("gap", "0.75em");

        // Avatar
        Image avatar = new Image(
                MainLayout.getProfilePicturePath(message.username()), "");
        avatar.setWidth("40px");
        avatar.setHeight("40px");
        avatar.getStyle().set("border-radius", "50%").set("object-fit", "cover")
                .set("flex-shrink", "0");

        // Content area (header + text)
        Div contentArea = new Div();
        contentArea.getStyle().set("flex", "1");

        Div header = new Div();
        header.getStyle().set("display", "flex")
                .set("justify-content", "space-between")
                .set("margin-bottom", "0.5em");

        Div author = new Div();
        author.setText(message.author());
        author.getStyle().set("font-weight", "bold").set("color",
                "var(--lumo-primary-color)");

        Div timestamp = new Div();
        timestamp.setText(message.getFormattedTimestamp());
        timestamp.getStyle().set("font-size", "0.85em").set("color",
                "var(--lumo-secondary-text-color)");

        header.add(author, timestamp);

        Div text = new Div();
        text.setText(message.text());
        text.getStyle().set("color", "var(--lumo-body-text-color)");

        contentArea.add(header, text);
        messageDiv.add(avatar, contentArea);
        return messageDiv;
    }

    private String getCurrentDisplayName() {
        if (sessionId == null) {
            return currentUser;
        }

        var users = userSessionRegistry.getActiveUsersSignal().get();
        var displayNames = userSessionRegistry.getDisplayNamesSignal().get();

        // Find matching session key for current user
        String sessionKey = currentUser + ":" + sessionId;
        for (int i = 0; i < users.size() && i < displayNames.size(); i++) {
            if (sessionKey.equals(users.get(i).get().getCompositeKey())) {
                return displayNames.get(i);
            }
        }

        // Fallback to username
        return currentUser;
    }
}
