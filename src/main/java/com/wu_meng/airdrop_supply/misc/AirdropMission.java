package com.wu_meng.airdrop_supply.misc;

import com.mojang.datafixers.util.Pair;
import com.wu_meng.airdrop_supply.AirdropSupply;
import com.wu_meng.airdrop_supply.block.AirdropSupplyBlock;
import com.wu_meng.airdrop_supply.capability.AirdropPlayerData;
import com.wu_meng.airdrop_supply.entry.ModAttachments;
import com.wu_meng.airdrop_supply.entry.ModBlocks;
import com.wu_meng.airdrop_supply.network.s2c.MapPointPacketS2C;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.ref.WeakReference;

@SuppressWarnings("null")
public class AirdropMission {
    final WeakReference<Player> playerRef;
    boolean done = false;

    public AirdropMission(Player player) {
        this.playerRef = new WeakReference<>(player);
    }

    public void run() {
        if (done) return;
        Player ref = playerRef.get();

        if (!(ref instanceof ServerPlayer player) || player.hasDisconnected()) {
            done = true;
            return;
        }

        var server = player.getServer();
        if (server == null) {
            done = true;
            return;
        }

        ServerLevel overworld = server.overworld();
        AirdropPlayerData data = player.getData(ModAttachments.AIRDROP_PLAYER_DATA);

        BlockPos centerPos = calculateCenterPos(player, overworld, data);

        double ang = player.getRandom().nextDouble() * 2 * Math.PI;
        int dist = player.getRandom().nextInt(Configuration.AIRDROP_SPREAD_RANGE.get());
        int targetX = centerPos.getX() + (int) (Math.cos(ang) * dist);
        int targetZ = centerPos.getZ() + (int) (Math.sin(ang) * dist);

        BlockPos dropPos = findSafeLandingPos(overworld, targetX, targetZ);
        if (dropPos == null) {
            player.sendSystemMessage(Component.translatable("notification.airdrop_supply.airdrop_crash", player.getScoreboardName()));
            done = true;
            return;
        }

        processAirdropSpawn(overworld, dropPos, player, data);
        done = true;
    }

    private BlockPos calculateCenterPos(ServerPlayer player, ServerLevel overworld, AirdropPlayerData data) {
        if (data.fixDropLocation != null) return data.fixDropLocation;
        return switch (Configuration.AIRDROP_CENTER_MODE.get()) {
            case WORLD_SPAWN -> overworld.getSharedSpawnPos();
            case CUSTOM -> new BlockPos(Configuration.CUSTOM_CENTER_X.get(), overworld.getMaxBuildHeight() - 1, Configuration.CUSTOM_CENTER_Z.get());
            default -> {
                BlockPos respawnPos = player.getRespawnPosition();
                if (respawnPos == null || !player.getRespawnDimension().equals(ServerLevel.OVERWORLD)) {
                    yield overworld.getSharedSpawnPos();
                }
                yield respawnPos;
            }
        };
    }

    private BlockPos findSafeLandingPos(ServerLevel level, int x, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, level.getMaxBuildHeight() - 1, z);
        while (pos.getY() > level.getMinBuildHeight() + 1) {
            BlockState currentState = level.getBlockState(pos);
            BlockState belowState = level.getBlockState(pos.below());

            if ((currentState.isAir() || currentState.is(BlockTags.SNOW) || currentState.is(BlockTags.LEAVES) || currentState.canBeReplaced())
                    && (belowState.isSolid() && !belowState.is(BlockTags.LEAVES))) {
                if (level.noCollision(ModBlocks.AIRDROP_SUPPLY.get().defaultBlockState().getShape(level, pos).bounds().move(pos))) {
                    return pos.immutable();
                }
            }
            pos.move(Direction.DOWN);
        }
        return null;
    }

    private void processAirdropSpawn(ServerLevel overworld, BlockPos dropPos, ServerPlayer player, AirdropPlayerData data) {
        int ammoWeight = Math.max(0, Configuration.AMMO_AIRDROP_WEIGHT.get());
        int medicWeight = Math.max(0, Configuration.MEDIC_AIRDROP_WEIGHT.get());
        int noneWeight = Math.max(0, Configuration.NO_AIRDROP_WEIGHT.get());
        int totalWeight = ammoWeight + medicWeight + noneWeight;

        if (totalWeight <= 0) {
            player.sendSystemMessage(Component.translatable("notification.airdrop_supply.airdrop_crash", player.getScoreboardName()));
            return;
        }

        int result = player.getRandom().nextInt(totalWeight);
        if (result < noneWeight) {
            player.sendSystemMessage(Component.translatable("notification.airdrop_supply.airdrop_crash", player.getScoreboardName()));
            return;
        }

        AirdropSupplyBlock.Type type = (result < noneWeight + ammoWeight) ? AirdropSupplyBlock.Type.NORMAL : AirdropSupplyBlock.Type.MEDIC;
        int day = (int) (overworld.getDayTime() / 24000) + 1;
        AirdropSupplyBlock.CaseLevel caseLevel = calculateCaseLevel(player, day);

        spawnAirdropCrate(overworld, dropPos, player, data, type, caseLevel);
    }

    private AirdropSupplyBlock.CaseLevel calculateCaseLevel(Player player, int day) {
        int nw = Math.max(0, Configuration.BASIC_BASE_WEIGHT.get() + day * Configuration.BASIC_MULTIPLE_WEIGHT.get());
        int mw = Math.max(0, Configuration.MEDIUM_BASE_WEIGHT.get() + day * Configuration.MEDIUM_MULTIPLE_WEIGHT.get());
        int aw = Math.max(0, Configuration.ADVANCED_BASE_WEIGHT.get() + day * Configuration.ADVANCED_MULTIPLE_WEIGHT.get());

        int total = nw + mw + aw;
        if (total <= 0) return AirdropSupplyBlock.CaseLevel.BASIC;

        int roll = player.getRandom().nextInt(total);
        if (roll < nw) return AirdropSupplyBlock.CaseLevel.BASIC;
        if (roll < nw + mw) return AirdropSupplyBlock.CaseLevel.MEDIUM;
        return AirdropSupplyBlock.CaseLevel.ADVANCED;
    }

    private void spawnAirdropCrate(ServerLevel overworld, BlockPos dropPos, ServerPlayer player, AirdropPlayerData data, AirdropSupplyBlock.Type type, AirdropSupplyBlock.CaseLevel caseLevel) {
        // 生成坐标改在最高处，保留 FallingBlockEntity 掉落效果
        BlockPos spawnPos = new BlockPos(dropPos.getX(), overworld.getMaxBuildHeight() - 1, dropPos.getZ());

        var fallingCrate = FallingBlockEntity.fall(overworld, spawnPos, ModBlocks.AIRDROP_SUPPLY.get().defaultBlockState()
                .setValue(AirdropSupplyBlock.TYPE, type)
                .setValue(HorizontalDirectionalBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(player.getRandom()))
                .setValue(AirdropSupplyBlock.LEVEL, caseLevel));

        CompoundTag blockData = new CompoundTag();
        fallingCrate.blockData = blockData;
        blockData.putString("LootTable", AirdropSupply.LootTables.calculateLootTable(type, caseLevel).location().toString());
        blockData.putLong("LootTableSeed", player.getRandom().nextLong());
        // 将 DespawnTime 打包进掉落方块里
        blockData.putLong("DespawnTime", overworld.getGameTime() + Configuration.AIRDROP_DESPAWN_TIME.get());

        data.airdropDespawnInfo.add(Pair.of(overworld.getGameTime() + Configuration.AIRDROP_DESPAWN_TIME.get(), dropPos));

        Component msg = Component.translatable("notification.airdrop_supply.airdrop_arrive", dropPos.getX(), dropPos.getY(), dropPos.getZ(), player.getScoreboardName());
        if (Configuration.BROADCAST_COORDINATES.get()) {
            player.getServer().getPlayerList().broadcastSystemMessage(msg, false);
        } else {
            player.sendSystemMessage(msg);
        }

        PacketDistributor.sendToPlayer(player, new MapPointPacketS2C(dropPos));
    }

    public boolean done() {
        return done;
    }
}