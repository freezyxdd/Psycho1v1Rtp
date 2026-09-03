<p align="center">
  <img src="docs/rtpqueue-banner.webp" alt="Psycho1v1Rtp - Folia RTP Queue Plugin" width="100%">
</p>

<h1 align="center">Psycho1v1Rtp</h1>

<p align="center">
  A lightweight 1v1 RTP matchmaking plugin for modern Minecraft PvP servers.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/version-2.4.0-blue" alt="Version 2.4.0">
  <img src="https://img.shields.io/badge/Java-21-orange" alt="Java 21">
  <img src="https://img.shields.io/badge/Minecraft-1.21%2B-green" alt="Minecraft 1.21+">
  <img src="https://img.shields.io/badge/Folia-supported-purple" alt="Folia supported">
  <img src="https://img.shields.io/badge/license-MIT-lightgrey" alt="MIT License">
</p>

## Overview

**Psycho1v1Rtp** provides a simple 1v1 matchmaking queue for PvP servers.

Players use `/1v1` to enter the queue. As soon as two players are available, the plugin selects a configured world, searches for a safe random location and teleports both players into the match.

It is designed for servers such as:

- Survival / SMP
- Crystal PvP
- Practice PvP
- KitPvP
- Hardcore
- Competitive PvP servers

## Features

- Smart 2-player matchmaking queue
- `/1v1` join/leave command
- `/rtpqueue` alias
- Automatic matchmaking when two players are queued
- Safe random teleport location search
- Fixed or random match world selection
- Configurable teleport radius
- Optional full armor requirement
- Configurable player separation distance
- Optional face-to-face spawning
- Configurable sounds
- Fully customizable MiniMessage messages
- English and Brazilian Portuguese language files
- Automatic English fallback for missing translations
- `/ps1v1 reload` admin command with tab completion
- Folia-aware region and entity scheduling

## How it works

1. Player 1 uses `/1v1` and enters the queue.
2. Player 2 uses `/1v1` and enters the queue.
3. Psycho1v1Rtp automatically creates the match.
4. A configured world is selected.
5. The plugin searches for a safe RTP location.
6. Both players are placed apart using the configured distance.
7. If `face-to-face.enabled` is enabled, both players are rotated to face each other.

If an opponent disconnects or becomes unavailable while the match is being prepared, the remaining player is returned to the queue instead of being silently removed.

If a safe teleport location cannot be found, the players are also returned to the queue and receive a configurable message.

## Commands

| Command | Description | Permission |
| --- | --- | --- |
| `/1v1` | Join or leave the 1v1 queue | `psrtpqueue.use` |
| `/rtpqueue` | Alias for `/1v1` | `psrtpqueue.use` |
| `/ps1v1 reload` | Reload configuration and language files | `ps1v1.admin` |

## Permissions

| Permission | Description | Default |
| --- | --- | --- |
| `psrtpqueue.use` | Allows players to use the 1v1 queue | Everyone |
| `ps1v1.admin` | Allows `/ps1v1 reload` | OP |

## Installation

1. Download the plugin JAR.
2. Place it inside your server's `plugins` folder.
3. Start or restart the server.
4. Configure `config.yml` and the desired language file.
5. Use `/ps1v1 reload` after making configuration changes.

The plugin requires **Java 21**.

## File structure

After the first startup, the plugin creates its configuration and language files:

```text
plugins/1v1/
├── config.yml
└── languages/
    ├── en.yml
    └── pt_BR.yml
```

## Configuration

Default `config.yml`:

```yaml
# ============================================
#              Psycho1v1Rtp
# ============================================

# Plugin language.
# Available bundled languages:
# - en
# - pt_BR
language: "en"

settings:
  # Require helmet, chestplate, leggings and boots
  # before a player can join the queue.
  require-full-armor: true

  # Maximum RTP distance from the selected world's spawn.
  teleport-radius: 5000

  # Match world selection.
  world-selection:
    # false = always use default-world
    # true  = randomly select a loaded world from worlds
    random-world: false

    # Used when random-world is false.
    default-world: "world"

    # Used when random-world is true.
    worlds:
      - "world"
      - "newworld"
      - "test"

  # Players are always separated using distance.
  # When enabled is true, they will also face each other.
  face-to-face:
    enabled: true
    distance: 10.0

# ============================================
# Sounds
# ============================================

sounds:
  searching:
    sound: "ui.button.click"
    volume: 1.0
    pitch: 1.0

  cancelled:
    sound: "entity.item.break"
    volume: 1.0
    pitch: 1.0

  found:
    sound: "entity.ender_dragon.growl"
    volume: 1.0
    pitch: 1.0

  error:
    sound: "entity.item.break"
    volume: 1.0
    pitch: 1.2
```

### World selection

To always use one world:

```yaml
world-selection:
  random-world: false
  default-world: "world"
```

To randomly select between multiple worlds:

```yaml
world-selection:
  random-world: true
  worlds:
    - "world"
    - "world_nether"
    - "pvp_world"
```

Only loaded and valid worlds are considered when random world selection is enabled.

### Face-to-face spawning

```yaml
face-to-face:
  enabled: true
  distance: 10.0
```

`distance` controls how far apart the two players spawn.

When `enabled` is `true`, the plugin additionally adjusts their yaw and pitch so both players look directly at each other.

## Languages and custom messages

Bundled languages:

- `en` — English
- `pt_BR` — Brazilian Portuguese

Select the language in `config.yml`:

```yaml
language: "en"
```

or:

```yaml
language: "pt_BR"
```

The aliases `pt`, `ptbr` and `pt-br` are also accepted.

Every player-facing and admin message can be edited in the language files using **MiniMessage** formatting.

Example from `languages/en.yml`:

```yaml
messages:
  searching: "<gray>Searching for an opponent. Type <white>/1v1</white> again to leave.</gray>"
  cancelled: "<gray>You left the 1v1 queue.</gray>"
  found: "<green>1v1 found! Teleporting...</green>"
  no-armor: "<red>You must wear a helmet, chestplate, leggings and boots.</red>"
  only-player: "<red>Only players can use this command.</red>"
  no-permission: "<red>You do not have permission to use this command.</red>"
  admin-usage: "<gray>Usage: <white>/ps1v1 reload</white></gray>"
  admin-reload-success: "<green>Configuration and language files reloaded successfully.</green>"
  admin-unknown: "<red>Unknown subcommand. Use <white>/ps1v1 reload</white>.</red>"
  no-valid-world: "<red>No valid 1v1 world is available. Please contact a server administrator.</red>"
  teleport-failed: "<red>A safe teleport location could not be found. You were returned to the queue.</red>"
  opponent-unavailable: "<yellow>Your opponent became unavailable. You were returned to the queue.</yellow>"
```

You can customize colors and formatting, for example:

```yaml
found: "<gradient:#5865F2:#EB459E><bold>Opponent found!</bold></gradient> <gray>Teleporting...</gray>"
```

If a message is missing from the selected language file, Psycho1v1Rtp automatically falls back to the default English message.

After changing `config.yml` or a language file, run:

```text
/ps1v1 reload
```

## Compatibility

- Java 21
- Minecraft 1.21+
- Paper
- Purpur
- Folia

The plugin declares `folia-supported: true` and uses Paper's entity, global-region and region schedulers for Folia-aware operations.

## Building from source

Clone the repository and run:

```bash
./gradlew build
```

The compiled plugin will be generated inside:

```text
build/libs/
```

## Support

For support, questions, bug reports or suggestions, join the PsychoStudios Discord server:

https://discord.gg/6smGe5Ujvc

## License

This project is licensed under the **MIT License**.

---

<p align="center">
  Made by <strong>PsychoStudios</strong>
</p>
