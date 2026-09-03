# Psycho1v1Rtp

Plugin simples de fila 1v1 com RTP para servidores Minecraft.

O jogador usa `/1v1` para entrar na fila. Quando entram dois jogadores, o plugin procura um local seguro e teleporta os dois para começar a luta.

Funciona com Paper, Purpur e Folia.

## Features

- fila automática de 1v1
- `/1v1` para entrar ou sair da fila
- `/rtpqueue` como alias
- RTP seguro
- mundo fixo ou aleatório
- raio do teleporte configurável
- opção de exigir armadura completa
- distância entre os jogadores configurável
- opção de deixar os jogadores olhando um para o outro
- sons configuráveis
- inglês e português do Brasil
- mensagens personalizáveis com MiniMessage
- `/ps1v1 reload`

## Commands

| Command | Description | Permission |
| --- | --- | --- |
| `/1v1` | Join or leave the queue | `psrtpqueue.use` |
| `/rtpqueue` | Alias for `/1v1` | `psrtpqueue.use` |
| `/ps1v1 reload` | Reload config and language files | `ps1v1.admin` |

## Installation

1. Coloque o JAR na pasta `plugins`.
2. Inicie o servidor.
3. Edite o `config.yml` se quiser mudar alguma coisa.

Java 21 é necessário.

## Config

Config padrão:

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

### Mundos

Para usar sempre o mesmo mundo:

```yaml
world-selection:
  random-world: false
  default-world: "world"
```

Para sortear entre vários mundos:

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

`distance` define a distância entre os dois jogadores. Com `enabled: true`, eles também são posicionados olhando um para o outro.

## Languages

O plugin vem com:

- `en`
- `pt_BR`

Para usar português:

```yaml
language: "pt_BR"
```

Também aceita `pt`, `ptbr` e `pt-br`.

Os arquivos ficam em:

```text
plugins/1v1/languages/
```

Todas as mensagens podem ser editadas com MiniMessage.

Exemplo:

```yaml
messages:
  searching: "<gray>Searching for an opponent...</gray>"
  found: "<green>1v1 found! Teleporting...</green>"
```

Depois de mudar o config ou as mensagens, use `/ps1v1 reload`.

## Permissions

| Permission | Default |
| --- | --- |
| `psrtpqueue.use` | Everyone |
| `ps1v1.admin` | OP |

## Support

Discord: https://discord.gg/6smGe5Ujvc

## License

MIT License.
