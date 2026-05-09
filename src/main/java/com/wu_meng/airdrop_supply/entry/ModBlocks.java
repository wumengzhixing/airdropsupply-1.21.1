package com.wu_meng.airdrop_supply.entry;

import com.wu_meng.airdrop_supply.AirdropSupply;
import com.wu_meng.airdrop_supply.block.AirdropSupplyBlock;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("null")
public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AirdropSupply.MOD_ID);
    public static final DeferredBlock<AirdropSupplyBlock> AIRDROP_SUPPLY = BLOCKS.registerBlock("airdrop_supply", AirdropSupplyBlock::new, AirdropSupplyBlock.createProperties());
}
