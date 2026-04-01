package com.wu_meng.airdrop_supply.client.render;

import com.wu_meng.airdrop_supply.AirdropSupply;
import com.wu_meng.airdrop_supply.block.AirdropSupplyBlock;
import com.wu_meng.airdrop_supply.blockentity.AirdropSupplyBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.model.GeoModel;

public class AirdropSupplyModel extends GeoModel<AirdropSupplyBlockEntity> {

    // 1. 动态获取模型 (.geo.json) - 修改为 CONTENT_ID
    @Override
    public ResourceLocation getModelResource(AirdropSupplyBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(AirdropSupply.CONTENT_ID, "geo/medium.geo.json");
    }

    // 2. 动态获取贴图 (.png) - 修改为 CONTENT_ID
    @Override
    public ResourceLocation getTextureResource(AirdropSupplyBlockEntity animatable) {
        String level = getLevelName(animatable);
        String type = getTypeName(animatable);
        return ResourceLocation.fromNamespaceAndPath(AirdropSupply.CONTENT_ID, "textures/block/" + level + "_" + type + ".png");
    }

    // 3. 动态获取动画 (.animation.json) - 修改为 CONTENT_ID
    @Override
    public ResourceLocation getAnimationResource(AirdropSupplyBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(AirdropSupply.CONTENT_ID, "animations/medium.animation.json");
    }

    // ================== 辅助解析方法 ==================

    private String getLevelName(AirdropSupplyBlockEntity entity) {
        BlockState state = entity.getBlockState();
        if (state.hasProperty(AirdropSupplyBlock.LEVEL)) {
            return state.getValue(AirdropSupplyBlock.LEVEL).getSerializedName();
        }
        return "basic";
    }

    private String getTypeName(AirdropSupplyBlockEntity entity) {
        BlockState state = entity.getBlockState();
        if (state.hasProperty(AirdropSupplyBlock.TYPE)) {
            return state.getValue(AirdropSupplyBlock.TYPE).getSerializedName();
        }
        return "normal";
    }
}
