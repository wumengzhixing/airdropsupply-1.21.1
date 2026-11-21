package com.wu_meng.airdrop_supply.client.render;

import com.wu_meng.airdrop_supply.AirdropSupply;
import com.wu_meng.airdrop_supply.entity.AirdropPlaneEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AirdropPlaneModel extends GeoModel<AirdropPlaneEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
            AirdropSupply.CONTENT_ID, "geo/entity/airdrop_plane.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AirdropSupply.CONTENT_ID, "textures/entity/airdrop_plane.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(
            AirdropSupply.CONTENT_ID, "animations/entity/airdrop_plane.animation.json");

    @Override
    public ResourceLocation getModelResource(AirdropPlaneEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(AirdropPlaneEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(AirdropPlaneEntity animatable) {
        return ANIMATION;
    }
}
