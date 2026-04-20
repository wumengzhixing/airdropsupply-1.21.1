package com.wu_meng.airdrop_supply;

import com.wu_meng.airdrop_supply.block.AirdropSupplyBlock;
import com.wu_meng.airdrop_supply.client.ModClientEvents;
import com.wu_meng.airdrop_supply.command.CallingAirdropCommand;
import com.wu_meng.airdrop_supply.entry.ModAttachments;
import com.wu_meng.airdrop_supply.entry.ModBlockEntities;
import com.wu_meng.airdrop_supply.entry.ModBlocks;
import com.wu_meng.airdrop_supply.entry.ModItems;
import com.wu_meng.airdrop_supply.entry.ModSoundEvents;
import com.wu_meng.airdrop_supply.item.MobTabs;
import com.wu_meng.airdrop_supply.misc.AirdropManager;
import com.wu_meng.airdrop_supply.misc.AmbushManager;
import com.wu_meng.airdrop_supply.misc.Configuration;
import com.wu_meng.airdrop_supply.network.NetworkHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;
import java.util.Objects;

@Mod(AirdropSupply.MOD_ID)
@SuppressWarnings("null")
public class AirdropSupply
{
    public static final String MOD_ID = "airdropsupply";
    public static final String CONTENT_ID = "airdrop_supply";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public AirdropSupply(@Nonnull IEventBus modEventBus, @Nonnull ModContainer modContainer)
    {
        IEventBus safeModEventBus = Objects.requireNonNull(modEventBus, "modEventBus");
        ModContainer safeModContainer = Objects.requireNonNull(modContainer, "modContainer");
        IEventBus gameEventBus = NeoForge.EVENT_BUS;

        safeModContainer.registerConfig(ModConfig.Type.COMMON, Configuration.COMMON_CONFIG);
        MobTabs.TABS.register(safeModEventBus);
        ModItems.ITEMS.register(safeModEventBus);
        ModBlocks.BLOCKS.register(safeModEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(safeModEventBus);
        ModSoundEvents.SOUNDS.register(safeModEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(safeModEventBus);
        NetworkHandler.register(safeModEventBus);

        AirdropManager.register(gameEventBus);
        gameEventBus.addListener(AirdropSupply::registerCommand);
        gameEventBus.addListener(AirdropSupply::onAddReloadListeners);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            safeModEventBus.addListener(ModClientEvents::onRegisterRenderers);
            // Temporary: disable open-crate camera animation hook.
            // gameEventBus.addListener(AirdropOpenCameraController::onComputeCameraAngles);
        }
    }

    public static void registerCommand(RegisterCommandsEvent event) {
        CallingAirdropCommand.register(event.getDispatcher());
    }

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(AmbushManager.INSTANCE);
    }

    public static final class LootTables {
        private LootTables() {}

        private static final ResourceKey<LootTable> BN = lootTableKey("ammo_basic");
        private static final ResourceKey<LootTable> MN = lootTableKey("ammo_medium");
        private static final ResourceKey<LootTable> AN = lootTableKey("ammo_advanced");
        private static final ResourceKey<LootTable> BM = lootTableKey("medic_basic");
        private static final ResourceKey<LootTable> MM = lootTableKey("medic_medium");
        private static final ResourceKey<LootTable> AM = lootTableKey("medic_advanced");

        public static ResourceKey<LootTable> calculateLootTable(AirdropSupplyBlock.Type type, AirdropSupplyBlock.CaseLevel caseLevel) {
            return switch (type) {
                case NORMAL -> switch (caseLevel) {
                    case BASIC -> BN;
                    case MEDIUM -> MN;
                    case ADVANCED -> AN;
                };
                case MEDIC -> switch (caseLevel) {
                    case BASIC -> BM;
                    case MEDIUM -> MM;
                    case ADVANCED -> AM;
                };
            };
        }

        private static ResourceKey<LootTable> lootTableKey(@Nonnull String path) {
            return ResourceKey.create(
                    Registries.LOOT_TABLE,
                    Objects.requireNonNull(ResourceLocation.fromNamespaceAndPath(CONTENT_ID, path), "loot table id")
            );
        }
    }
}
