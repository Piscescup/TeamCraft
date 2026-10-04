# Adding a TeamCraft tab

For a Chinese guide to the rendering classes and where to make changes, see [GUI rendering responsibilities](gui-rendering.md).

`TeamcraftTab` extends Minecraft's `Screen`. Each tab is the actual displayed screen, not a content builder inside another screen.

Create a subclass of `TeamcraftTab` and register its constructor once during client initialization, before opening any configuration screen.

```java
import io.github.piscescup.fabricmc.teamcraft.gui.tab.TeamcraftTab;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftPageContext;
import net.minecraft.network.chat.Component;

public final class ExamplePage extends TeamcraftTab {
    public static final String ID = "example";

    public ExamplePage(TeamcraftPageContext context) {
        super(context, ID, "teamcraft.gui.nav.example", "teamcraft.gui.nav.example.tooltip");
    }

    @Override
    protected void build() {
        addSection("teamcraft.gui.example.section", () ->
            addTextRow(Component.translatable("teamcraft.gui.example.description"), null)
        );
    }

    @Override
    public boolean requiresServer() {
        return false; // Also available from Mod Menu without a supported server.
    }
}
```

Register it once in your client initializer:

```java
TeamcraftPages.register(ExamplePage.ID, ExamplePage::new);
```

Add the translation keys to the project's language provider (`TeamcraftTranslations`).
The tab is then included automatically; no navigation enum or screen switch needs editing.

## Extension points

- `build()`: add rows and controls using `addHeader`, `addSection`, `addTextRow`, `addValueRow`, `addLabeledWidget`, `addFullWidget`, or `addTwoWidgets`.
- `addResettableWidget(key, widget, tooltip, reset)`: a labeled control with a trailing Reset button. The callback restores only that field in the draft; the helper rebuilds the page. The label/row keeps its complete explanation, while Reset has its own short hint. Reset does not automatically save. Both controls are disabled while waiting for a server response.
- `addSection(id, label, subtitle, tooltip, initiallyExpanded, contents)`: dynamic collapsible rows, such as teams. Use a stable ID so renaming or refreshing keeps the expanded state; pass `false` to start collapsed. The original `addSection(key, contents)` still starts expanded.
- `button`, `cycleButton`, `textBox`, `numberBox`, and `colorDropdownButton`: create controls with the shared theme; `tooltip` creates a formatted explanation.
- `footerActions()`: optional custom footer buttons. The default is a single Close button. Use `closeAction()`, `action(label, callback)`, or `new FooterAction(key, callback, idleOnly)`.
- `headerHint()`: optional hint beside the screen title; the default is no hint.
- `showHoverTooltips()`: defaults to `true`; Help overrides it to disable hover tooltips without removing its inline text or collapsible groups.
- `TeamcraftButton.hoverHint(Component)`: a short description of the button action, separate from the row tooltip. Hovering a content button always suppresses the complete row explanation (even if disabled or without a hint). Non-button areas keep the row explanation; cycle controls get a short change-setting hint automatically.
- `requiresServer()`: defaults to `true`. Return `false` only for client-local pages; this controls visibility, not server authorization.
- `rebuildPage()`: rebuild controls after a local state change. Store page-local draft values in instance fields if they must survive a rebuild.
- `showFeedback(message, Type.SUCCESS / INFO / ERROR)`: show a modal result with this tab as the return screen. Pass either a `Component` or a translation key.

Pages are instantiated separately for every configuration session and retained while switching tabs or rebuilding controls.
Keep constructors limited to metadata and initial state; build widgets only in `build()` after Minecraft initializes the screen.
The base delegates row layout, clipping, scrolling and candidate input to `widget/TeamcraftContentList`, geometry to `layout`, and shared drawing to `render`. Its helper methods remain the page-building API. The shared
`TeamcraftPageContext` retains configuration drafts, the parent screen, page instances
and server-waiting state. Switching tabs displays the existing target page, so local
draft fields and expanded sections are retained.
To switch tabs from a page action, call `host.switchPage(targetId)`.
To return to the original parent, call `onClose()`.

Because a page is a `Screen`, it already has `font`, `minecraft`, `width`, `height`
and the usual lifecycle, drawing and input methods. Override them when necessary
and call `super` to retain common behavior. Usually only `build()` is needed.
The base constructor accepts `TeamcraftPageHost`; a page that does not use shared
TeamCraft draft data may accept this interface instead of the concrete context.

## Capability interfaces

- `TeamcraftPageHost`: title, navigation, close action and session flags. It does not render anything.
- `TeamcraftConfigResponseReceiver`: receives server snapshots. The page base forwards them to a supporting host; the details screen also implements it. Network dispatch does not depend on concrete screen classes.
- `TeamcraftCandidateEditor`: optional mutable candidate draft list for click-to-remove and drag-to-reorder. Only candidate-editing pages implement it; register selected rows with `registerCandidateRow(name)`.

Opening a client-local session directly uses `TeamcraftPageContext.help(parent).screen()`.
The normal in-world entry point remains `TeamcraftConfigClient.requestOpen(parent)`,
which requests a server snapshot before showing the selected page.

Registration order determines tab order. IDs must be unique, and the registration ID must match the ID passed to the page's base constructor.
Existing screens keep their page list; registrations affect newly opened screens.

## Result dialogs

GUI server responses use modal dialogs instead of short HUD overlays. Successful splits,
disbands and clears keep the existing All Teams return destination; other results return
to the current tab. Local validation errors leave the editable draft unchanged.
The initial server snapshot still opens the configuration directly, without an extra result dialog.

For a standard result from a tab:

```java
showFeedback(Component.translatable("teamcraft.gui.saved"),
    TeamcraftFeedbackScreen.Type.SUCCESS);
```

`TeamcraftFeedbackScreen` supports `SUCCESS` (green), `INFO` (yellow) and `ERROR` (red).
Its constructor also accepts a custom title. `PermissionDeniedScreen` keeps its dedicated
entry point and red permission message. Both extend `TeamcraftDialogScreen`.

For a dedicated dialog, extend `TeamcraftDialogScreen`:

```java
public final class ExampleDialog extends TeamcraftDialogScreen {
    public ExampleDialog(Screen parent) {
        super(parent, Component.literal("Example"), Component.literal("Your message"));
    }

    @Override
    protected int accentColor() {
        return 0xFF55FF55;
    }

    public static void open(Screen parent) {
        display(new ExampleDialog(parent));
    }
}
```

The base handles the dimmed parent, centered frame, text wrapping, scrolling for long
messages, resizing, Done button and Escape return. Override `doneLabel()` to change the
button label. Imports are in `io.github.piscescup.fabricmc.teamcraft.gui`; `Screen` and
`Component` are Minecraft types. Only the active dialog receives input, so clicks cannot
reach the parent. Late server responses are forwarded to a supporting parent receiver.
Opening another result replaces the previous dialog instead of nesting it.
