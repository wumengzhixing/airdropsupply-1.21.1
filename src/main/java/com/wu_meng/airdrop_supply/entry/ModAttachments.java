package com.wu_meng.airdrop_supply.entry;

import com.wu_meng.airdrop_supply.AirdropSupply;
import com.wu_meng.airdrop_supply.capability.AirdropPlayerData;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

@SuppressWarnings("null")
public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, AirdropSupply.MOD_ID);

    public static final Supplier<AttachmentType<AirdropPlayerData>> AIRDROP_PLAYER_DATA = ATTACHMENT_TYPES.register(
            "airdrop_player_data",
            () -> AttachmentType.serializable(AirdropPlayerData::new).copyOnDeath().build()
    );

    private ModAttachments() {}
}
