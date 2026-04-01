package com.wu_meng.airdrop_supply;

import com.wu_meng.airdrop_supply.block.AirdropSupplyBlock;
import com.wu_meng.airdrop_supply.client.camera.AirdropOpenCameraController;
import com.wu_meng.airdrop_supply.client.ModClientEvents; // 【新增导入：客户端事件】
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
import net.neoforged.api.distmarker.Dist; // 【新增导入：物理端判断】
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment; // 【新增导入：物理端判断】
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;
import java.util.Objects;

@Mod(AirdropSupply.MOD_ID)
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

        // 【最核心的一步】：安全地注册客户端渲染器
        // 判断当前物理端是否为客户端（CLIENT），如果是，就把 ModClientEvents 里的方法添加进事件总线
        if (FMLEnvironment.dist == Dist.CLIENT) {
            safeModEventBus.addListener(ModClientEvents::onRegisterRenderers);
            gameEventBus.addListener(AirdropOpenCameraController::onComputeCameraAngles);
        }
    }

    public static void registerCommand(RegisterCommandsEvent event) {
        CallingAirdropCommand.register(event.getDispatcher());
    }

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(AmbushManager.INSTANCE);
    }

    public static class LootTables{

        public static ResourceKey<LootTable> calculateLootTable(AirdropSupplyBlock.Type type, AirdropSupplyBlock.CaseLevel caseLevel){
            if(type== AirdropSupplyBlock.Type.NORMAL){
                return switch (caseLevel){
                    case BASIC -> BN;
                    case MEDIUM -> MN;
                    case ADVANCED -> AN;
                };
            } else{
                return switch (caseLevel){
                    case BASIC -> BM;
                    case MEDIUM -> MM;
                    case ADVANCED -> AM;
                };
            }
        }

        static final ResourceKey<LootTable> BN = lootTableKey("ammo_basic");
        static final ResourceKey<LootTable> MN = lootTableKey("ammo_medium");
        static final ResourceKey<LootTable> AN = lootTableKey("ammo_advanced");
        static final ResourceKey<LootTable> BM = lootTableKey("medic_basic");
        static final ResourceKey<LootTable> MM = lootTableKey("medic_medium");
        static final ResourceKey<LootTable> AM = lootTableKey("medic_advanced");

        private static ResourceKey<LootTable> lootTableKey(@Nonnull String path) {
            return ResourceKey.create(
                    Objects.requireNonNull(Registries.LOOT_TABLE, "Registries.LOOT_TABLE"),
                    Objects.requireNonNull(ResourceLocation.fromNamespaceAndPath(CONTENT_ID, path), "loot table id")
            );
        }
    }
}
