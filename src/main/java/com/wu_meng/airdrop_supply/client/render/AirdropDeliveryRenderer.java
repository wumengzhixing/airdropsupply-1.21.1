package com.wu_meng.airdrop_supply.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wu_meng.airdrop_supply.block.AirdropSupplyBlock;
import com.wu_meng.airdrop_supply.entity.AirdropDeliveryVisualEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class AirdropDeliveryRenderer extends GeoEntityRenderer<AirdropDeliveryVisualEntity> {
    public AirdropDeliveryRenderer(EntityRendererProvider.Context context) {
        super(context, new AirdropDeliveryModel());
        this.shadowRadius = 0.5F;
        this.addRenderLayer(new ParachuteLayer(this));
    }

    private static final class ParachuteLayer extends GeoRenderLayer<AirdropDeliveryVisualEntity> {
        private static final double PARACHUTE_OFFSET_Y = 2.5;
        private static final float PARACHUTE_HALF_SIZE = 0.5F;
        private static final float ROPE_WIDTH = 0.025F;
        private static final int ROPE_SEGMENTS = 24;
        private static final int[] CORNER_SIGNS = {-1, 1};
        private final AirdropParachuteModel parachuteModel = new AirdropParachuteModel();

        private ParachuteLayer(AirdropDeliveryRenderer renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, AirdropDeliveryVisualEntity animatable,
                           BakedGeoModel bakedModel, @Nullable RenderType renderType,
                           MultiBufferSource bufferSource, @Nullable VertexConsumer buffer,
                           float partialTick, int packedLight, int packedOverlay) {
            BakedGeoModel parachute = this.parachuteModel.getBakedModel(
                    this.parachuteModel.getModelResource(animatable));
            RenderType parachuteRenderType = RenderType.entityCutoutNoCull(
                    this.parachuteModel.getTextureResource(animatable));

            renderRopes(poseStack, bufferSource, animatable, packedLight);

            poseStack.pushPose();
            poseStack.translate(0.0, PARACHUTE_OFFSET_Y, 0.0);
            this.renderer.reRender(
                    parachute,
                    poseStack,
                    bufferSource,
                    animatable,
                    parachuteRenderType,
                    bufferSource.getBuffer(parachuteRenderType),
                    partialTick,
                    packedLight,
                    packedOverlay,
                    0xFFFFFFFF
            );
            poseStack.popPose();
        }

        private static void renderRopes(PoseStack poseStack, MultiBufferSource bufferSource,
                                        AirdropDeliveryVisualEntity animatable, int packedLight) {
            CrateAttachment attachment = crateAttachment(animatable);
            VertexConsumer ropeBuffer = bufferSource.getBuffer(RenderType.leash());

            for (int xSign : CORNER_SIGNS) {
                for (int zSign : CORNER_SIGNS) {
                    renderRope(
                            poseStack,
                            ropeBuffer,
                            xSign * PARACHUTE_HALF_SIZE,
                            (float) PARACHUTE_OFFSET_Y,
                            zSign * PARACHUTE_HALF_SIZE,
                            xSign * attachment.halfWidth(),
                            attachment.topY(),
                            zSign * attachment.halfDepth(),
                            packedLight
                    );
                }
            }
        }

        private static void renderRope(PoseStack poseStack, VertexConsumer buffer,
                                       float startX, float startY, float startZ,
                                       float endX, float endY, float endZ, int packedLight) {
            poseStack.pushPose();
            poseStack.translate(startX, startY, startZ);

            float deltaX = endX - startX;
            float deltaY = endY - startY;
            float deltaZ = endZ - startZ;
            float horizontalLengthSquared = deltaX * deltaX + deltaZ * deltaZ;
            float sideX;
            float sideZ;

            if (horizontalLengthSquared < 1.0E-6F) {
                sideX = ROPE_WIDTH / 2.0F;
                sideZ = 0.0F;
            } else {
                float sideScale = Mth.invSqrt(horizontalLengthSquared) * ROPE_WIDTH / 2.0F;
                sideX = deltaZ * sideScale;
                sideZ = deltaX * sideScale;
            }

            Matrix4f pose = poseStack.last().pose();
            for (int segment = 0; segment <= ROPE_SEGMENTS; segment++) {
                addRopeVertexPair(buffer, pose, deltaX, deltaY, deltaZ,
                        ROPE_WIDTH, ROPE_WIDTH, sideX, sideZ, segment, false, packedLight);
            }
            for (int segment = ROPE_SEGMENTS; segment >= 0; segment--) {
                addRopeVertexPair(buffer, pose, deltaX, deltaY, deltaZ,
                        ROPE_WIDTH, 0.0F, sideX, sideZ, segment, true, packedLight);
            }

            poseStack.popPose();
        }

        private static void addRopeVertexPair(VertexConsumer buffer, Matrix4f pose,
                                              float deltaX, float deltaY, float deltaZ,
                                              float yOffset, float yInset, float sideX, float sideZ,
                                              int segment, boolean reverse, int packedLight) {
            float progress = (float) segment / ROPE_SEGMENTS;
            float shade = segment % 2 == (reverse ? 1 : 0) ? 0.72F : 1.0F;
            float red = 0.36F * shade;
            float green = 0.24F * shade;
            float blue = 0.12F * shade;
            float x = deltaX * progress;
            float y = deltaY * progress;
            float z = deltaZ * progress;

            buffer.addVertex(pose, x - sideX, y + yInset, z + sideZ)
                    .setColor(red, green, blue, 1.0F)
                    .setLight(packedLight);
            buffer.addVertex(pose, x + sideX, y + yOffset - yInset, z - sideZ)
                    .setColor(red, green, blue, 1.0F)
                    .setLight(packedLight);
        }

        private static CrateAttachment crateAttachment(AirdropDeliveryVisualEntity entity) {
            if (entity.getCrateType() == AirdropSupplyBlock.Type.AMMO) {
                return switch (entity.getCrateLevel()) {
                    case BASIC -> new CrateAttachment(0.738F, 0.547F, 0.360F);
                    case MEDIUM -> new CrateAttachment(1.0F, 0.813F, 0.5F);
                    case ADVANCED -> new CrateAttachment(1.25F, 1.016F, 0.658F);
                };
            }

            return switch (entity.getCrateLevel()) {
                case BASIC -> new CrateAttachment(0.891F, 0.891F, 0.516F);
                case MEDIUM, ADVANCED -> new CrateAttachment(0.891F, 0.984F, 0.516F);
            };
        }

        private record CrateAttachment(float halfWidth, float topY, float halfDepth) {
        }
    }
}
