package com.mr.structures_are_not_spawners;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;

public final class StructureProgressData extends SavedData {
    private final Map<String, Progress> structures;

    private static final Codec<StructureProgressData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Progress.CODEC).fieldOf("structures")
                    .forGetter(data -> data.structures)
    ).apply(instance, StructureProgressData::new));

    public static final SavedDataType<StructureProgressData> TYPE = new SavedDataType<>(
            SANS.MOD_ID + "/structure_progress",
            StructureProgressData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    public StructureProgressData() {
        this.structures = new HashMap<>();
    }

    private StructureProgressData(Map<String, Progress> structures) {
        this.structures = new HashMap<>(structures);
    }

    public int getKills(String key) {
        Progress progress = structures.get(key);
        return progress == null ? 0 : progress.kills();
    }

    public boolean isCleared(String key, Config.Rule rule) {
        return getKills(key) >= rule.requiredKills();
    }

    public void addKill(String key, String structureId, long chunkPos, Config.Rule rule) {
        Progress old = structures.get(key);
        if (old == null) {
            structures.put(key, new Progress(structureId, chunkPos, 1));
        } else if (old.kills() < rule.requiredKills()) {
            structures.put(key, new Progress(old.structureId(), old.chunkPos(), old.kills() + 1));
        }
        setDirty();
    }

    public record Progress(String structureId, long chunkPos, int kills) {
        private static final Codec<Progress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("structure").forGetter(Progress::structureId),
                Codec.LONG.fieldOf("chunk").forGetter(Progress::chunkPos),
                Codec.INT.fieldOf("kills").forGetter(Progress::kills)
        ).apply(instance, Progress::new));
    }
}
