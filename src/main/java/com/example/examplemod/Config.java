package com.mr.structures_are_not_spawners;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<List<? extends String>> RULES = BUILDER
            .comment("Structure rules. Format: structure_id|entity_id,entity_id|required_kills|radius_blocks")
            .comment("Example: minecraft:pillager_outpost|minecraft:pillager|10|96")
            .defineListAllowEmpty("rules", List.of(
                    "minecraft:ocean_monument|minecraft:guardian,minecraft:elder_guardian|20|96",
                    "minecraft:pillager_outpost|minecraft:pillager|10|96"
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

    private static volatile List<Rule> parsedRules = List.of();

    private Config() {}

    public static List<Rule> rules() {
        return parsedRules;
    }

    @SubscribeEvent
    static void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) reload();
    }

    @SubscribeEvent
    static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) reload();
    }

    private static void reload() {
        List<Rule> result = new ArrayList<>();
        for (String raw : RULES.get()) {
            String[] parts = raw.split("\\|", -1);
            if (parts.length != 4) {
                SANS.LOGGER.warn("Ignoring malformed SANS rule: {}", raw);
                continue;
            }

            try {
                ResourceLocation structure = ResourceLocation.parse(parts[0].trim());
                List<ResourceLocation> entities = new ArrayList<>();
                for (String entity : parts[1].split(",")) {
                    if (!entity.isBlank()) entities.add(ResourceLocation.parse(entity.trim()));
                }
                int requiredKills = Integer.parseInt(parts[2].trim());
                int radius = Integer.parseInt(parts[3].trim());

                if (entities.isEmpty() || requiredKills <= 0 || radius <= 0) {
                    throw new IllegalArgumentException("entities, required kills and radius must be positive");
                }
                result.add(new Rule(structure, List.copyOf(entities), requiredKills, radius));
            } catch (RuntimeException ex) {
                SANS.LOGGER.warn("Ignoring invalid SANS rule '{}': {}", raw, ex.getMessage());
            }
        }
        parsedRules = List.copyOf(result);
        SANS.LOGGER.info("Loaded {} SANS structure rule(s).", parsedRules.size());
    }

    private static boolean isString(Object value) {
        return value instanceof String;
    }

    public record Rule(ResourceLocation structureId, List<ResourceLocation> entityIds, int requiredKills, int radius) {}
}
