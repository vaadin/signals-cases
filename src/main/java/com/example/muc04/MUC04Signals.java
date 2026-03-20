package com.example.muc04;

import org.springframework.stereotype.Component;

import com.vaadin.flow.signals.shared.SharedMapSignal;
import com.vaadin.flow.signals.shared.SharedValueSignal;

/**
 * Application-scoped signals for MUC04: Collaborative Form Editing.
 */
@Component
public class MUC04Signals {

    public record FieldLock(String username, String sessionId) {
    }

    private final SharedValueSignal<String> companyNameSignal =
            new SharedValueSignal<>("");
    private final SharedValueSignal<String> addressSignal =
            new SharedValueSignal<>("");
    private final SharedValueSignal<String> phoneSignal =
            new SharedValueSignal<>("");
    private final SharedMapSignal<FieldLock> fieldLocksSignal =
            new SharedMapSignal<>(FieldLock.class);

    public SharedValueSignal<String> getCompanyNameSignal() {
        return companyNameSignal;
    }

    public SharedValueSignal<String> getAddressSignal() {
        return addressSignal;
    }

    public SharedValueSignal<String> getPhoneSignal() {
        return phoneSignal;
    }

    public SharedMapSignal<FieldLock> getFieldLocksSignal() {
        return fieldLocksSignal;
    }

    public void startEditing(String fieldName, String username,
            String sessionId) {
        fieldLocksSignal.put(fieldName, new FieldLock(username, sessionId));
    }

    public void stopEditing(String fieldName, String username,
            String sessionId) {
        SharedValueSignal<FieldLock> lockSignal =
                fieldLocksSignal.peek().get(fieldName);
        if (lockSignal != null) {
            FieldLock lock = lockSignal.peek();
            if (lock != null && username.equals(lock.username())
                    && sessionId.equals(lock.sessionId())) {
                fieldLocksSignal.remove(fieldName);
            }
        }
    }
}
