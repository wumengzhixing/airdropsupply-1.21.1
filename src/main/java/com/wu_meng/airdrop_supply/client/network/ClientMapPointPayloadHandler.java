package com.wu_meng.airdrop_supply.client.network;

import com.wu_meng.airdrop_supply.AirdropSupply;
import com.wu_meng.airdrop_supply.network.s2c.MapPointPacketS2C;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;

import java.util.Objects;

public final class ClientMapPointPayloadHandler {
    private ClientMapPointPayloadHandler() {}

    public static void handle(MapPointPacketS2C payload) {
        if (!ModList.get().isLoaded("xaerominimap")) {
            return;
        }

        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) {
            return;
        }

        localPlayer.sendSystemMessage(Objects.requireNonNull(Component.translatable("notification.airdrop_supply.map_point"), "message"));

        try {
            Object connection = localPlayer.connection;
            Class<?> handlerInterface = Class.forName("xaero.common.core.IXaeroMinimapClientPlayNetHandler");
            if (!handlerInterface.isInstance(connection)) {
                return;
            }

            Object session = handlerInterface.getMethod("getXaero_minimapSession").invoke(connection);
            Object waypointsManager = session.getClass().getMethod("getWaypointsManager").invoke(session);
            Object waypoints = waypointsManager.getClass().getMethod("getWaypoints").invoke(waypointsManager);
            Object list = waypoints.getClass().getMethod("getList").invoke(waypoints);

            String name = Objects.requireNonNull(Component.translatable("container.airdrop_supply.airdrop_supply").getString(), "name");
            String shortName = name.isEmpty() ? "A" : name.substring(0, 1);

            Class<?> modSettingsClass = Class.forName("xaero.common.settings.ModSettings");
            Object enchantColors = modSettingsClass.getField("ENCHANT_COLORS").get(null);
            int colorCount = enchantColors instanceof int[] colors ? colors.length : 1;
            int colorIndex = (int) (Math.random() * colorCount);

            Class<?> waypointClass = Class.forName("xaero.common.minimap.waypoints.Waypoint");
            Object waypoint = waypointClass
                    .getConstructor(int.class, int.class, int.class, String.class, String.class, int.class)
                    .newInstance(payload.dropPos().getX(), payload.dropPos().getY(), payload.dropPos().getZ(), name, shortName, colorIndex);
            list.getClass().getMethod("add", Object.class).invoke(list, waypoint);

            Object xaeroMinimap = Class.forName("xaero.minimap.XaeroMinimap").getField("instance").get(null);
            Object settings = xaeroMinimap.getClass().getMethod("getSettings").invoke(xaeroMinimap);
            Object currentWorld = waypointsManager.getClass().getMethod("getCurrentWorld").invoke(waypointsManager);
            settings.getClass().getMethod("saveWaypoints", currentWorld.getClass()).invoke(settings, currentWorld);
        } catch (Exception e) {
            AirdropSupply.LOGGER.error("保存 Xaero 小地图航点失败", e);
        }
    }
}
