package com.wu_meng.airdrop_supply.client.render;

import com.wu_meng.airdrop_supply.AirdropSupply;
import com.wu_meng.airdrop_supply.entity.AirdropDeliveryVisualEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AirdropDeliveryModel extends GeoModel<AirdropDeliveryVisualEntity> {
    @Override
    public ResourceLocation getModelResource(AirdropDeliveryVisualEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(AirdropSupply.CONTENT_ID,
                "geo/%s_%s.geo.json".formatted(typeName(animatable), levelName(animatable)));
    }

    @Override
    public ResourceLocation getTextureResource(AirdropDeliveryVisualEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(AirdropSupply.CONTENT_ID,
                "textures/block/%s_%s.png".formatted(typeName(animatable), levelName(animatable)));
    }

    @Override
    public ResourceLocation getAnimationResource(AirdropDeliveryVisualEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(AirdropSupply.CONTENT_ID,
                "animations/%s_%s.animation.json".formatted(typeName(animatable), levelName(animatable)));
    }

    private static String typeName(AirdropDeliveryVisualEntity entity) {
        return entity.getCrateType().getSerializedName();
    }

    private static String levelName(AirdropDeliveryVisualEntity entity) {
        return entity.getCrateLevel().getSerializedName();
    }
}
