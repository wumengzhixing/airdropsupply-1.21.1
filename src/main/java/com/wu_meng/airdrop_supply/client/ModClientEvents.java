package com.wu_meng.airdrop_supply.client;

import com.wu_meng.airdrop_supply.client.animation.AirdropPlayerAnimations;
import com.wu_meng.airdrop_supply.client.render.AirdropDeliveryRenderer;
import com.wu_meng.airdrop_supply.client.render.AirdropPlaneRenderer;
import com.wu_meng.airdrop_supply.client.render.AirdropSupplyRenderer;
import com.wu_meng.airdrop_supply.entry.ModBlockEntities;
import com.wu_meng.airdrop_supply.entry.ModEntities;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@SuppressWarnings("null")
public class ModClientEvents {

    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.AIRDROP_SUPPLY.get(), context -> new AirdropSupplyRenderer());
        event.registerEntityRenderer(ModEntities.AIRDROP_PLANE.get(), AirdropPlaneRenderer::new);
        event.registerEntityRenderer(ModEntities.AIRDROP_DELIVERY_VISUAL.get(), AirdropDeliveryRenderer::new);
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(AirdropPlayerAnimations::register);
    }

}
