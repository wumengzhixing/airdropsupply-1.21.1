package com.wu_meng.airdrop_supply.client;

import com.wu_meng.airdrop_supply.client.render.AirdropSupplyRenderer;
import com.wu_meng.airdrop_supply.entry.ModBlockEntities;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ModClientEvents {

    // 这是一个监听方法，它的参数 EntityRenderersEvent.RegisterRenderers 就是我们要找的事件！
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 在这里，把我们的空投方块实体 和 刚刚写的 GeckoLib 渲染器 绑定起来
        event.registerBlockEntityRenderer(ModBlockEntities.AIRDROP_SUPPLY.get(), context -> new AirdropSupplyRenderer());
    }

}