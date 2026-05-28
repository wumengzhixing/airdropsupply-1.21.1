package com.wu_meng.airdrop_supply.client.render;

import com.wu_meng.airdrop_supply.AirdropSupply;
import com.wu_meng.airdrop_supply.block.AirdropSupplyBlock;
import com.wu_meng.airdrop_supply.blockentity.AirdropSupplyBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

@SuppressWarnings("null")
public class AirdropSupplyModel extends GeoModel<AirdropSupplyBlockEntity> {

    @Override
    public ResourceLocation getModelResource(AirdropSupplyBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(AirdropSupply.CONTENT_ID, "geo/%s_%s.geo.json".formatted(getTypeName(animatable), getLevelName(animatable)));
    }

    @Override
    public ResourceLocation getTextureResource(AirdropSupplyBlockEntity animatable) {
//        return ResourceLocation.fromNamespaceAndPath(AirdropSupply.CONTENT_ID, "textures/block/" + level + "_" + type + ".png");
        return ResourceLocation.fromNamespaceAndPath(AirdropSupply.CONTENT_ID, "textures/block/%s_%s.png".formatted(getTypeName(animatable), getLevelName(animatable)));
    }

    @Override
    public ResourceLocation getAnimationResource(AirdropSupplyBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(AirdropSupply.CONTENT_ID, "animations/%s_%s.animation.json".formatted(getTypeName(animatable), getLevelName(animatable)));
    }

    private String getLevelName(AirdropSupplyBlockEntity entity) {
        return entity.getBlockState().getValue(AirdropSupplyBlock.LEVEL).getSerializedName();
    }

    private String getTypeName(AirdropSupplyBlockEntity entity) {
        return entity.getBlockState().getValue(AirdropSupplyBlock.TYPE).getSerializedName();
    }
}
