# Structures Are Not Spawners (SANS)

SANS prevents configured Minecraft structures from acting as effectively endless mob spawners after their associated enemies have been killed in sufficient numbers.

## Target

- Minecraft 1.21.1
- NeoForge 21.1.211+
- Java 21

## Default rules

| Structure | Counted entities | Kills | Radius |
| --- | --- | ---: | ---: |
| Ocean Monument | Guardian, Elder Guardian | 20 | 96 blocks |
| Pillager Outpost | Pillager | 10 | 96 blocks |

The rules are server-side and configurable in `config/structures_are_not_spawners-common.toml`.

## Rule format

```text
structure_id|entity_id,entity_id|required_kills|radius_blocks
```

For example:

```text
minecraft:pillager_outpost|minecraft:pillager|10|96
minecraft:ocean_monument|minecraft:guardian,minecraft:elder_guardian|20|96
```

A kill counts regardless of who or what caused the death. The victim may be outside the physical structure, provided it is within the configured radius of the corresponding structure.

## Persistence and performance

SANS stores only per-structure kill progress in `SavedData`. It does not alter generated blocks, structures, chunks, or entities. Cleared structures therefore remain vanilla structures; SANS only suppresses their structure-specific mob spawn override once the configured threshold is reached.

Removing SANS restores vanilla spawning behaviour. Its saved data is harmless if the mod is later reinstalled.

## Debugging

The common config provides:

- `debug` — general SANS debug logging.
- `debugKills` — log counted kills and progress.
- `debugSpawnChecks` — log structure spawn interception checks.

`debugSpawnChecks` can be noisy and should normally remain disabled.
