package com.wu_meng.airdrop_supply.entry;

import com.wu_meng.airdrop_supply.AirdropSupply;
import com.wu_meng.airdrop_supply.blockentity.AirdropSupplyBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("null")
public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AirdropSupply.MOD_ID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AirdropSupplyBlockEntity>> AIRDROP_SUPPLY = BLOCK_ENTITIES.register("airdrop_supply", () ->
            BlockEntityType.Builder.of(AirdropSupplyBlockEntity::new, ModBlocks.AIRDROP_SUPPLY.get()).build(null));

}
