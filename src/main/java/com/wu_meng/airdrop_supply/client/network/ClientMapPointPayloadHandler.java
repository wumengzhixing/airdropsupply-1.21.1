package com.wu_meng.airdrop_supply.client.network;

import com.wu_meng.airdrop_supply.AirdropSupply;
import com.wu_meng.airdrop_supply.network.s2c.MapPointPacketS2C;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public final class ClientMapPointPayloadHandler {
    private static final String NAME = "AirDrop";
    private static final String SYMBOL = "air";
    private static final Set<BlockPos> PENDING_REMOVALS = new HashSet<>();

    private ClientMapPointPayloadHandler() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ClientMapPointPayloadHandler::onClientTick);
        NeoForge.EVENT_BUS.addListener(ClientMapPointPayloadHandler::onLogout);
    }

    public static void handle(MapPointPacketS2C payload) {
        var player = Minecraft.getInstance().player;
        var minimap = BuiltInHudModules.MINIMAP.getCurrentSession();
        if (player == null || minimap == null) {
            return;
        }

        var worldManager = minimap.getWorldManager();
        var world = worldManager.getCurrentWorld();
        var worldState = minimap.getWorldState();
        if (worldState.getAutoRootContainerPath() == null) {
            return;
        }
        if (world == null || world.getContainer().getRoot() != worldManager.getAutoRootContainer()) {
            world = worldState.getAutoWorldPath() == null ? null : worldManager.getAutoWorld();
        }
        if (world == null) {
            return;
        }

        try {
            BlockPos pos = payload.dropPos();
            // Apply an earlier removal before adding another drop at the same position.
            flushRemovals();
            Waypoint waypoint = new Waypoint(pos.getX(), pos.getY(), pos.getZ(),
                    NAME, SYMBOL, WaypointColor.getRandom(), WaypointPurpose.DESTINATION);
            world.getCurrentWaypointSet().add(waypoint);
            minimap.getWorldManagerIO().saveWorld(world);
            player.sendSystemMessage(Component.translatable("notification.airdrop_supply.map_point"));
        } catch (IOException e) {
            AirdropSupply.LOGGER.error("Could not save the Xaero airdrop waypoint", e);
        }
    }

    public static void queueRemoval(BlockPos position) {
        PENDING_REMOVALS.add(position);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        flushRemovals();
    }

    private static void flushRemovals() {
        if (PENDING_REMOVALS.isEmpty()) {
            return;
        }

        var minimap = BuiltInHudModules.MINIMAP.getCurrentSession();
        if (minimap == null || minimap.getWorldState().getAutoRootContainerPath() == null
                || minimap.getWorldState().getAutoWorldPath() == null) {
            return;
        }

        var worldManager = minimap.getWorldManager();
        var root = worldManager.getAutoRootContainer();
        if (root == null || worldManager.getAutoWorld() == null) {
            return;
        }

        for (var world : root.getAllWorldsIterable()) {
            boolean changed = false;
            for (var set : world.getIterableWaypointSets()) {
                for (int i = set.size() - 1; i >= 0; i--) {
                    Waypoint waypoint = set.get(i);
                    if (NAME.equals(waypoint.getName()) && SYMBOL.equals(waypoint.getSymbol())
                            && waypoint.getPurpose() == WaypointPurpose.DESTINATION
                            && PENDING_REMOVALS.contains(new BlockPos(waypoint.getX(), waypoint.getY(), waypoint.getZ()))) {
                        set.remove(i);
                        changed = true;
                    }
                }
            }
            if (changed) {
                try {
                    minimap.getWorldManagerIO().saveWorld(world);
                } catch (IOException e) {
                    AirdropSupply.LOGGER.error("Could not save the Xaero airdrop waypoint removal", e);
                }
            }
        }
        PENDING_REMOVALS.clear();
    }

    private static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        PENDING_REMOVALS.clear();
    }
}
