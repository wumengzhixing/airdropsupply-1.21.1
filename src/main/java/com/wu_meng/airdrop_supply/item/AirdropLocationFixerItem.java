package com.wu_meng.airdrop_supply.item;

import com.wu_meng.airdrop_supply.entry.ModAttachments;
import com.wu_meng.airdrop_supply.entry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

@SuppressWarnings("null")
public class AirdropLocationFixerItem extends Item {

    private final boolean cancel;

    public AirdropLocationFixerItem(boolean cancel) {
        super(new Properties());
        this.cancel = cancel;
    }

    @Override
    public @Nonnull InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand pUsedHand) {
        var itemStack = player.getItemInHand(pUsedHand);
        if((!cancel && !itemStack.is(ModItems.AIRDROP_LOCATION_FIXER.get())) ||
                (cancel && !itemStack.is(ModItems.AIRDROP_LOCATION_CANCELLER.get())))
            return InteractionResultHolder.pass(player.getItemInHand(pUsedHand));
        if(!level.isClientSide()){
            if(level.dimension().equals(Level.OVERWORLD)){
                var data = player.getData(ModAttachments.AIRDROP_PLAYER_DATA);
                data.fixDropLocation = cancel ? null : new BlockPos(player.getOnPos().getX(), level.getMaxBuildHeight() - 1, player.getOnPos().getZ());
                if(!player.isCreative()){
                    itemStack.shrink(1);
                }
                player.sendSystemMessage(
                        cancel?
                                Component.translatable("notification.airdrop_supply.airdrop_location_unset",player.getScoreboardName()) :
                                Component.translatable("notification.airdrop_supply.airdrop_location_set",player.getOnPos().getX(),player.getOnPos().getZ(),player.getScoreboardName()));
            } else {
                player.sendSystemMessage(
                        cancel?
                                Component.translatable("notification.airdrop_supply.airdrop_location_unset_invalid_dimension"):
                                Component.translatable("notification.airdrop_supply.airdrop_location_set_invalid_dimension",player.getScoreboardName()));
            }
        }
        return InteractionResultHolder.sidedSuccess(itemStack,level.isClientSide());
    }
}
