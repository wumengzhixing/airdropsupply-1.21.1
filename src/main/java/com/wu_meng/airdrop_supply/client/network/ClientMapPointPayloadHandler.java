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
import xaero.hud.minimap.waypoint.WaypointSession;
import xaero.hud.module.HudModule;

import java.util.Objects;

public final class ClientMapPointPayloadHandler {
    private ClientMapPointPayloadHandler() {}

    public static void handle(MapPointPacketS2C payload) {
        if (!ModList.get().isLoaded("xaerominimap")) return;
        LocalPlayer localPlayer = Minecraft.getInstance().player;


        try {
            BlockPos pos = payload.dropPos();
            Waypoint waypoint = new Waypoint(pos.getX(), pos.getY(), pos.getZ(), "AirDrop", "airdrop", WaypointColor.getRandom());
            MinimapSession minimap = BuiltInHudModules.MINIMAP.getCurrentSession();
            minimap.getWorldManager().getCurrentWorld().getCurrentWaypointSet().add(waypoint);

            localPlayer.sendSystemMessage(Component.translatable("notification.airdrop_supply.map_point"));
        } catch (Exception e) {
            AirdropSupply.LOGGER.error("保存 Xaero 小地图航点失败");
        }
    }
}
