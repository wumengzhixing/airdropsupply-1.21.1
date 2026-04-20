package com.wu_meng.airdrop_supply.client.render;

import com.wu_meng.airdrop_supply.blockentity.AirdropSupplyBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class AirdropSupplyRenderer extends GeoBlockRenderer<AirdropSupplyBlockEntity> {

    public AirdropSupplyRenderer() {
        super(new AirdropSupplyModel());
    }

    @Override
    public void preRender(PoseStack poseStack, AirdropSupplyBlockEntity animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int packedColor) {
        model.getBone("Head").ifPresent(bone -> bone.setTrackingMatrices(true));
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, packedColor);
    }

    @Override
    public void renderRecursively(PoseStack poseStack, AirdropSupplyBlockEntity animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int packedColor) {
        // Camera animation is temporarily disabled.
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, packedColor);
    }
}
