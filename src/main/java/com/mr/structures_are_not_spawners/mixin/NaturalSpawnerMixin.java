package com.mr.structures_are_not_spawners.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NaturalSpawner.class)
public final class NaturalSpawnerMixin {
    @Inject(method = "mobsAt", at = @At("HEAD"))
    private static void sans$passServerLevel(
            CallbackInfoReturnable<WeightedRandomList<MobSpawnSettings.SpawnerData>> cir,
            @Local(argsOnly = true) ServerLevel level,
            @Local(argsOnly = true) ChunkGenerator generator
    ) {
        ((ChunkGeneratorExtension) generator).sans$setServerLevel(level);
    }
}
