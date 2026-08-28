package com.example.uc5;

import java.time.Instant;
import java.util.List;

import com.example.uc5.ClientErrorLog.BrowserError;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.observability.micrometer.MeterNames;

/**
 * What is left of {@code API-GAPS.md} #5 now that the kit collects connection
 * state: the <em>detail</em> of a browser error.
 * <p>
 * The Observability Kit's in-browser collector listens to the same two global
 * events this does and records {@link MeterNames#CLIENT_ERRORS}, tagged
 * {@code uncaught} or {@code promise}. That is a count and nothing else — the
 * message, the script it came from and the stack are all dropped at the point
 * of collection, and the ingest allowlist would reject them anyway. "Errors
 * went up by four" is a smoke alarm, not a diagnosis, so this hidden component
 * listens alongside the collector and keeps what it discards, in
 * {@link ClientErrorLog}.
 * <p>
 * It records no meter. The kit already counts these, and a message is not a
 * number — nor a tag: putting free-form browser text on a meter would make its
 * cardinality unbounded. Detail belongs where the kit puts the detail of a
 * failed interaction, in an insight.
 * <p>
 * <b>What an application-level reporter cannot do.</b> It sends immediately and
 * lets Flow's own pending-message queue deliver the call if the connection is
 * down — which works, but means it cannot say how long the report waited. The
 * collector measures exactly that, as {@code ClientSample.ageMs}, on the
 * browser's own clock, and buffers through an outage into {@code
 * sessionStorage} so a reload does not lose it. None of that machinery is
 * reachable from application code: {@code recordSamples} takes the kit's own
 * sample names and nothing else. Rebuilding it here would be the third
 * implementation of the same buffer.
 * <p>
 * It is also attached by a view rather than by the framework, so only a browser
 * sitting on this route is being watched, where the kit's collector is attached
 * to every UI.
 *
 * @see <a href=
 *      "https://github.com/vaadin/use-cases/blob/main/observability/API-GAPS.md">API-GAPS.md</a>
 */
@Tag("uc5-client-error-reporter")
public class ClientErrorReporter extends Component {

    /**
     * Cap on one batch. The kit rate-limits its own client ingest per session
     * and counts what it refuses as {@link MeterNames#CLIENT_THROTTLED}; this
     * has no such accounting, which is another reason the reporting belongs on
     * the collector's side of the wire.
     */
    static final int MAX_PER_CALL = 50;

    /**
     * Installs the listeners once per page and keeps them pointed at the
     * currently attached reporter, so navigating away and back does not stack a
     * second set of window listeners on a stale element.
     * <p>
     * Deliberately does <em>not</em> subscribe to
     * {@code window.Vaadin.connectionState}: that is the collector's job now,
     * and a second subscriber would be a second opinion on the same signal.
     */
    private static final String INSTALL = """
            const el = this;
            if (window.__uc5errors) {
                window.__uc5errors.el = el;
                return;
            }
            const reporter = { el: el };
            window.__uc5errors = reporter;

            function firstFrame(error) {
                if (!error || !error.stack) {
                    return '';
                }
                const lines = String(error.stack).split('\\n');
                for (let i = 0; i < lines.length; i++) {
                    if (lines[i].indexOf('at ') >= 0) {
                        return lines[i].trim();
                    }
                }
                return lines[0].trim();
            }

            function report(entry) {
                const target = reporter.el;
                if (!target || !target.$server) {
                    return;
                }
                try {
                    // No buffering: if the connection is down, Flow queues the
                    // invocation and sends it on reconnect. What is lost is
                    // knowing how long it waited -- the collector measures
                    // that for its own samples and takes no others.
                    target.$server.reportErrors([entry]);
                } catch (e) {
                    /* nothing to fall back to; the kit still has the count */
                }
            }

            window.addEventListener('error', function (event) {
                const error = event.error;
                report({
                    kind: 'uncaught',
                    message: String(event.message
                        || (error && error.message) || 'Error'),
                    source: (event.filename || location.pathname) + ':'
                        + (event.lineno || 0),
                    frame: firstFrame(error)
                });
            });

            window.addEventListener('unhandledrejection', function (event) {
                const reason = event.reason;
                report({
                    kind: 'promise',
                    message: String((reason && reason.message)
                        || reason || 'Unhandled rejection'),
                    source: location.pathname,
                    frame: firstFrame(reason)
                });
            });
            """;

    private final transient ClientErrorLog log;
    private final SerializableRunnable onReport;

    /** Which browser tab this reporter watches; resolved on attach. */
    private String client = "unknown";

    /**
     * @param log
     *            the application-wide error log, so a tab that has since been
     *            closed is still readable from another one
     * @param onReport
     *            run after a report is recorded, on the reporting UI's thread;
     *            lets the hosting view repaint without polling
     */
    public ClientErrorReporter(ClientErrorLog log,
            SerializableRunnable onReport) {
        this.log = log;
        this.onReport = onReport;
        getElement().getStyle().set("display", "none");
    }

    @Override
    protected void onAttach(AttachEvent event) {
        super.onAttach(event);
        client = label(event.getUI());
        getElement().executeJs(INSTALL);
    }

    /**
     * Receives browser errors, with the detail the kit's counter does not keep.
     *
     * @param errors
     *            the reports, oldest first
     */
    @ClientCallable
    public void reportErrors(List<ClientErrorReport> errors) {
        if (errors == null || errors.isEmpty()) {
            return;
        }
        List<ClientErrorReport> batch = errors.size() > MAX_PER_CALL
                ? errors.subList(0, MAX_PER_CALL)
                : errors;
        for (ClientErrorReport error : batch) {
            String where = error.getFrame().isBlank() ? error.getSource()
                    : error.getFrame();
            log.add(new BrowserError(Instant.now(), client,
                    error.getKind().isBlank() ? "error" : error.getKind(),
                    error.getMessage(), where));
        }
        onReport.run();
    }

    /**
     * Names the reporting tab without exposing the session id: a UI id, plus a
     * short hash of the session, which is what the kit does with the session id
     * in its insight payload.
     */
    private static String label(UI ui) {
        VaadinSession session = ui.getSession();
        String id = session == null ? "" : session.getSession().getId();
        return "tab %d/%04x".formatted(ui.getUIId(), id.hashCode() & 0xffff);
    }
}
