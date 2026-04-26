package com.example.usecase21;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.signals.Signal;

/**
 * Session-scoped service that bridges ResourceBundle and Signals for reactive
 * i18n.
 *
 * Each call to t(key) returns a computed signal that recomputes when the locale
 * changes. ResourceBundle.getBundle() has built-in caching so repeated calls
 * are efficient.
 */
public class TranslationService {

    private static final String BUNDLE_NAME = "i18n.messages";

    /**
     * Returns a computed signal that provides the translated string for the
     * given key. The signal automatically recomputes when the locale changes.
     *
     * @param key
     *            the translation key
     * @return a Signal containing the translated string, or "!key!" if not
     *         found
     */
    public static Signal<String> translate(String key) {
        return Signal.computed(() -> {
            Locale locale = UI.getCurrent().localeSignal().get();
            try {
                ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME,
                        locale);
                if (bundle.containsKey(key)) {
                    return bundle.getString(key);
                }
            } catch (MissingResourceException e) {
                // Fall through to return missing key indicator
            }
            return "!" + key + "!";
        });
    }
}
