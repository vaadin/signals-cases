package com.example.uc5;

import java.io.Serializable;

/**
 * One browser error, as the in-page reporter sends it, deserialized by Flow's
 * JSON codec.
 * <p>
 * Shaped like the Observability Kit's own {@code ClientSample} — a plain bean
 * with setters, because that is what Flow's codec binds to — but carrying the
 * fields a sample cannot: the message, where it came from and the first stack
 * frame. That is the whole reason this type exists. The kit counts browser
 * errors in {@code vaadin.client.errors} and its ingest allowlist keeps nothing
 * else, and a message could not travel as a meter tag anyway without making the
 * meter's cardinality unbounded.
 *
 * @see ClientErrorReporter
 */
public class ClientErrorReport implements Serializable {

    private String kind = "";
    private String message = "";
    private String source = "";
    private String frame = "";

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getFrame() {
        return frame;
    }

    public void setFrame(String frame) {
        this.frame = frame;
    }
}
