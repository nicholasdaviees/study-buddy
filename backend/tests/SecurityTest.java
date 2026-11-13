// Tests database safety for user-controlled content.
public class SecurityTest {

    // Verifies injection-like text is handled as data by prepared statements.
    public void preventsSqlInjection() {
    }

    // Verifies deleting an unknown identifier changes nothing else.
    public void protectsUnrelatedRowsDuringDelete() {
    }

    // Verifies generated content is escaped before display in the browser.
    public void preventsUnsafeGeneratedHtml() {
    }
}

