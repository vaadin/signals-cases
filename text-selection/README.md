# Text Selection API — use cases

A standalone Spring Boot demo of the Text Selection API for Vaadin Flow text
inputs. The API is implemented on the
[`select-api` branch of `vaadin/flow-components`](https://github.com/vaadin/flow-components/tree/select-api),
addressing
[vaadin/flow-components#1377](https://github.com/vaadin/flow-components/issues/1377)
and continuing the work in Matti Tahvonen's
[PR #3194](https://github.com/vaadin/flow-components/pull/3194) (the same
shape has shipped in [Viritin](https://github.com/viritin/flow-viritin)
for years). Build that branch into the local Maven repo to run this module.

| # | View | What it shows |
| - | ---- | ------------- |
| UC1 | Select all on focus | Auto-selects the value on focus so typing overwrites it. Server-driven, per-event control — not the same as the static `autoselect` attribute. |
| UC2 | Find and highlight in textarea | "Find next" walks the user through matches by selecting each one in place; wraps to the start past the last match. |
| UC3 | Jump to validation error | On a failed submit, the offending substring is selected so the user can immediately retype it. |
| UC4 | Insert template at cursor | Snippet buttons insert at the current cursor position; if the snippet has a placeholder it is left selected for the user to type over. |
| UC5 | Live selection info | Side panel reactively shows range, length, word count and a preview of the current selection — bindings are computed from `selectionSignal()`. |
| UC6 | Selection-driven transform toolbar | Toolbar of UPPERCASE / lowercase / "Quote" / Trim, enabled only when there is a selection; transforms replace in place and re-select the result so actions chain. |

## API surface

`TextField`, `TextArea`, and the other `TextFieldBase` subclasses (Email,
Password, Integer/Number/BigDecimalField) implement
`com.vaadin.flow.component.shared.HasSelection`:

```java
public interface HasSelection extends HasElement {
    void selectAll();
    void deselect(); // collapse to selectionEnd, leaving the cursor there
    void setSelectionRange(int selectionStart, int selectionEnd);
    void setCursorPosition(int position);
    Signal<SelectionRange> selectionSignal();
}

public record SelectionRange(int start, int end, String content) {
    public int length() { return end - start; }
    public boolean isEmpty() { return start == end; }
    public static SelectionRange empty();
}
```

Names mirror `HTMLInputElement.setSelectionRange()` / `selectionStart` /
`selectionEnd`. The reactive `selectionSignal()` replaces the original
async `getSelectionRange(callback)` from PR #3194: now that the Vaadin
event-ordering bug is fixed, the client pushes the current selection on
every change rather than the server pulling on demand. The signal value
carries `content` so views don't have to slice the field's value manually.

Clipboard integration (`copyToClipboard()` and friends) is intentionally
out of scope for this round and will be tackled separately.

## Run

```
cd text-selection
mvn spring-boot:run
```

Open <http://localhost:8080/>.
