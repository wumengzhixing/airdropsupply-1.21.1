package com.wu_meng.airdrop_supply.client.camera;

import com.wu_meng.airdrop_supply.misc.Configuration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ViewportEvent;

import javax.annotation.Nullable;

public final class AirdropOpenCameraController {
    private static long activeUntilGameTime = -1L;
    @Nullable
    private static BlockPos activePos = null;

    @Nullable
    private static Entity previousCameraEntity = null;
    @Nullable
    private static ArmorStand cameraAnchor = null;

    private static boolean hasBonePose = false;
    private static Vec3 boneWorldPos = Vec3.ZERO;
    private static float boneYaw = 0F;
    private static float bonePitch = 0F;

    private AirdropOpenCameraController() {}

    public static void onAirdropOpened(BlockPos pos, BlockState state) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        activePos = pos.immutable();
        int durationTicks = Math.max(0, Configuration.OPEN_ANIMATION_DELAY_TICKS.get());
        activeUntilGameTime = mc.level.getGameTime() + durationTicks;
        hasBonePose = false;
        ensureAnchor(mc);
    }

    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (activePos == null || mc.level == null) {
            return;
        }
        if (mc.level.getGameTime() >= activeUntilGameTime) {
            stop(mc);
            return;
        }

        ensureAnchor(mc);

        if (hasBonePose) {
            event.setYaw(boneYaw);
            event.setPitch(bonePitch);
            if (cameraAnchor != null) {
                cameraAnchor.setPos(boneWorldPos.x, boneWorldPos.y, boneWorldPos.z);
                cameraAnchor.setYRot(boneYaw);
                cameraAnchor.setXRot(bonePitch);
            }
            return;
        }

        Vec3 cameraPos = event.getCamera().getPosition();
        Vec3 lookAt = Vec3.atCenterOf(activePos).add(0.0, 0.6, 0.0);
        Vec3 delta = lookAt.subtract(cameraPos);

        double dx = delta.x;
        double dy = delta.y;
        double dz = delta.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        if (horiz <= 1.0E-6) {
            return;
        }

        float yaw = (float) (Mth.atan2(dz, dx) * (180F / (float) Math.PI)) - 90F;
        float pitch = (float) (-(Mth.atan2(dy, horiz) * (180F / (float) Math.PI)));

        event.setYaw(yaw);
        event.setPitch(pitch);
    }

    public static boolean isActiveFor(BlockPos pos) {
        return activePos != null && activePos.equals(pos);
    }

    public static void updateFromBone(BlockPos pos, Vec3 worldPos, float yaw, float pitch) {
        if (activePos == null || !activePos.equals(pos)) {
            return;
        }
        boneWorldPos = worldPos;
        boneYaw = yaw;
        bonePitch = pitch;
        hasBonePose = true;
    }

    private static void ensureAnchor(Minecraft mc) {
        if (mc.level == null || mc.player == null) {
            return;
        }

        if (cameraAnchor == null || cameraAnchor.isRemoved() || cameraAnchor.level() != mc.level) {
            var created = EntityType.ARMOR_STAND.create(mc.level);
            if (!(created instanceof ArmorStand stand)) {
                return;
            }
            stand.setInvisible(true);
            stand.setNoGravity(true);
            stand.setInvulnerable(true);
            stand.setSilent(true);
            cameraAnchor = stand;
        }

        if (previousCameraEntity == null) {
            previousCameraEntity = mc.getCameraEntity();
        }
        if (mc.getCameraEntity() != cameraAnchor) {
            mc.setCameraEntity(cameraAnchor);
        }
    }

    private static void stop(Minecraft mc) {
        activePos = null;
        activeUntilGameTime = -1L;
        hasBonePose = false;
        boneWorldPos = Vec3.ZERO;
        boneYaw = 0F;
        bonePitch = 0F;

        if (previousCameraEntity != null && mc.getCameraEntity() != previousCameraEntity) {
            mc.setCameraEntity(previousCameraEntity);
        }
        previousCameraEntity = null;

        if (cameraAnchor != null) {
            cameraAnchor.remove(Entity.RemovalReason.DISCARDED);
        }
        cameraAnchor = null;
    }
}
