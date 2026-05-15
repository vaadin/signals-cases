// Client-side factory for FlashAction (custom action — UC5).
// Pairs with com.example.uc5.FlashAction on the server. Registers a
// "demo:flash" action against window.Vaadin.Flow.triggers, which is the
// registry installed by flow-client/Triggers.ts.

interface TriggersRegistry {
  registerAction(
    typeId: string,
    factory: (
      config: Record<string, unknown>,
      extras: HTMLElement[]
    ) => { run(): void }
  ): void;
}

const triggers = (window as unknown as {
  Vaadin?: { Flow?: { triggers?: TriggersRegistry } };
}).Vaadin?.Flow?.triggers;

if (!triggers) {
  console.debug(
    'window.Vaadin.Flow.triggers not available — flash-action.ts loaded too early'
  );
} else {
  triggers.registerAction('demo:flash', (config, extras) => {
    const elementIndex = Number(config.element ?? 0);
    return {
      run() {
        // elementIndex 0 means "host"; this action only supports an extra
        // target. extras[i] is the (i+1)-th referenced element.
        const target =
          elementIndex === 0 ? null : extras[elementIndex - 1] ?? null;
        if (!target) {
          return;
        }
        const originalBg = target.style.backgroundColor;
        const originalTransition = target.style.transition;
        target.style.transition = 'background-color 200ms';
        target.style.backgroundColor = 'var(--aura-yellow, gold)';
        window.setTimeout(() => {
          target.style.backgroundColor = originalBg;
          window.setTimeout(() => {
            target.style.transition = originalTransition;
          }, 220);
        }, 220);
      }
    };
  });
}

export {};
