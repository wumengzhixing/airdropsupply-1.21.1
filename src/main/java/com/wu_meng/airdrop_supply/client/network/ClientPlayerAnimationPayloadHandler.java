package com.wu_meng.airdrop_supply.client.network;

import com.wu_meng.airdrop_supply.client.animation.AirdropPlayerAnimations;
import com.wu_meng.airdrop_supply.network.s2c.PlayPlayerAnimationPacketS2C;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;

public final class ClientPlayerAnimationPayloadHandler {
    private ClientPlayerAnimationPayloadHandler() {}

    public static void handle(PlayPlayerAnimationPacketS2C payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        Entity entity = minecraft.level.getEntity(payload.playerId());
        if (entity instanceof AbstractClientPlayer player) {
            AirdropPlayerAnimations.playOpenCrate(player);
        }
    }
}
