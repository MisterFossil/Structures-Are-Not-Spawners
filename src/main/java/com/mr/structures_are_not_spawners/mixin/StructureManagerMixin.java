package com.mr.structures_are_not_spawners.mixin;

import com.mr.structures_are_not_spawners.StructureManagerExtension;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(StructureManager.class)
public abstract class StructureManagerMixin implements StructureManagerExtension {
    @Shadow @Final private LevelAccessor level;

    @Override
    @Unique
    public LevelAccessor sans$getLevel() {
        return level;
    }
}
