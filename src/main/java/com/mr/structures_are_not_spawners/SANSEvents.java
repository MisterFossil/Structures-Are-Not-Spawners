package com.mr.structures_are_not_spawners;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = SANS.MOD_ID)
public final class SANSEvents {
    private SANSEvents() {}

    static void notifyRuleWarnings(List<String> warnings) {
        if (warnings.isEmpty()) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        Component message = ruleWarningMessage(warnings);
        server.execute(() -> server.getPlayerList().getPlayers()
                .forEach(player -> player.sendSystemMessage(message)));
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        List<String> warnings = Config.ruleWarnings();
        if (warnings.isEmpty() || !(event.getEntity() instanceof ServerPlayer player)) return;

        player.sendSystemMessage(ruleWarningMessage(warnings));
    }

    private static Component ruleWarningMessage(List<String> warnings) {
        String count = warnings.size() == 1 ? "1 invalid rule was" : warnings.size() + " invalid rules were";
        String firstWarning = warnings.getFirst();
        if (firstWarning.length() > 180) firstWarning = firstWarning.substring(0, 177) + "...";
        return Component.literal("SANS: " + count + " ignored. " + firstWarning
                + " Check the SANS rules in config/structures_are_not_spawners-common.toml.");
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;

        LivingEntity victim = event.getEntity();
        List<Match> matches = findMatchingStructures(level, victim.blockPosition(), victim.getType());
        if (matches.isEmpty()) return;

        StructureProgressData data = level.getDataStorage().computeIfAbsent(
                StructureProgressData.FACTORY, StructureProgressData.STORAGE_ID);
        for (Match match : matches) {
            Config.Rule rule = match.rule();
            long chunkPos = match.start().getChunkPos().toLong();
            String key = key(rule.structureId(), chunkPos);
            if (data.isCleared(key, rule)) continue;

            int before = data.getKills(key);
            data.addKill(key, rule.structureId().toString(), chunkPos, rule);
            int after = data.getKills(key);

            if (Config.DEBUG.get() || Config.DEBUG_KILLS.get()) {
                SANS.LOGGER.info("Counted {} near {} {}: {}/{} kills", victim.getType(), rule.structureId(),
                        match.start().getChunkPos(), after, rule.requiredKills());
            }
            if (before < rule.requiredKills() && after >= rule.requiredKills()) {
                SANS.LOGGER.info("Cleared {} at {} after {} kills.", rule.structureId(),
                        match.start().getChunkPos(), after);
            }
        }
    }

    /**
     * Called only for a structure override that vanilla has already selected at this position.
     * Keeping non-configured entries preserves unrelated mobs from the same override.
     */
    public static WeightedRandomList<MobSpawnSettings.SpawnerData> filterClearedStructureSpawns(
            ServerLevel level, BlockPos pos, Structure structure,
            WeightedRandomList<MobSpawnSettings.SpawnerData> original) {
        if (original.isEmpty() || Config.rules().isEmpty()) return original;

        Registry<Structure> structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        ResourceLocation structureId = structureRegistry.getKey(structure);
        if (structureId == null) return original;

        Config.Rule rule = Config.ruleForStructure(structureId);
        if (rule == null) return original;

        StructureProgressData data = level.getDataStorage().get(
                StructureProgressData.FACTORY, StructureProgressData.STORAGE_ID);
        if (data == null || !data.couldHaveCleared(rule)) return original;

        Registry<EntityType<?>> entityRegistry = level.registryAccess().registryOrThrow(Registries.ENTITY_TYPE);
        boolean containsConfiguredEntity = false;
        for (MobSpawnSettings.SpawnerData entry : original.unwrap()) {
            if (rule.entityIds().contains(entityRegistry.getKey(entry.type))) {
                containsConfiguredEntity = true;
                break;
            }
        }
        if (!containsConfiguredEntity) return original;

        StructureStart start = level.structureManager().getStructureAt(pos, structure);
        if (start == null || start == StructureStart.INVALID_START || !start.isValid()) return original;

        long chunkPos = start.getChunkPos().toLong();
        String key = key(rule.structureId(), chunkPos);
        boolean cleared = data.isCleared(key, rule);

        if (Config.DEBUG.get() || Config.DEBUG_SPAWN_CHECKS.get()) {
            SANS.LOGGER.info("Spawn check {} at {} {}: cleared={}", structureId,
                    start.getChunkPos(), pos, cleared);
        }
        if (!cleared) return original;

        List<MobSpawnSettings.SpawnerData> remaining = new ArrayList<>();
        for (MobSpawnSettings.SpawnerData entry : original.unwrap()) {
            if (!rule.entityIds().contains(entityRegistry.getKey(entry.type))) {
                remaining.add(entry);
            }
        }
        return WeightedRandomList.create(remaining);
    }

    private static List<Match> findMatchingStructures(ServerLevel level, BlockPos pos, EntityType<?> entityType) {
        List<Match> matches = new ArrayList<>();
        Registry<EntityType<?>> entityRegistry = level.registryAccess().registryOrThrow(Registries.ENTITY_TYPE);
        ResourceLocation entityId = entityRegistry.getKey(entityType);
        if (entityId == null) return matches;

        List<Config.Rule> matchingRules = Config.rulesForEntity(entityId);
        if (matchingRules.isEmpty()) return matches;

        Registry<Structure> structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (Config.Rule rule : matchingRules) {
            Structure structure = structureRegistry.get(rule.structureId());
            if (structure == null) continue;

            // Inspect loaded chunks only. Kill tracking must never load or generate chunks.
            int radius = rule.radius();
            int minChunkX = Math.floorDiv(pos.getX() - radius - 8, 16);
            int maxChunkX = Math.floorDiv(pos.getX() + radius - 8, 16);
            int minChunkZ = Math.floorDiv(pos.getZ() - radius - 8, 16);
            int maxChunkZ = Math.floorDiv(pos.getZ() + radius - 8, 16);
            Set<Long> seenStarts = new HashSet<>();

            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                    if (chunk == null) continue;

                    StructureStart start = chunk.getStartForStructure(structure);
                    if (start == null || start == StructureStart.INVALID_START || !start.isValid()) continue;

                    ChunkPos startChunk = start.getChunkPos();
                    long startKey = startChunk.toLong();
                    if (!seenStarts.add(startKey)) continue;

                    double dx = startChunk.getMiddleBlockX() - (double) pos.getX();
                    double dz = startChunk.getMiddleBlockZ() - (double) pos.getZ();
                    if (dx * dx + dz * dz <= (double) radius * radius) {
                        matches.add(new Match(rule, start));
                    }
                }
            }
        }
        return matches;
    }

    private static String key(ResourceLocation structureId, long chunkPos) {
        return structureId + "@" + chunkPos;
    }

    private record Match(Config.Rule rule, StructureStart start) {}
}
