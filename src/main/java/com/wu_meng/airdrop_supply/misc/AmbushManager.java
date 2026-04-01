package com.wu_meng.airdrop_supply.misc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.wu_meng.airdrop_supply.block.AirdropSupplyBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class AmbushManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    public static final AmbushManager INSTANCE = new AmbushManager();

    private static final AtomicReference<AmbushPool> POOL = new AtomicReference<>(new AmbushPool(0.05, List.of()));

    public AmbushManager() {
        super(GSON, "airdrop_ambush");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        double[] spawnChanceHolder = new double[] {0.05};
        var entriesBuilder = new java.util.ArrayList<AmbushEntry>();

        objectMap.forEach((location, element) -> {
            try {
                JsonObject json = element.getAsJsonObject();
                if (json.has("spawn_chance")) spawnChanceHolder[0] = json.get("spawn_chance").getAsDouble();

                json.getAsJsonArray("mobs").forEach(mobElement -> {
                    JsonObject mobJson = mobElement.getAsJsonObject();
                    String entityId = mobJson.get("type").getAsString();
                    int baseWeight = mobJson.has("base_weight") ? mobJson.get("base_weight").getAsInt() : mobJson.get("weight").getAsInt();
                    int multipleWeight = mobJson.has("multiple_weight") ? mobJson.get("multiple_weight").getAsInt() : 0;
                    int minCount = mobJson.has("min") ? mobJson.get("min").getAsInt() : 1;
                    int maxCount = mobJson.has("max") ? mobJson.get("max").getAsInt() : 1;

                    int minDay = mobJson.has("min_day") ? mobJson.get("min_day").getAsInt() : 1;
                    int maxDay = mobJson.has("max_day") ? mobJson.get("max_day").getAsInt() : Integer.MAX_VALUE;

                    AirdropSupplyBlock.Type crateType = null;
                    if (mobJson.has("crate_type")) {
                        String typeValue = mobJson.get("crate_type").getAsString();
                        if (!"any".equalsIgnoreCase(typeValue)) {
                            crateType = AirdropSupplyBlock.Type.valueOf(typeValue.toUpperCase());
                        }
                    }

                    AirdropSupplyBlock.CaseLevel crateLevel = null;
                    if (mobJson.has("crate_level")) {
                        String levelValue = mobJson.get("crate_level").getAsString();
                        if (!"any".equalsIgnoreCase(levelValue)) {
                            crateLevel = AirdropSupplyBlock.CaseLevel.valueOf(levelValue.toUpperCase());
                        }
                    }

                    entriesBuilder.add(new AmbushEntry(ResourceLocation.parse(entityId), baseWeight, multipleWeight, minCount, maxCount, minDay, maxDay, crateType, crateLevel));
                });
            } catch (Exception e) {
                LOGGER.error("Failed to load ambush pack: {}", location, e);
            }
        });

        POOL.set(new AmbushPool(spawnChanceHolder[0], List.copyOf(entriesBuilder)));
    }

    public static void trySpawnAmbush(Level level, BlockPos pos, AirdropSupplyBlock.Type crateType, AirdropSupplyBlock.CaseLevel crateLevel, int day) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AmbushPool pool = POOL.get();
        RandomSource random = level.getRandom();
        if (random.nextDouble() > pool.spawnChance) {
            return;
        }

        int totalWeight = 0;
        for (AmbushEntry entry : pool.entries) {
            if (!entry.matches(crateType, crateLevel, day)) {
                continue;
            }
            totalWeight += entry.effectiveWeight(day);
        }
        if (totalWeight <= 0) {
            return;
        }

        int roll = random.nextInt(totalWeight);
        AmbushEntry selectedEntry = null;
        for (AmbushEntry entry : pool.entries) {
            if (!entry.matches(crateType, crateLevel, day)) {
                continue;
            }
            int w = entry.effectiveWeight(day);
            roll -= w;
            if (roll < 0) {
                selectedEntry = entry;
                break;
            }
        }

        if (selectedEntry != null) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(selectedEntry.entityId);
            int spawnCount = Mth.nextInt(random, selectedEntry.min, selectedEntry.max);
            for (int i = 0; i < spawnCount; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 10;
                double offsetZ = (random.nextDouble() - 0.5) * 10;
                BlockPos spawnPos = BlockPos.containing(pos.getX() + offsetX, pos.getY() + 1, pos.getZ() + offsetZ);

                Entity entity = type.create(serverLevel);
                if (entity instanceof Mob mob) {
                    mob.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                    mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT, null);
                    serverLevel.addFreshEntity(mob);
                }
            }
        }
    }

    private record AmbushPool(double spawnChance, List<AmbushEntry> entries) {}

    private record AmbushEntry(
            ResourceLocation entityId,
            int baseWeight,
            int multipleWeight,
            int min,
            int max,
            int minDay,
            int maxDay,
            AirdropSupplyBlock.Type crateType,
            AirdropSupplyBlock.CaseLevel crateLevel
    ) {
        int effectiveWeight(int day) {
            long w = (long) baseWeight + (long) day * (long) multipleWeight;
            return (int) Math.max(0, Math.min(Integer.MAX_VALUE, w));
        }

        boolean matches(AirdropSupplyBlock.Type crateType, AirdropSupplyBlock.CaseLevel crateLevel, int day) {
            if (day < minDay || day > maxDay) {
                return false;
            }
            if (this.crateType != null && this.crateType != crateType) {
                return false;
            }
            if (this.crateLevel != null && this.crateLevel != crateLevel) {
                return false;
            }
            return true;
        }
    }
}
