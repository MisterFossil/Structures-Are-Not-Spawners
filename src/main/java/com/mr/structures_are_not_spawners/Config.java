package com.mr.structures_are_not_spawners;

import net.neoforged.fml.ModList;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<List<? extends String>> RULES = BUILDER
            .comment("Custom rules. Format: structure_id|entity_id,entity_id|required_kills|radius_blocks")
            .comment("Example: minecraft:pillager_outpost|minecraft:pillager|10|96")
            .comment("The radius is measured from the structure start chunk center; maximum radius is 512 blocks.")
            .defineListAllowEmpty("rules", List.of(
                    "minecraft:monument|minecraft:guardian,minecraft:elder_guardian|20|96",
                    "minecraft:pillager_outpost|minecraft:pillager|10|96"
            ), () -> "", Config::isString);

    public static final ModConfigSpec.BooleanValue ENABLE_MODDED_RULES = BUILDER
            .comment("Enable the optional built-in rules in moddedRules when their required mods are loaded.")
            .define("enableModdedRules", true);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> MODDED_RULES = BUILDER
            .comment("Optional integration rules. Format: structure_id|entity_id,entity_id|required_kills|radius_blocks|required_mod_id")
            .comment("An entry is active only while its required mod is loaded. Custom rules above take precedence.")
            .defineListAllowEmpty("moddedRules", List.of(
                    "repurposed_structures:outpost_badlands|minecraft:pillager|10|96|repurposed_structures",
                    "repurposed_structures:outpost_birch|minecraft:pillager|10|96|repurposed_structures",
                    "repurposed_structures:outpost_desert|minecraft:pillager|10|96|repurposed_structures",
                    "repurposed_structures:outpost_giant_tree_taiga|minecraft:pillager|10|96|repurposed_structures",
                    "repurposed_structures:outpost_icy|minecraft:pillager|10|96|repurposed_structures",
                    "repurposed_structures:outpost_jungle|minecraft:pillager|10|96|repurposed_structures",
                    "repurposed_structures:outpost_mangrove|minecraft:pillager|10|96|repurposed_structures",
                    "repurposed_structures:outpost_oak|minecraft:pillager|10|96|repurposed_structures",
                    "repurposed_structures:outpost_savanna|minecraft:pillager|10|96|repurposed_structures",
                    "repurposed_structures:outpost_snowy|minecraft:pillager|10|96|repurposed_structures",
                    "repurposed_structures:outpost_taiga|minecraft:pillager|10|96|repurposed_structures",
                    "repurposed_structures:outpost_basalt|minecraft:piglin|10|96|repurposed_structures",
                    "repurposed_structures:outpost_crimson|minecraft:piglin|10|96|repurposed_structures",
                    "repurposed_structures:outpost_nether_brick|minecraft:piglin|10|96|repurposed_structures",
                    "repurposed_structures:outpost_soul|minecraft:piglin|10|96|repurposed_structures",
                    "repurposed_structures:outpost_warped|minecraft:piglin|10|96|repurposed_structures",
                    "terralith:desert_outpost|minecraft:pillager|10|96|terralith",
                    "towns_and_towers:pillager_outpost_wooded_badlands|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:pillager_outpost_taiga|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:pillager_outpost_swamp|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:pillager_outpost_sunflower_plains|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:pillager_outpost_sparse_jungle|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:pillager_outpost_snowy_taiga|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:pillager_outpost_snowy_slopes|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:pillager_outpost_snowy_plains|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:pillager_outpost_snowy_beach|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:pillager_outpost_savanna|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:pillager_outpost_savanna_plateau|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:pillager_outpost_old_growth_taiga|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:pillager_outpost_ocean|minecraft:pillager,minecraft:vindicator|10|96|t_and_t",
                    "towns_and_towers:pillager_outpost_mushroom_fields|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:pillager_outpost_meadow|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:pillager_outpost_jungle|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:pillager_outpost_grove|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:pillager_outpost_forest|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:pillager_outpost_flower_forest|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:pillager_outpost_desert|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:pillager_outpost_birch_forest|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:pillager_outpost_beach|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:pillager_outpost_badlands|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:exclusives/pillager_outpost_classic|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:exclusives/pillager_outpost_iberian|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:exclusives/pillager_outpost_mediterranean|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:exclusives/pillager_outpost_nilotic|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:exclusives/pillager_outpost_oriental|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:exclusives/pillager_outpost_rustic|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:exclusives/pillager_outpost_swedish|minecraft:pillager|10|96|t_and_t",
                    "towns_and_towers:exclusives/pillager_outpost_tudor|minecraft:pillager|10|96|t_and_t"
            ), () -> "", Config::isString);

    public static final ModConfigSpec.BooleanValue DEBUG = BUILDER
            .comment("Enable SANS debug logging.")
            .define("debug", false);

    public static final ModConfigSpec.BooleanValue DEBUG_SPAWN_CHECKS = BUILDER
            .comment("Log structure spawn checks. Can be noisy on active servers.")
            .define("debugSpawnChecks", false);

    public static final ModConfigSpec.BooleanValue DEBUG_KILLS = BUILDER
            .comment("Log counted kills and structure progress.")
            .define("debugKills", false);

    static final ModConfigSpec SPEC = BUILDER.build();
    private static volatile Snapshot snapshot = Snapshot.empty();

    private Config() {}

    public static List<Rule> rules() {
        return snapshot.rules();
    }

    public static Rule ruleForStructure(ResourceLocation structureId) {
        return snapshot.byStructure().get(structureId);
    }

    public static List<Rule> rulesForEntity(ResourceLocation entityId) {
        return snapshot.byEntity().getOrDefault(entityId, List.of());
    }

    static List<String> ruleWarnings() {
        return snapshot.warnings();
    }

    static void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) reload();
    }

    static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) reload();
    }

    private static void reload() {
        List<Rule> rules = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<ResourceLocation> allStructures = new HashSet<>();
        Set<ResourceLocation> customStructures = new HashSet<>();

        parseRules(RULES.get(), false, allStructures, customStructures, rules, warnings);
        if (ENABLE_MODDED_RULES.get()) {
            parseRules(MODDED_RULES.get(), true, allStructures, customStructures, rules, warnings);
        }

        Map<ResourceLocation, Rule> byStructure = new HashMap<>();
        Map<ResourceLocation, List<Rule>> mutableByEntity = new HashMap<>();
        for (Rule rule : rules) {
            byStructure.put(rule.structureId(), rule);
            for (ResourceLocation entityId : rule.entityIds()) {
                mutableByEntity.computeIfAbsent(entityId, ignored -> new ArrayList<>()).add(rule);
            }
        }

        Map<ResourceLocation, List<Rule>> byEntity = new HashMap<>();
        mutableByEntity.forEach((entityId, entityRules) -> byEntity.put(entityId, List.copyOf(entityRules)));
        snapshot = new Snapshot(List.copyOf(rules), Map.copyOf(byStructure), Map.copyOf(byEntity),
                List.copyOf(warnings));
        SANSEvents.notifyRuleWarnings(snapshot.warnings());
        SANS.LOGGER.info("Loaded {} SANS structure rule(s).", rules.size());
    }

    private static void parseRules(List<? extends String> rawRules, boolean requireMod,
                                   Set<ResourceLocation> allStructures,
                                   Set<ResourceLocation> customStructures,
                                   List<Rule> output, List<String> warnings) {
        for (String raw : rawRules) {
            String[] parts = raw.split("\\|", -1);
            if ((requireMod && parts.length != 5) || (!requireMod && parts.length != 4 && parts.length != 5)) {
                String warning = "Ignoring malformed SANS rule '" + raw
                        + "': expected structure|entities|required_kills|radius_blocks"
                        + (requireMod ? "|required_mod_id" : " (with an optional |required_mod_id)");
                warnings.add(warning);
                SANS.LOGGER.warn("{}", warning);
                continue;
            }

            try {
                ResourceLocation structure = ResourceLocation.parse(parts[0].trim());
                Set<ResourceLocation> entities = new LinkedHashSet<>();
                for (String entity : parts[1].split(",", -1)) {
                    if (!entity.isBlank()) entities.add(ResourceLocation.parse(entity.trim()));
                }
                int requiredKills = Integer.parseInt(parts[2].trim());
                int radius = Integer.parseInt(parts[3].trim());
                String requiredMod = parts.length == 5 ? parts[4].trim() : "";

                if (entities.isEmpty() || requiredKills <= 0 || radius <= 0 || radius > 512) {
                    throw new IllegalArgumentException("entities and required kills must be positive; radius must be from 1 to 512");
                }
                if (requireMod && requiredMod.isEmpty()) {
                    throw new IllegalArgumentException("required mod id must not be empty");
                }
                if (!requiredMod.isEmpty() && !requiredMod.matches("[a-z0-9_.-]+")) {
                    throw new IllegalArgumentException("invalid required mod id '" + requiredMod + "'");
                }

                // A custom rule replaces its built-in integration rule for the same structure.
                if (requireMod && customStructures.contains(structure)) continue;
                if (!requiredMod.isEmpty() && !ModList.get().isLoaded(requiredMod)) continue;
                if (!allStructures.add(structure)) {
                    throw new IllegalArgumentException("only one rule per structure is allowed");
                }
                if (!requireMod) customStructures.add(structure);
                output.add(new Rule(structure, List.copyOf(entities), requiredKills, radius));
            } catch (RuntimeException ex) {
                String warning = "Ignoring invalid SANS rule '" + raw + "': " + ex.getMessage();
                warnings.add(warning);
                SANS.LOGGER.warn("{}", warning);
            }
        }
    }

    private static boolean isString(Object value) {
        return value instanceof String;
    }

    public record Rule(ResourceLocation structureId, List<ResourceLocation> entityIds,
                       int requiredKills, int radius) {}

    private record Snapshot(List<Rule> rules, Map<ResourceLocation, Rule> byStructure,
                            Map<ResourceLocation, List<Rule>> byEntity, List<String> warnings) {
        private static Snapshot empty() {
            return new Snapshot(List.of(), Map.of(), Map.of(), List.of());
        }
    }
}
