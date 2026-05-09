package com.wu_meng.airdrop_supply.misc;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Configuration {

    public static final ModConfigSpec COMMON_CONFIG;

    public static ModConfigSpec.EnumValue<CenterType> AIRDROP_CENTER_MODE;
    public static ModConfigSpec.IntValue CUSTOM_CENTER_X;
    public static ModConfigSpec.IntValue CUSTOM_CENTER_Z;
    public static ModConfigSpec.BooleanValue BROADCAST_COORDINATES;

    public static ModConfigSpec.IntValue AIRDROP_SPREAD_RANGE;
    public static ModConfigSpec.IntValue AIRDROP_SPAWN_INTERVAL;
    public static ModConfigSpec.IntValue AIRDROP_DESPAWN_TIME;

    public static ModConfigSpec.IntValue AMBUSH_START_AFTER_TICKS;
    public static ModConfigSpec.IntValue AMBUSH_ATTEMPT_INTERVAL_TICKS;

    public static ModConfigSpec.IntValue MEDIC_AIRDROP_WEIGHT;
    public static ModConfigSpec.IntValue AMMO_AIRDROP_WEIGHT;
    public static ModConfigSpec.IntValue NO_AIRDROP_WEIGHT;

    public static ModConfigSpec.IntValue BASIC_BASE_WEIGHT;
    public static ModConfigSpec.IntValue BASIC_MULTIPLE_WEIGHT;
    public static ModConfigSpec.IntValue MEDIUM_BASE_WEIGHT;
    public static ModConfigSpec.IntValue MEDIUM_MULTIPLE_WEIGHT;
    public static ModConfigSpec.IntValue ADVANCED_BASE_WEIGHT;
    public static ModConfigSpec.IntValue ADVANCED_MULTIPLE_WEIGHT;

    public enum CenterType {
        PLAYER, WORLD_SPAWN, CUSTOM
    }

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment(
                "Airdrop General Settings",
                "空投基础设置"
        ).push("airdrop_general_settings");

        AIRDROP_CENTER_MODE = builder.comment(
                "Airdrop center location mode.",
                "PLAYER: Centered around the player.",
                "WORLD_SPAWN: Centered around the world spawn point.",
                "CUSTOM: Use custom coordinates below.",
                "空投中心位置参考模式。",
                "PLAYER: 以玩家当前位置为中心。",
                "WORLD_SPAWN: 以世界重生点为中心。",
                "CUSTOM: 使用下方自定义坐标。"
        ).defineEnum("AIRDROP_CENTER_MODE", CenterType.PLAYER);

        CUSTOM_CENTER_X = builder.comment(
                "Custom center X coordinate. Only effective when mode is CUSTOM.",
                "自定义中心 X 坐标。仅当模式为 CUSTOM 时生效。"
        ).defineInRange("CUSTOM_CENTER_X", 0, -30000000, 30000000);

        CUSTOM_CENTER_Z = builder.comment(
                "Custom center Z coordinate. Only effective when mode is CUSTOM.",
                "自定义中心 Z 坐标。仅当模式为 CUSTOM 时生效。"
        ).defineInRange("CUSTOM_CENTER_Z", 0, -30000000, 30000000);

        BROADCAST_COORDINATES = builder.comment(
                "Broadcast airdrop arrival coordinates to all players on the server.",
                "是否向全服所有玩家广播空投到达的坐标信息。"
        ).define("BROADCAST_COORDINATES", false);

        builder.pop();

        builder.comment(
                "Airdrop Spawn Settings",
                "空投生成规则设置"
        ).push("airdrop_spawn_setting");

        AIRDROP_SPREAD_RANGE = builder.comment(
                "The maximum spread radius for airdrop spawns around the center point.",
                "空投生成的最大扩散半径（以中心点为原点）。"
        ).defineInRange("AIRDROP_SPREAD_RANGE", 1500, 10, 10000);

        AIRDROP_SPAWN_INTERVAL = builder.comment(
                "Time interval between automatic airdrops in ticks (1 sec = 20 ticks, 1 day = 24000 ticks).",
                "自动空投的生成时间间隔，单位为 tick（1 秒 = 20 ticks，1 游戏日 = 24000 ticks）。"
        ).defineInRange("AIRDROP_SPAWN_INTERVAL", 3 * 24000, 100, 30 * 24000);

        AIRDROP_DESPAWN_TIME = builder.comment(
                "Time before an unopened airdrop despawns in ticks.",
                "未开启的空投自然消失的倒计时，单位为 tick。"
        ).defineInRange("AIRDROP_DESPAWN_TIME", 24000, 100, 365 * 24000);

        builder.pop();

        builder.comment(
                "Airdrop Ambush Settings",
                "空投伏击设置（长时间未开启时在附近刷怪）"
        ).push("airdrop_ambush_setting");

        AMBUSH_START_AFTER_TICKS = builder.comment(
                "Start trying to spawn ambush after this many server ticks since crate landed.",
                "空投箱落地后经过多少 tick 才开始尝试刷怪。"
        ).defineInRange("AMBUSH_START_AFTER_TICKS", 1200, 0, 365 * 24000);

        AMBUSH_ATTEMPT_INTERVAL_TICKS = builder.comment(
                "Ambush spawn attempt interval in ticks after start delay.",
                "开始尝试后，每隔多少 tick 尝试一次刷怪。"
        ).defineInRange("AMBUSH_ATTEMPT_INTERVAL_TICKS", 100, 1, 24000);

        builder.pop();

        builder.comment(
                "Airdrop Type Weight Settings",
                "Determine the probability of which type of crate will be dropped.",
                "空投类型权重设置，决定掉落哪种箱子或坠毁。"
        ).push("airdrop_type_weight");

        MEDIC_AIRDROP_WEIGHT = builder.comment(
                "Weight for Medic Supply Airdrop.",
                "医疗补给箱的生成权重。"
        ).defineInRange("MEDIC_AIRDROP_WEIGHT", 5, 0, 10000);

        AMMO_AIRDROP_WEIGHT = builder.comment(
                "Weight for Ammo Supply Airdrop.",
                "武器弹药补给箱的生成权重。"
        ).defineInRange("AMMO_AIRDROP_WEIGHT", 3, 0, 10000);

        NO_AIRDROP_WEIGHT = builder.comment(
                "Weight for No Airdrop (crate crash event without loot).",
                "无空投（即空投坠毁事件，无物资）的生成权重。"
        ).defineInRange("NO_AIRDROP_WEIGHT", 2, 0, 10000);

        builder.pop();

        builder.comment(
                "Airdrop Level Weight Settings",
                "Final Weight = BASE_WEIGHT + (Current Day * MULTIPLE_WEIGHT).",
                "等级权重设置。动态计算公式：最终权重 = 基础权重 + (当前游戏天数 * 倍率权重)。"
        ).push("airdrop_level_weight");

        BASIC_BASE_WEIGHT = builder.comment(
                "Base weight for Basic level crate.",
                "基础级空投箱的基础权重。"
        ).defineInRange("BASIC_BASE_WEIGHT", 100, 0, 10000);

        BASIC_MULTIPLE_WEIGHT = builder.comment(
                "Daily multiple weight for Basic level crate.",
                "基础级空投箱随天数增加的倍率权重。"
        ).defineInRange("BASIC_MULTIPLE_WEIGHT", 0, 0, 10000);

        MEDIUM_BASE_WEIGHT = builder.comment(
                "Base weight for Medium level crate.",
                "中级空投箱的基础权重。"
        ).defineInRange("MEDIUM_BASE_WEIGHT", -10, -10000, 10000);

        MEDIUM_MULTIPLE_WEIGHT = builder.comment(
                "Daily multiple weight for Medium level crate.",
                "中级空投箱随天数增加的倍率权重。"
        ).defineInRange("MEDIUM_MULTIPLE_WEIGHT", 1, -10000, 10000);

        ADVANCED_BASE_WEIGHT = builder.comment(
                "Base weight for Advanced level crate.",
                "高级空投箱的基础权重。"
        ).defineInRange("ADVANCED_BASE_WEIGHT", -60, -10000, 10000);

        ADVANCED_MULTIPLE_WEIGHT = builder.comment(
                "Daily multiple weight for Advanced level crate.",
                "高级空投箱随天数增加的倍率权重。"
        ).defineInRange("ADVANCED_MULTIPLE_WEIGHT", 3, -10000, 10000);

        builder.pop();

        COMMON_CONFIG = builder.build();
    }
}
