package com.mr.structures_are_not_spawners.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mr.structures_are_not_spawners.SANSEvents;
import com.mr.structures_are_not_spawners.StructureManagerExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {
    @ModifyExpressionValue(
            method = "getMobsAt",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/StructureSpawnOverride;spawns()Lnet/minecraft/util/random/WeightedRandomList;")
    )
    private WeightedRandomList<MobSpawnSettings.SpawnerData> sans$filterClearedSpawnList(
            WeightedRandomList<MobSpawnSettings.SpawnerData> original,
            @Local(argsOnly = true) StructureManager structureManager,
            @Local(argsOnly = true) BlockPos pos,
            @Local Structure structure
    ) {
        LevelAccessor levelAccessor = ((StructureManagerExtension) structureManager).sans$getLevel();
        if (!(levelAccessor instanceof ServerLevel level)) return original;
        return SANSEvents.filterClearedStructureSpawns(level, pos, structure, original);
    }
}
