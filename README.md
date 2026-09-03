# Psycho1v1Rtp

Simple 1v1 RTP queue plugin for Minecraft servers.

Players use `/1v1` to join the queue. When two players are available, the plugin finds a safe random location and teleports them to the match.

Supports Paper, Purpur and Folia.

## Features

- 1v1 matchmaking queue
- `/1v1` to join or leave the queue
- `/rtpqueue` alias
- Safe random teleport
- Fixed or random world selection
- Configurable teleport radius
- Optional full armor requirement
- Configurable distance between players
- Optional face-to-face teleport
- Configurable sounds
- English and Brazilian Portuguese
- Custom messages with MiniMessage
- `/ps1v1 reload`

## Commands

| Command | Description | Permission |
| --- | --- | --- |
| `/1v1` | Join or leave the queue | `psrtpqueue.use` |
| `/rtpqueue` | Alias for `/1v1` | `psrtpqueue.use` |
| `/ps1v1 reload` | Reload config and language files | `ps1v1.admin` |

## Installation

1. Download the plugin JAR.
2. Put it inside the `plugins` folder.
3. Start the server.
4. Edit the config if needed.

Requires Java 21.

## Configuration

Default `config.yml`:

```yaml
language: "en"

settings:
  require-full-armor: true
  teleport-radius: 5000

  world-selection:
    random-world: false
    default-world: "world"
    worlds:
      - "world"
      - "newworld"
      - "test"

  face-to-face:
    enabled: true
    distance: 10.0

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

### Worlds

To always use the same world:

```yaml
world-selection:
  random-world: false
  default-world: "world"
```

To randomly choose between multiple worlds:

```yaml
world-selection:
  random-world: true
  worlds:
    - "world"
    - "world_nether"
    - "pvp_world"
```

### Face to face

```yaml
face-to-face:
  enabled: true
  distance: 10.0
```

`distance` controls how far apart the players are teleported. If `enabled` is `true`, they will also face each other.

## Languages

Available languages:

- `en`
- `pt_BR`

Choose one in `config.yml`:

```yaml
language: "pt_BR"
```

The plugin also accepts `pt`, `ptbr` and `pt-br`.

Language files are created in:

```text
plugins/1v1/languages/
```

All messages can be changed there using MiniMessage.

Example:

```yaml
messages:
  searching: "<gray>Searching for an opponent...</gray>"
  found: "<green>1v1 found! Teleporting...</green>"
```

After changing the config or language files, use:

```text
/ps1v1 reload
```

## Permissions

| Permission | Default |
| --- | --- |
| `psrtpqueue.use` | Everyone |
| `ps1v1.admin` | OP |

## Building

```bash
./gradlew build
```

The JAR will be generated in `build/libs/`.

## Support

Discord: https://discord.gg/6smGe5Ujvc

## License

MIT License.
