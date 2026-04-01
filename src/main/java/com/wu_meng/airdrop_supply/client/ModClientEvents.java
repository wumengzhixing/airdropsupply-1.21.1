package com.wu_meng.airdrop_supply.client;

import com.wu_meng.airdrop_supply.client.render.AirdropSupplyRenderer;
import com.wu_meng.airdrop_supply.entry.ModBlockEntities;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@SuppressWarnings("null")
public class ModClientEvents {

    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.AIRDROP_SUPPLY.get(), context -> new AirdropSupplyRenderer());
    }

}
