package com.wu_meng.airdrop_supply.client.render;

import com.wu_meng.airdrop_supply.entity.AirdropPlaneEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AirdropPlaneRenderer extends GeoEntityRenderer<AirdropPlaneEntity> {
    public AirdropPlaneRenderer(EntityRendererProvider.Context context) {
        super(context, new AirdropPlaneModel());
        this.shadowRadius = 0.0F;
    }
}
