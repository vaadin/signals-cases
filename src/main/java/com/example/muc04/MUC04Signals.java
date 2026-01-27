package com.example.muc04;

import org.springframework.stereotype.Component;

import com.vaadin.signals.shared.SharedMapSignal;
import com.vaadin.signals.shared.SharedValueSignal;
import com.vaadin.signals.WritableSignal;

/**
 * Application-scoped signals for MUC04: Form Locking
 */
@Component
public class MUC04Signals {

    public record FieldLock(String username, String sessionId) {
    }

    private final WritableSignal<String> companyNameSignal = new SharedValueSignal<>(
            "");
    private final WritableSignal<String> addressSignal = new SharedValueSignal<>("");
    private final WritableSignal<String> phoneSignal = new SharedValueSignal<>("");
    // MapSignal where key is fieldName and value is FieldLock
    private final SharedMapSignal<FieldLock> fieldLocksSignal = new SharedMapSignal<>(
            FieldLock.class);

    public WritableSignal<String> getCompanyNameSignal() {
        return companyNameSignal;
    }

    public WritableSignal<String> getAddressSignal() {
        return addressSignal;
    }

    public WritableSignal<String> getPhoneSignal() {
        return phoneSignal;
    }

    public SharedMapSignal<FieldLock> getFieldLocksSignal() {
        return fieldLocksSignal;
    }

    public void lockField(String fieldName, String username, String sessionId) {
        fieldLocksSignal.put(fieldName, new FieldLock(username, sessionId));
    }

    public void unlockField(String fieldName, String username,
            String sessionId) {
        SharedValueSignal<FieldLock> lockSignal = fieldLocksSignal.value()
                .get(fieldName);
        if (lockSignal != null) {
            FieldLock lock = lockSignal.value();
            if (lock != null && username.equals(lock.username())
                    && sessionId.equals(lock.sessionId())) {
                fieldLocksSignal.remove(fieldName);
            }
        }
    }

    public boolean isFieldLockedByOther(String fieldName, String username,
            String sessionId) {
        SharedValueSignal<FieldLock> lockSignal = fieldLocksSignal.value()
                .get(fieldName);
        if (lockSignal == null) {
            return false;
        }
        FieldLock lockOwner = lockSignal.value();
        return lockOwner != null && !(username.equals(lockOwner.username())
                && sessionId.equals(lockOwner.sessionId()));
    }
}
