package com.wu_meng.airdrop_supply.client.render;

import com.wu_meng.airdrop_supply.AirdropSupply;
import com.wu_meng.airdrop_supply.entity.AirdropDeliveryVisualEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AirdropParachuteModel extends GeoModel<AirdropDeliveryVisualEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
            AirdropSupply.CONTENT_ID, "geo/entity/airdrop_parachute.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AirdropSupply.CONTENT_ID, "textures/entity/airdrop_parachute.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(
            AirdropSupply.CONTENT_ID, "animations/entity/airdrop_parachute.animation.json");

    @Override
    public ResourceLocation getModelResource(AirdropDeliveryVisualEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(AirdropDeliveryVisualEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(AirdropDeliveryVisualEntity animatable) {
        return ANIMATION;
    }
}
