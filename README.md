# Structures Are Not Spawners (SANS)

SANS stops configured structures from continuing to spawn their configured mobs after enough of those mobs have died nearby.

## Supported versions

- Minecraft 1.21.1 + NeoForge 21.1.211 or newer

## Default rules

| Structure | Counted entities | Required deaths | Counting radius |
| --- | --- | ---: | ---: |
| Ocean Monument (`minecraft:monument`) | Guardian, Elder Guardian | 20 | 96 blocks |
| Pillager Outpost | Pillager | 10 | 96 blocks |

Rules are server-side and stored in `config/structures_are_not_spawners-common.toml`.
Optional integrations are listed separately under `moddedRules`; they activate only while their required mod is loaded. Set `enableModdedRules` to `false` to disable all built-in modded rules. A custom `rules` entry takes precedence over an optional rule for the same structure.

## Rule format

Each rule is one line in this format:

```text
structure_id|entity_id,entity_id|required_kills|radius_blocks
```

Example:

```text
minecraft:pillager_outpost|minecraft:pillager|10|96
minecraft:monument|minecraft:guardian,minecraft:elder_guardian|20|96
```

Optional modded rules use a fifth field for the required mod ID:

```text
repurposed_structures:outpost_badlands|minecraft:pillager|10|96|repurposed_structures
towns_and_towers:pillager_outpost_ocean|minecraft:pillager,minecraft:vindicator|10|96|t_and_t
```

The built-in optional rules cover Repurposed Structures outposts, Terralith's desert outpost, and Towns and Towers pillager outposts. Their defaults use the same 10 deaths and 96-block radius as vanilla Pillager Outposts. Repurposed Structures' Nether outposts count Piglins; Towns and Towers' ocean outpost also counts Vindicators.

Use one rule per structure ID. The radius is measured horizontally from the center of the structure's start chunk, and can be set from 1 to 512 blocks. Each death counts once regardless of its cause or who caused it. A configured mob can count outside the physical structure if it dies within the rule radius.

SANS only inspects chunks that are already loaded while associating a death with nearby structures. It never loads or generates chunks as a side effect of tracking kills.

## Persistence and performance

Kill progress is stored in per-dimension `SavedData`, keyed by structure ID and start chunk. Spawn checks only touch saved progress when vanilla has already selected that structure's spawn override. Once cleared, only the configured mob types are removed from that override; any other mobs in the same override remain eligible to spawn.

The mod does not edit blocks, structures, chunks, or entities. Removing it restores vanilla spawning. If it is installed again in the same world, saved progress is available again.

## Debug logging

The common config includes:

- `debug`: general SANS debug output.
- `debugKills`: log counted deaths and progress.
- `debugSpawnChecks`: log structure spawn filtering checks. This can be noisy.
