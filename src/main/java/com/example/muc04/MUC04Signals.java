package com.example.muc04;

import java.util.HashMap;
import java.util.Map;
import com.example.muc04.SignalFieldHighlighter.User;
import org.springframework.stereotype.Component;
import com.vaadin.flow.signals.shared.SharedListSignal;
import com.vaadin.flow.signals.shared.SharedValueSignal;

/**
 * Application-scoped signals for MUC04: Collaborative Form Editing.
 */
@Component
public class MUC04Signals {

    private final SharedValueSignal<String> companyNameSignal = new SharedValueSignal<>(
            "");
    private final SharedValueSignal<String> addressSignal = new SharedValueSignal<>(
            "");
    private final SharedValueSignal<String> phoneSignal = new SharedValueSignal<>(
            "");
    private final Map<String, SharedListSignal<User>> fieldEditors = new HashMap<>();

    public SharedValueSignal<String> getCompanyNameSignal() {
        return companyNameSignal;
    }

    public SharedValueSignal<String> getAddressSignal() {
        return addressSignal;
    }

    public SharedValueSignal<String> getPhoneSignal() {
        return phoneSignal;
    }

    public SharedListSignal<User> getFieldEditors(String fieldName) {
        return fieldEditors.computeIfAbsent(fieldName,
                k -> new SharedListSignal<>(User.class));
    }

    public void startEditing(String fieldName, User user) {
        var editors = getFieldEditors(fieldName);
        boolean alreadyEditing = editors.peek().stream()
                .map(entry -> entry.peek())
                .anyMatch(u -> u != null && u.sessionId().equals(user.sessionId()));
        if (!alreadyEditing) {
            editors.insertLast(user);
        }
    }

    public void stopEditing(String fieldName, User user) {
        getFieldEditors(fieldName).peek().stream().filter(entry -> {
            var u = entry.peek();
            return u != null && u.sessionId().equals(user.sessionId());
        }).findFirst().ifPresent(getFieldEditors(fieldName)::remove);
    }
}
