# ChatSpliter

[中文](README.md) | [English](README_EN.md)

A client-side Minecraft Fabric mod that splits the vanilla chat into multiple independent HUD windows using keyword filtering. Each window is fully customizable.

## Features

- **Keyword Filtering** — Route chat messages to separate HUD windows based on keywords
- **Three Match Modes** — Any keyword, all keywords, or regex pattern matching
- **Fully Customizable Windows** — Each filter group has independent settings for position, size, scale, opacity, colors, and more
- **Text Scaling** — Scale text and layout per window
- **Text Alignment** — Left or right text alignment
- **Scroll Direction** — Vanilla bottom-up, top-down (newest on top), or top-anchored (auto-shift to fill gaps)
- **Vanilla-Style Fade** — Messages fade out based on configurable display and fade duration
- **Auto Word Wrap** — Long messages automatically wrap to fit window width
- **Chat History** — Press `T` to open chat, then scroll through filtered HUD history
- **Drag to Resize/Move** — Open chat (`T`) and drag window borders to reposition or resize
- **Hide from Main Chat** — Optionally remove matched messages from the vanilla chat
- **One-Click Settings** — Gear button on each HUD (visible when chat is open) jumps directly to that group's settings
- **Customizable Keybind** — Config key shown in vanilla Controls → Miscellaneous

## Requirements

- Minecraft **1.21.8**
- Fabric Loader **>= 0.16.9**
- Java **21+**
- No other dependencies (pure mixin-based, no Fabric API required)

## Usage

| Action | How |
|--------|-----|
| Open config | Press `K` (customizable in Controls → Miscellaneous) |
| View chat history | Press `T`, then scroll in filtered HUDs |
| Move a HUD | Press `T`, drag the window title area |
| Resize a HUD | Press `T`, drag the bottom-right corner |
| Open group settings | Press `T`, click the ⚙ gear button |

## Configuration

Settings are saved in `.minecraft/config/chatspliter.json`.

### Global Settings

| Setting | Description |
|---------|-------------|
| Enabled | Master toggle for the entire mod |
| Hide matched from main | Remove matched messages from vanilla chat |

### Per-Group Settings

| Setting | Default | Description |
|---------|---------|-------------|
| Name | `New Group` | Display name |
| Keywords | `[]` | Comma-separated keywords to match |
| Match Mode | Any | `Any` / `All` / `Regex` |
| X / Y | `2, 2` | Window position (pixels) |
| Width / Height | `320, 180` | Window size (pixels) |
| Text Scale | `1.0` | Scale multiplier for text and layout |
| Display Time | `15` | Total seconds before message disappears |
| Fade Time | `10` | Seconds of fade-out at end of display time |
| Background Opacity | `0.5` | Background darkness (0–1) |
| Text Opacity | `1.0` | Text visibility (0–1) |
| Line Spacing | `0` | Extra pixels between lines |
| Max Lines | `100` | Maximum buffered message lines |
| Show Timestamp | Off | Prepend `HH:mm:ss` to each message |
| Show Title | On | Show group name on the window |
| Text Align | Left | `Left` or `Right` |
| Scroll Direction | Bottom Up | `Bottom Up` / `Top Down` / `Top Anchored` |

### Scroll Direction Modes

| Mode | Behavior |
|------|----------|
| **Bottom Up** | Vanilla style — newest message at the bottom |
| **Top Down** | Reverse — newest message at the top |
| **Top Anchored** | Oldest at top; when old messages fade out, newer messages shift up to fill the gap |

## Default Filter Groups

The mod creates three example groups on first launch:

| Group | Keywords |
|-------|----------|
| Party | `party` |
| Whisper | `whispers`, `whisper to you` |
| System | `[System]`, `[Server]` |

## Building

```bash
./gradlew build
# Output: build/libs/chatspliter-1.0.0.jar
```

For VS Code, install **Extension Pack for Java** and **Gradle for Java**, then open the project folder and run the `build` Gradle task.

## License

MIT
