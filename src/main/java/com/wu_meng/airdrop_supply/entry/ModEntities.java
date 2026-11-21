package com.wu_meng.airdrop_supply.entry;

import com.wu_meng.airdrop_supply.AirdropSupply;
import com.wu_meng.airdrop_supply.entity.AirdropDeliveryVisualEntity;
import com.wu_meng.airdrop_supply.entity.AirdropPlaneEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, AirdropSupply.CONTENT_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<AirdropPlaneEntity>> AIRDROP_PLANE =
            ENTITY_TYPES.register("airdrop_plane", () -> EntityType.Builder
                    .of(AirdropPlaneEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .noSave()
                    .clientTrackingRange(32)
                    .updateInterval(1)
                    .build(AirdropSupply.CONTENT_ID + ":airdrop_plane"));

    public static final DeferredHolder<EntityType<?>, EntityType<AirdropDeliveryVisualEntity>> AIRDROP_DELIVERY_VISUAL =
            ENTITY_TYPES.register("airdrop_delivery_visual", () -> EntityType.Builder
                    .of(AirdropDeliveryVisualEntity::new, MobCategory.MISC)
                    .sized(1.0F, 4.0F)
                    .noSave()
                    .clientTrackingRange(32)
                    .updateInterval(1)
                    .build(AirdropSupply.CONTENT_ID + ":airdrop_delivery_visual"));

    private ModEntities() {}
}
