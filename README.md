# 1v1RtpQueue

A lightweight Minecraft 1v1 queue plugin for Paper, Purpur and Folia-compatible servers.

## Version

Current development version: **2.4.0**

## Features

- `/1v1` command-based matchmaking queue
- `/rtpqueue` alias
- `/ps1v1 reload` admin command
- Random or fixed world selection
- Configurable random teleport radius
- Safe random teleport location search
- Optional face-to-face spawning
- Configurable minimum armor requirement
- Configurable sounds
- Fully customizable MiniMessage messages
- English and Brazilian Portuguese language files
- Folia-aware entity and region scheduling

## Languages

The plugin creates the following files on first startup:

```text
plugins/1v1/
├─ config.yml
└─ languages/
   ├─ en.yml
   └─ pt_BR.yml
```

Choose the language in `config.yml`:

```yaml
language: "en"
```

or:

```yaml
language: "pt_BR"
```

Aliases such as `pt`, `ptbr` and `pt-br` are also accepted.

All player-facing and admin command messages can be customized in the selected language file using MiniMessage formatting. If a message is missing from the selected language, the plugin automatically falls back to the English version.

After changing `config.yml` or a language file, run:

```text
/ps1v1 reload
```

## Main configuration

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

  face-to-face:
    enabled: true
    distance: 10.0
```

Players are always separated by the configured distance. When `face-to-face.enabled` is `true`, their yaw and pitch are additionally adjusted so they face each other.

## Permissions

| Permission | Description | Default |
| --- | --- | --- |
| `psrtpqueue.use` | Allows `/1v1` and `/rtpqueue` | Everyone |
| `ps1v1.admin` | Allows `/ps1v1 reload` | OP |

## Requirements

- Java 21
- Paper/Purpur API 1.21+

The project declares `folia-supported: true` and uses Paper's entity, global-region and region schedulers for Folia-aware operations.

## Building

```bash
./gradlew build
```

The compiled JAR will be generated in `build/libs/`.

## Support

For support, questions or suggestions, join the Discord server:

https://discord.gg/6smGe5Ujvc

## License

This project is licensed under the MIT License.
