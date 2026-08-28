package com.example.uc5;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * A bounded, application-wide log of what browsers said their errors were.
 * <p>
 * Application-scoped on purpose: a script fails in a tab nobody is watching,
 * and that tab is rarely the one an operator is looking at. So the readout has
 * to collect what <em>every</em> browser sent, the way the Observability Kit's
 * interaction insights do.
 * <p>
 * And that is the shape this should have had: the kit's insight buffer already
 * groups by fingerprint with an occurrence count, hashes the session id,
 * truncates messages and filters framework stack frames — none of which this
 * does. See {@code API-GAPS.md} #5.
 *
 * @see ClientErrorReporter
 */
@Component
public class ClientErrorLog {

    /**
     * One error a browser reported.
     *
     * @param recordedAt
     *            when the server received it
     * @param client
     *            which browser tab reported it, as a UI id and a short hash of
     *            the session id — the kit withholds the session id itself, and
     *            so does this
     * @param kind
     *            {@code uncaught} or {@code promise}, matching the {@code kind}
     *            tag on the kit's {@code vaadin.client.errors} counter, so a
     *            row here can be lined up with the count there
     * @param message
     *            what the browser said went wrong
     * @param where
     *            the first stack frame, or the script and line when the error
     *            carried no stack
     */
    public record BrowserError(Instant recordedAt, String client, String kind,
            String message, String where) {
    }

    /**
     * Enough to show a pattern, few enough that a misbehaving client cannot
     * grow the heap. Oldest entries are dropped.
     */
    static final int CAPACITY = 100;

    private final Deque<BrowserError> errors = new ArrayDeque<>();

    /** Adds one error, evicting the oldest once {@link #CAPACITY} is hit. */
    public synchronized void add(BrowserError error) {
        errors.addFirst(error);
        while (errors.size() > CAPACITY) {
            errors.removeLast();
        }
    }

    /** A snapshot of what has been reported, newest first. */
    public synchronized List<BrowserError> recent() {
        return List.copyOf(errors);
    }

    /** Forgets everything reported so far. */
    public synchronized void clear() {
        errors.clear();
    }
}
