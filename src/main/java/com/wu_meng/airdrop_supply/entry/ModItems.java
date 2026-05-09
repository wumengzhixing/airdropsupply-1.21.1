package com.wu_meng.airdrop_supply.entry;

import com.wu_meng.airdrop_supply.AirdropSupply;
import com.wu_meng.airdrop_supply.item.AirdropLocationFixerItem;
import com.wu_meng.airdrop_supply.item.AirdropPagerItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AirdropSupply.MOD_ID);
    public static final DeferredItem<AirdropPagerItem> AIRDROP_PAGER = ITEMS.register("airdrop_pager", AirdropPagerItem::new);
    public static final DeferredItem<AirdropLocationFixerItem> AIRDROP_LOCATION_FIXER = ITEMS.register("airdrop_location_fixer", () -> new AirdropLocationFixerItem(false));
    public static final DeferredItem<AirdropLocationFixerItem> AIRDROP_LOCATION_CANCELLER = ITEMS.register("airdrop_location_canceller", () -> new AirdropLocationFixerItem(true));

}
