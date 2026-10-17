package com.mr.structures_are_not_spawners;

import net.minecraft.world.level.LevelAccessor;

// Mixin bridge: the spawn hook needs the level currently owned by the structure manager
public interface StructureManagerExtension {
    LevelAccessor sans$getLevel();
}
