package com.mr.structures_are_not_spawners.mixin;

import net.minecraft.server.level.ServerLevel;

public interface ChunkGeneratorExtension {
    void sans$setServerLevel(ServerLevel level);
}
