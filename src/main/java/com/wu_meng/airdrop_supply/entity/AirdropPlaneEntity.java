package com.wu_meng.airdrop_supply.entity;

import com.wu_meng.airdrop_supply.entry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AirdropPlaneEntity extends Entity implements GeoEntity {
    private static final RawAnimation FLY_ANIMATION = RawAnimation.begin().thenPlay("plane_fly");
    private static final int LIFETIME_TICKS = 64;
    private static final int FLIGHT_HEIGHT_ABOVE_TERRAIN = 64;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AirdropPlaneEntity(EntityType<? extends AirdropPlaneEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /**
     * Calculates the shared flight and crate release height at a horizontal position.
     *
     * <p>The result is {@value #FLIGHT_HEIGHT_ABOVE_TERRAIN} blocks above the highest
     * motion-blocking terrain that is not foliage, clamped to the highest valid block
     * coordinate in the level.
     *
     * @param  level the server level containing the airdrop position
     * @param  x the block X coordinate of the airdrop position
     * @param  z the block Z coordinate of the airdrop position
     * @return the block Y coordinate at which the aircraft and crate are released
     */
    public static int calculateFlightY(ServerLevel level, int x, int z) {
        int terrainY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        return Math.min(terrainY + FLIGHT_HEIGHT_ABOVE_TERRAIN, level.getMaxBuildHeight() - 1);
    }

    public static void spawn(ServerLevel level, Entity fallingCrate) {
        AirdropPlaneEntity plane = ModEntities.AIRDROP_PLANE.get().create(level);
        if (plane == null) {
            return;
        }

        plane.moveTo(
                fallingCrate.getX(),
                fallingCrate.getY() - 16.0,
                fallingCrate.getZ(),
                level.random.nextFloat() * 360.0F,
                0.0F
        );
        level.addFreshEntity(plane);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount >= LIFETIME_TICKS) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 512.0 * 512.0;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(48.0, 24.0, 48.0);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "plane", 0,
                state -> state.setAndContinue(FLY_ANIMATION)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
