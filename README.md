# 亡命奔逃
可正常使用，使用方法請自行摸索。 本專案採用 `GNU Affero General Public License v3.0`。

## Usage
### Commands
#### `/start <player selector> <map name>`
#### `/join <lobby UUID> <player selector>`
### Configuration

```yml
---
maps:
  - name: testing
    folder_name: "ExampleMap1"
    survivor_spawn:
      x: -1
      y: 1
      z: -1
    killer_spawn:
      spawn:
        x: -0.5
        y: 2
        z: 6
      door:
        start_pos:
          x: -1
          y: 1
          z: 2
        end_pos:
          x: -2
          y: 3
          z: 2
    stages:
      - name: ohio
        time: 15
        activate:
          pos:
            x: -4
            y: 2
            z: -16
          type: "BUTTON" # BUTTON / TOUCH
        door:
          start_pos:
            x: -3
            y: 2
            z: -17
          end_pos:
            x: 0
            y: 1
            z: -17

```



