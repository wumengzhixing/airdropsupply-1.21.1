package com.wu_meng.airdrop_supply.capability;

import com.mojang.datafixers.util.Pair;
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

public class AirdropPlayerData implements INBTSerializable<CompoundTag> {
    public int nextAirdropCountdown = Configuration.AIRDROP_SPAWN_INTERVAL.get();
    @Nullable
    public BlockPos fixDropLocation = null;
    public List<Pair<Long, BlockPos>> airdropDespawnInfo = new ArrayList<>();
    public AirdropPlayerData() {}

    @Override
    public CompoundTag serializeNBT(@Nonnull HolderLookup.Provider provider) {
        CompoundTag compoundNBT = new CompoundTag();
        compoundNBT.putInt("NextAirdropCountdown", nextAirdropCountdown);

        ListTag lt = new ListTag();
        airdropDespawnInfo.forEach(p -> {
            CompoundTag cp = new CompoundTag();
            cp.putLong("InvalidateTime", p.getFirst());
            BlockPos pos = Objects.requireNonNull(p.getSecond(), "AirdropPosition");
            cp.put("AirdropPosition", Objects.requireNonNull(NbtUtils.writeBlockPos(pos), "AirdropPosition"));
            lt.add(cp);
        });
        compoundNBT.put("AirdropDespawnInfo", lt);

        if (fixDropLocation != null) {
            compoundNBT.put("FixDropLocation", Objects.requireNonNull(NbtUtils.writeBlockPos(fixDropLocation), "FixDropLocation"));
        }
        return compoundNBT;
    }

    @Override
    public void deserializeNBT(@Nonnull HolderLookup.Provider provider, @Nonnull CompoundTag nbt) {
        nextAirdropCountdown = nbt.getInt("NextAirdropCountdown");

        airdropDespawnInfo.clear();
        if (nbt.contains("AirdropDespawnInfo")) {
            ListTag list = nbt.getList("AirdropDespawnInfo", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                long invalidateTime = entry.getLong("InvalidateTime");
                NbtUtils.readBlockPos(entry, "AirdropPosition").ifPresent(pos -> airdropDespawnInfo.add(Pair.of(invalidateTime, pos)));
            }
        }

        fixDropLocation = nbt.contains("FixDropLocation") ? NbtUtils.readBlockPos(nbt, "FixDropLocation").orElse(null) : null;
    }
}
