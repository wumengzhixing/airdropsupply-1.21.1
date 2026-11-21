package com.wu_meng.airdrop_supply.entity;

import com.wu_meng.airdrop_supply.block.AirdropSupplyBlock;
import com.wu_meng.airdrop_supply.entry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AirdropDeliveryVisualEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Integer> CARRIER_ID =
            SynchedEntityData.defineId(AirdropDeliveryVisualEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CRATE_TYPE =
            SynchedEntityData.defineId(AirdropDeliveryVisualEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CRATE_LEVEL =
            SynchedEntityData.defineId(AirdropDeliveryVisualEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AirdropDeliveryVisualEntity(EntityType<? extends AirdropDeliveryVisualEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static void spawn(ServerLevel level, FallingBlockEntity carrier,
                             AirdropSupplyBlock.Type type, AirdropSupplyBlock.CaseLevel caseLevel) {
        AirdropDeliveryVisualEntity visual = ModEntities.AIRDROP_DELIVERY_VISUAL.get().create(level);
        if (visual == null) {
            return;
        }

        visual.entityData.set(CARRIER_ID, carrier.getId());
        visual.entityData.set(CRATE_TYPE, type.ordinal());
        visual.entityData.set(CRATE_LEVEL, caseLevel.ordinal());
        visual.moveTo(carrier.getX(), carrier.getY(), carrier.getZ(), carrier.getYRot(), carrier.getXRot());
        level.addFreshEntity(visual);
    }

    public AirdropSupplyBlock.Type getCrateType() {
        AirdropSupplyBlock.Type[] values = AirdropSupplyBlock.Type.values();
        return values[Mth.clamp(this.entityData.get(CRATE_TYPE), 0, values.length - 1)];
    }

    public AirdropSupplyBlock.CaseLevel getCrateLevel() {
        AirdropSupplyBlock.CaseLevel[] values = AirdropSupplyBlock.CaseLevel.values();
        return values[Mth.clamp(this.entityData.get(CRATE_LEVEL), 0, values.length - 1)];
    }

    @Override
    public void tick() {
        super.tick();
        Entity carrier = this.level().getEntity(this.entityData.get(CARRIER_ID));
        if (carrier instanceof FallingBlockEntity && !carrier.isRemoved()) {
            this.setPos(carrier.getX(), carrier.getY(), carrier.getZ());
            this.setDeltaMovement(carrier.getDeltaMovement());
        } else if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(CARRIER_ID, -1);
        builder.define(CRATE_TYPE, 0);
        builder.define(CRATE_LEVEL, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(CARRIER_ID, tag.getInt("CarrierId"));
        this.entityData.set(CRATE_TYPE, tag.getInt("CrateType"));
        this.entityData.set(CRATE_LEVEL, tag.getInt("CrateLevel"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("CarrierId", this.entityData.get(CARRIER_ID));
        tag.putInt("CrateType", this.entityData.get(CRATE_TYPE));
        tag.putInt("CrateLevel", this.entityData.get(CRATE_LEVEL));
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
        return this.getBoundingBox().inflate(4.0);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
