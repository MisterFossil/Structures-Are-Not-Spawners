package com.mr.structures_are_not_spawners;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = SANS.MOD_ID)
public final class SANSEvents {
    private SANSEvents() {}

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        LivingEntity victim = event.getEntity();
        EntityType<?> type = victim.getType();
        if (Config.rules().isEmpty()) return;

        List<Match> matches = findMatchingStructures(level, victim.blockPosition(), type);
        if (matches.isEmpty()) return;

        StructureProgressData data = level.getDataStorage().computeIfAbsent(StructureProgressData.TYPE);
        for (Match match : matches) {
            Config.Rule rule = match.rule();
            String key = key(rule.structureId(), match.start());
            if (data.isCleared(key, rule)) continue;

            int before = data.getKills(key);
            data.addKill(key, rule.structureId().toString(), match.start().getChunkPos().toLong(), rule);
            int after = data.getKills(key);

            if (Config.DEBUG.get() || Config.DEBUG_KILLS.get()) {
                SANS.LOGGER.info("Counted {} at {} {}: {}/{} kills", type, rule.structureId(),
                        match.start().getChunkPos(), after, rule.requiredKills());
            }
            if (before < rule.requiredKills() && after >= rule.requiredKills()) {
                SANS.LOGGER.info("Cleared {} at {} after {} kills.", rule.structureId(),
                        match.start().getChunkPos(), after);
            }
        }
    }

    public static boolean shouldSuppressStructureSpawn(ServerLevel level, net.minecraft.core.BlockPos pos,
                                                        Structure structure, StructureSpawnOverride override,
                                                        boolean original) {
        if (!original || Config.rules().isEmpty()) return original;

        ResourceLocation structureId = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(structure);
        if (structureId == null) return original;

        for (Config.Rule rule : Config.rules()) {
            if (!rule.structureId().equals(structureId)) continue;

            boolean overrideContainsConfiguredEntity = override.spawns().unwrap().map(
                    list -> list.stream().anyMatch(entry -> rule.entityIds().contains(
                            level.registryAccess().registryOrThrow(Registries.ENTITY_TYPE).getKey(entry.value().type())
                    )),
                    tag -> false
            );
            if (!overrideContainsConfiguredEntity) return original;

            StructureStart start = findNearestStructure(level, pos, rule);
            if (start == StructureStart.INVALID_START) return original;

            StructureProgressData data = level.getDataStorage().computeIfAbsent(StructureProgressData.TYPE);
            String key = key(rule.structureId(), start);
            boolean cleared = data.isCleared(key, rule);

            if (Config.DEBUG.get() || Config.DEBUG_SPAWN_CHECKS.get()) {
                SANS.LOGGER.info("Spawn check {} at {} {}: cleared={}", structureId, start.getChunkPos(), pos, cleared);
            }
            return !cleared;
        }
        return original;
    }

    private static List<Match> findMatchingStructures(ServerLevel level, net.minecraft.core.BlockPos pos,
                                                       EntityType<?> entityType) {
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        List<Match> matches = new ArrayList<>();
        ResourceLocation entityId = registryKey(level, entityType);
        if (entityId == null) return matches;

        for (Config.Rule rule : Config.rules()) {
            if (!rule.entityIds().contains(entityId)) continue;
            StructureStart start = findNearestStructure(level, pos, rule);
            if (start == StructureStart.INVALID_START) continue;
            matches.add(new Match(rule, start));
        }
        return matches;
    }

    private static StructureStart findNearestStructure(ServerLevel level, net.minecraft.core.BlockPos pos,
                                                        Config.Rule rule) {
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, rule.structureId());
        Holder<Structure> holder = registry.getHolder(key).orElse(null);
        Structure structure = registry.get(rule.structureId());
        if (holder == null || structure == null) return StructureStart.INVALID_START;

        int radiusChunks = Math.max(1, (rule.radius() + 15) / 16);
        HolderSet<Structure> structures = HolderSet.direct(holder);
        Pair<net.minecraft.core.BlockPos, Holder<Structure>> located =
                level.findNearestMapStructure(structures, pos, radiusChunks, false);
        if (located == null) return StructureStart.INVALID_START;

        StructureStart start = level.structureManager().getStructureAt(located.getFirst(), structure);
        if (start == StructureStart.INVALID_START) return StructureStart.INVALID_START;

        double distanceSqr = AABB.of(start.getBoundingBox()).distanceToSqr(Vec3.atCenterOf(pos));
        return distanceSqr <= (double) rule.radius() * rule.radius() ? start : StructureStart.INVALID_START;
    }

    private static ResourceLocation registryKey(ServerLevel level, EntityType<?> entityType) {
        return level.registryAccess().registryOrThrow(Registries.ENTITY_TYPE).getKey(entityType);
    }

    private static String key(ResourceLocation structureId, StructureStart start) {
        return structureId + "@" + start.getChunkPos().toLong();
    }

    private record Match(Config.Rule rule, StructureStart start) {}
}
