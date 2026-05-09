package com.wu_meng.airdrop_supply.item;

import com.wu_meng.airdrop_supply.AirdropSupply;
import com.wu_meng.airdrop_supply.entry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("null")
public class MobTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AirdropSupply.MOD_ID);
    // 工具
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> VENDING_MACHINE = TABS.register("vending_machine",
            () -> CreativeModeTab
                    .builder()
                    .title(Component.translatable("creativetab.airdrop_supply.airdrop_supply"))
                    .icon(() -> new ItemStack(ModItems.AIRDROP_PAGER.get()))
                    .displayItems((parameters, output)->{
                        output.accept(ModItems.AIRDROP_PAGER.get());
                        output.accept(ModItems.AIRDROP_LOCATION_CANCELLER.get());
                        output.accept(ModItems.AIRDROP_LOCATION_FIXER.get());
                    })
                    .build()
    );
}
