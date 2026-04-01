package com.wu_meng.airdrop_supply.client.render;

import com.wu_meng.airdrop_supply.blockentity.AirdropSupplyBlockEntity;
import com.wu_meng.airdrop_supply.client.camera.AirdropOpenCameraController;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
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
        if ("Head".equals(bone.getName()) && animatable != null) {
            var blockPos = animatable.getBlockPos();
            if (AirdropOpenCameraController.isActiveFor(blockPos)) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null) {
                    Matrix4f pose = poseStack.last().pose();
                    Vector4f local = new Vector4f(0, 0, 0, 1);
                    pose.transform(local);

                    Vec3 renderSpacePos = new Vec3(local.x, local.y, local.z);
                    Vec3 worldPos = mc.gameRenderer.getMainCamera().getPosition().add(renderSpacePos);

                    Matrix3f normal = poseStack.last().normal();
                    Vector3f forward = new Vector3f(0, 0, -1);
                    normal.transform(forward);

                    double dx = forward.x;
                    double dy = forward.y;
                    double dz = forward.z;
                    double horiz = Math.sqrt(dx * dx + dz * dz);
                    if (horiz > 1.0E-6) {
                        float yaw = (float) (Mth.atan2(dz, dx) * (180F / (float) Math.PI)) - 90F;
                        float pitch = (float) (-(Mth.atan2(dy, horiz) * (180F / (float) Math.PI)));
                        AirdropOpenCameraController.updateFromBone(blockPos, worldPos, yaw, pitch);
                    }
                }
            }
        }
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, packedColor);
    }
}
