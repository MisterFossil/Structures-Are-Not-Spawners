package com.mr.structures_are_not_spawners.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mr.structures_are_not_spawners.SANSEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkGenerator.class)
public final class ChunkGeneratorMixin implements ChunkGeneratorExtension {
    @Unique
    private ServerLevel sans$serverLevel;

    @ModifyExpressionValue(
            method = "getMobsAt",
            at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/mutable/MutableBoolean;isTrue()Z")
    )
    private boolean sans$preventClearedStructureSpawns(
            boolean original,
            @Local(argsOnly = true) BlockPos pos,
            @Local Structure structure,
            @Local StructureSpawnOverride override
    ) {
        if (sans$serverLevel == null) return original;
        return SANSEvents.shouldSuppressStructureSpawn(sans$serverLevel, pos, structure, override, original);
    }

    @Override
    public void sans$setServerLevel(ServerLevel level) {
        this.sans$serverLevel = level;
    }
}
