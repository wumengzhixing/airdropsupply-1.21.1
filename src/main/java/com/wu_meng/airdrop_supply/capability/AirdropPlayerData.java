package com.wu_meng.airdrop_supply.capability;

import com.wu_meng.airdrop_supply.misc.AirdropWaypointRemovals;
import com.wu_meng.airdrop_supply.misc.Configuration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AirdropPlayerData implements INBTSerializable<CompoundTag> {
    public int nextAirdropCountdown = Configuration.AIRDROP_SPAWN_INTERVAL.get();
    @Nullable
    public BlockPos fixDropLocation = null;
    public final List<Waypoint> waypoints = new ArrayList<>();
    public AirdropPlayerData() {}

    @Override
    public CompoundTag serializeNBT(@Nonnull HolderLookup.Provider provider) {
        CompoundTag compoundNBT = new CompoundTag();
        compoundNBT.putInt("NextAirdropCountdown", nextAirdropCountdown);

        ListTag lt = new ListTag();
        for (Waypoint waypoint : waypoints) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("InvalidateTime", waypoint.expiresAt());
            entry.put("AirdropPosition", NbtUtils.writeBlockPos(waypoint.position()));
            entry.putUUID("WaypointId", waypoint.id());
            entry.putBoolean("AnnounceExpiry", waypoint.announceExpiry());
            lt.add(entry);
        }
        compoundNBT.put("AirdropDespawnInfo", lt);

        if (fixDropLocation != null) {
            compoundNBT.put("FixDropLocation", Objects.requireNonNull(NbtUtils.writeBlockPos(fixDropLocation), "FixDropLocation"));
        }
        return compoundNBT;
    }

    @Override
    public void deserializeNBT(@Nonnull HolderLookup.Provider provider, @Nonnull CompoundTag nbt) {
        nextAirdropCountdown = nbt.getInt("NextAirdropCountdown");

        waypoints.clear();
        if (nbt.contains("AirdropDespawnInfo")) {
            ListTag list = nbt.getList("AirdropDespawnInfo", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                long expiresAt = entry.getLong("InvalidateTime");
                boolean announceExpiry = !entry.contains("AnnounceExpiry") || entry.getBoolean("AnnounceExpiry");
                NbtUtils.readBlockPos(entry, "AirdropPosition").ifPresent(pos -> {
                    UUID id = entry.hasUUID("WaypointId")
                            ? entry.getUUID("WaypointId")
                            : AirdropWaypointRemovals.legacyId(pos, expiresAt);
                    waypoints.add(new Waypoint(id, expiresAt, pos, announceExpiry));
                });
            }
        }

        fixDropLocation = nbt.contains("FixDropLocation") ? NbtUtils.readBlockPos(nbt, "FixDropLocation").orElse(null) : null;
    }

    public record Waypoint(UUID id, long expiresAt, BlockPos position, boolean announceExpiry) {}
}
