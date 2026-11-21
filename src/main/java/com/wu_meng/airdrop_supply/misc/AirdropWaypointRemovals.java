package com.wu_meng.airdrop_supply.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AirdropWaypointRemovals extends SavedData {
    private static final Factory<AirdropWaypointRemovals> FACTORY =
            new Factory<>(AirdropWaypointRemovals::new, AirdropWaypointRemovals::load);
    private final Map<UUID, Long> pending = new HashMap<>();

    public static AirdropWaypointRemovals get(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(FACTORY, "airdropsupply_waypoint_removals");
    }

    public static UUID legacyId(BlockPos position, long expiresAt) {
        // Older drops have no id; the saved position may have a different Y than the crate.
        String key = "airdropsupply:" + expiresAt + ":" + position.getX() + ":" + position.getZ();
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }

    public void record(UUID id, long expiresAt) {
        pending.put(id, expiresAt);
        setDirty();
    }

    boolean consume(UUID id) {
        if (pending.remove(id) == null) {
            return false;
        }
        setDirty();
        return true;
    }

    void discardExpired(long now) {
        // The player's tracked expiry removes the waypoint after this time.
        if (pending.values().removeIf(expiresAt -> now >= expiresAt)) {
            setDirty();
        }
    }

    private static AirdropWaypointRemovals load(CompoundTag tag, HolderLookup.Provider registries) {
        var removals = new AirdropWaypointRemovals();
        ListTag entries = tag.getList("Removals", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            if (entry.hasUUID("WaypointId")) {
                removals.pending.put(entry.getUUID("WaypointId"), entry.getLong("ExpiresAt"));
            }
        }
        return removals;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();
        pending.forEach((id, expiresAt) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("WaypointId", id);
            entry.putLong("ExpiresAt", expiresAt);
            entries.add(entry);
        });
        tag.put("Removals", entries);
        return tag;
    }
}
