package com.mr.structures_are_not_spawners;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public final class StructureProgressData extends SavedData {
    // Saved per world, keyed by structure type and chunk where structure starts
    public static final String STORAGE_ID = SANS.MOD_ID + "_structure_progress";

    private final Map<String, Progress> structures;
    // Skip structure lookups until a rule can be completed
    private final Map<String, Integer> maxKillsByStructure;

    public static final Factory<StructureProgressData> FACTORY = new Factory<>(
            StructureProgressData::new,
            StructureProgressData::load,
            DataFixTypes.LEVEL
    );

    public StructureProgressData() {
        this.structures = new HashMap<>();
        this.maxKillsByStructure = new HashMap<>();
    }

    private static StructureProgressData load(CompoundTag tag, HolderLookup.Provider registries) {
        StructureProgressData data = new StructureProgressData();
        ListTag entries = tag.getList("structures", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            if (!entry.contains("key", Tag.TAG_STRING)
                    || !entry.contains("structure", Tag.TAG_STRING)
                    || !entry.contains("chunk", Tag.TAG_LONG)
                    || !entry.contains("kills", Tag.TAG_INT)) {
                continue;
            }

            int kills = entry.getInt("kills");
            if (kills > 0) {
                String structureId = entry.getString("structure");
                data.structures.put(entry.getString("key"), new Progress(
                        structureId, entry.getLong("chunk"), kills));
                data.maxKillsByStructure.merge(structureId, kills, Math::max);
            }
        }
        return data;
    }

    public int getKills(String key) {
        Progress progress = structures.get(key);
        return progress == null ? 0 : progress.kills();
    }

    public boolean isCleared(String key, Config.Rule rule) {
        return getKills(key) >= rule.requiredKills();
    }


    public boolean couldHaveCleared(Config.Rule rule) {
        return maxKillsByStructure.getOrDefault(rule.structureId().toString(), 0) >= rule.requiredKills();
    }

    public void addKill(String key, String structureId, long chunkPos, Config.Rule rule) {
        Progress old = structures.get(key);
        if (old == null) {
            structures.put(key, new Progress(structureId, chunkPos, 1));
        } else if (old.kills() < rule.requiredKills()) {
            structures.put(key, new Progress(old.structureId(), old.chunkPos(), old.kills() + 1));
        }
        Progress updated = structures.get(key);
        if (updated != null) {
            maxKillsByStructure.merge(updated.structureId(), updated.kills(), Math::max);
        }
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();
        for (Map.Entry<String, Progress> structure : structures.entrySet()) {
            Progress progress = structure.getValue();
            CompoundTag entry = new CompoundTag();
            entry.putString("key", structure.getKey());
            entry.putString("structure", progress.structureId());
            entry.putLong("chunk", progress.chunkPos());
            entry.putInt("kills", progress.kills());
            entries.add(entry);
        }
        tag.put("structures", entries);
        return tag;
    }

    public record Progress(String structureId, long chunkPos, int kills) {}
}
