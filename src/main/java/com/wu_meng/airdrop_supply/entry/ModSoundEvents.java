package com.wu_meng.airdrop_supply.entry;

import com.wu_meng.airdrop_supply.AirdropSupply;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("null")
public class ModSoundEvents {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, AirdropSupply.MOD_ID);
    public static final DeferredHolder<SoundEvent, SoundEvent> OPEN_AIRDROP = register("open_airdrop");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AirdropSupply.MOD_ID, name)));
    }
}
