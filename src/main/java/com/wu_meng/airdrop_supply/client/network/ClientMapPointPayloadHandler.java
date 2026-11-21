package com.wu_meng.airdrop_supply.client.network;

import com.wu_meng.airdrop_supply.AirdropSupply;
import com.wu_meng.airdrop_supply.network.s2c.MapPointPacketS2C;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointCollector;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.WaypointSession;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.hud.module.HudModule;

import java.util.Objects;

public final class ClientMapPointPayloadHandler {
    private ClientMapPointPayloadHandler() {}

    public static void handle(MapPointPacketS2C payload) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        try {
            BlockPos pos = payload.dropPos();
            Waypoint waypoint = new Waypoint(pos.getX(), pos.getY(), pos.getZ(), "AirDrop", "air", WaypointColor.getRandom(), WaypointPurpose.DESTINATION);
            MinimapSession minimap = BuiltInHudModules.MINIMAP.getCurrentSession();
            MinimapWorld currentWorld = minimap.getWorldManager().getCurrentWorld();
            currentWorld.getCurrentWaypointSet().add(waypoint);
            minimap.getWorldManagerIO().saveWorld(currentWorld);

            localPlayer.sendSystemMessage(Component.translatable("notification.airdrop_supply.map_point"));
        } catch (Exception e) {
            AirdropSupply.LOGGER.error("保存 Xaero 小地图航点失败", e);
        }
    }
}
