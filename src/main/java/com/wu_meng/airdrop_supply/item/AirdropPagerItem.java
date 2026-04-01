package com.wu_meng.airdrop_supply.item;

import com.wu_meng.airdrop_supply.AirdropSupply;
import com.wu_meng.airdrop_supply.block.AirdropSupplyBlock;
import com.wu_meng.airdrop_supply.entry.ModBlocks;
import com.wu_meng.airdrop_supply.entry.ModItems;
import com.wu_meng.airdrop_supply.misc.Configuration;
import com.wu_meng.airdrop_supply.network.s2c.MapPointPacketS2C;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;

@SuppressWarnings("null")
public class AirdropPagerItem extends Item {

    public AirdropPagerItem() {
        super(new Item.Properties());
    }

    @Override
    public @Nonnull InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand pUsedHand) {
        var itemStack = player.getItemInHand(pUsedHand);
        if(!itemStack.is(ModItems.AIRDROP_PAGER.get()))
            return InteractionResultHolder.pass(player.getItemInHand(pUsedHand));

        if(!level.isClientSide() && level instanceof ServerLevel serverLevel){
            if(level.dimension().equals(Level.OVERWORLD)){
                var dropPos = new BlockPos((int) player.getX(), level.getMaxBuildHeight(), (int) player.getZ());

                int ammoWeight = Math.max(0, Configuration.AMMO_AIRDROP_WEIGHT.get());
                int medicWeight = Math.max(0, Configuration.MEDIC_AIRDROP_WEIGHT.get());
                int totalWeight = ammoWeight + medicWeight;

                if (totalWeight <= 0) {
                    return InteractionResultHolder.fail(itemStack);
                }

                var result = level.getRandom().nextInt(totalWeight);

                // 修复：统一获取主世界的时间
                ServerLevel overworld = serverLevel.getServer().overworld();
                int day = (int) (overworld.getDayTime() / 24000) + 1;

                int nw = Math.max(0, Configuration.BASIC_BASE_WEIGHT.get() + day * Configuration.BASIC_MULTIPLE_WEIGHT.get());
                int mw = Math.max(0, Configuration.MEDIUM_BASE_WEIGHT.get() + day * Configuration.MEDIUM_MULTIPLE_WEIGHT.get());
                int aw = Math.max(0, Configuration.ADVANCED_BASE_WEIGHT.get() + day * Configuration.ADVANCED_MULTIPLE_WEIGHT.get());

                AirdropSupplyBlock.CaseLevel caseLevel = AirdropSupplyBlock.CaseLevel.BASIC;
                int totalLevelWeight = nw + mw + aw;

                if (totalLevelWeight > 0) {
                    int result2 = player.getRandom().nextInt(totalLevelWeight);
                    if (result2 < nw) {
                        caseLevel = AirdropSupplyBlock.CaseLevel.BASIC;
                    } else if (result2 < nw + mw) {
                        caseLevel = AirdropSupplyBlock.CaseLevel.MEDIUM;
                    } else {
                        caseLevel = AirdropSupplyBlock.CaseLevel.ADVANCED;
                    }
                }

                var isAmmo = result < ammoWeight;
                var fallingCrate = FallingBlockEntity.fall(level, dropPos, isAmmo ?
                        ModBlocks.AIRDROP_SUPPLY.get().defaultBlockState()
                                .setValue(AirdropSupplyBlock.TYPE, AirdropSupplyBlock.Type.NORMAL)
                                .setValue(HorizontalDirectionalBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(level.getRandom()))
                                .setValue(AirdropSupplyBlock.LEVEL, caseLevel) :
                        ModBlocks.AIRDROP_SUPPLY.get().defaultBlockState()
                                .setValue(AirdropSupplyBlock.TYPE, AirdropSupplyBlock.Type.MEDIC)
                                .setValue(HorizontalDirectionalBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(level.getRandom()))
                                .setValue(AirdropSupplyBlock.LEVEL, caseLevel));

                CompoundTag blockData = new CompoundTag();
                fallingCrate.blockData = blockData;
                blockData.putString("LootTable", AirdropSupply.LootTables.calculateLootTable(isAmmo ? AirdropSupplyBlock.Type.NORMAL : AirdropSupplyBlock.Type.MEDIC, caseLevel).location().toString());
                blockData.putLong("LootTableSeed", level.random.nextLong());

                blockData.putLong("DespawnTime", overworld.getGameTime() + Configuration.AIRDROP_DESPAWN_TIME.get());

                if(!player.isCreative()){
                    itemStack.shrink(1);
                }

                Component msg = Component.translatable("notification.airdrop_supply.airdrop_summoned", player.getScoreboardName());
                // 如果开启了全服广播配置，则进行全服提示
                if (Configuration.BROADCAST_COORDINATES.get()) {
                    serverLevel.getServer().getPlayerList().broadcastSystemMessage(msg, false);
                } else {
                    player.sendSystemMessage(msg);
                }

                if (player instanceof ServerPlayer serverPlayer) {
                    PacketDistributor.sendToPlayer(serverPlayer, new MapPointPacketS2C(dropPos));
                }
            } else {
                player.sendSystemMessage(
                        Component.translatable("notification.airdrop_supply.airdrop_summoned_invalid_dimension", player.getScoreboardName()));
            }
        }
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}