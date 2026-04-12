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
    private final SharedValueSignal<Boolean> lockingEnabledSignal = new SharedValueSignal<>(
            false);
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

    public SharedValueSignal<Boolean> getLockingEnabledSignal() {
        return lockingEnabledSignal;
    }

    public SharedListSignal<User> getFieldEditors(String fieldName) {
        return fieldEditors.computeIfAbsent(fieldName,
                k -> new SharedListSignal<>(User.class));
    }

    public void startEditing(String fieldName, User user) {
        getFieldEditors(fieldName).insertLast(user);
    }

    public void stopEditing(String fieldName, User user) {
        getFieldEditors(fieldName).peek().stream().filter(entry -> {
            var u = entry.peek();
            return u != null && u.id() == user.id();
        }).findFirst().ifPresent(getFieldEditors(fieldName)::remove);
    }

    /**
     * Update the display name for a user across all field editor lists.
     */
    public void updateUserName(int userId, String newName) {
        for (var editors : fieldEditors.values()) {
            for (var entry : editors.peek()) {
                var user = entry.peek();
                if (user != null && user.id() == userId) {
                    entry.set(new User(user.id(), newName, user.colorIndex()));
                }
            }
        }
    }
}
