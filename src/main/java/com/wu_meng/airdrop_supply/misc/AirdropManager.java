package com.wu_meng.airdrop_supply.misc;

import com.wu_meng.airdrop_supply.capability.AirdropPlayerData;
import com.wu_meng.airdrop_supply.entry.ModAttachments;
import com.wu_meng.airdrop_supply.network.s2c.MapPointPacketS2C;
import com.wu_meng.airdrop_supply.network.s2c.RemoveMapPointPacketS2C;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

@SuppressWarnings("null")
public class AirdropManager {

    private static final Queue<AirdropMission> queue = new ArrayDeque<>();

    public static void register(IEventBus eventBus){
        eventBus.addListener(AirdropManager::tickPlayer);
        eventBus.addListener(AirdropManager::tickWorld);
    }

    private static void tickPlayer(PlayerTickEvent.Pre event){
        if(!event.getEntity().level().isClientSide()){
            ServerPlayer player = (ServerPlayer) event.getEntity();
            var data = player.getData(ModAttachments.AIRDROP_PLAYER_DATA);

            var server = player.getServer();
            if (server == null) {
                return;
            }
            var overworld = server.overworld();
            removeFinishedWaypoints(player, overworld, data);

            if(data.nextAirdropCountdown==0){
                queue.add(new AirdropMission(player));
                data.nextAirdropCountdown = Configuration.AIRDROP_SPAWN_INTERVAL.get();
            }
            data.nextAirdropCountdown--;
        }
    }

    public static void trackWaypoint(ServerPlayer player, FallingBlockEntity crate, BlockPos position,
                                     long expiresAt, boolean announceExpiry) {
        var id = crate.getUUID();
        crate.blockData.putUUID("AirdropWaypointId", id);
        var data = player.getData(ModAttachments.AIRDROP_PLAYER_DATA);
        data.waypoints.add(new AirdropPlayerData.Waypoint(id, expiresAt, position, announceExpiry));
        PacketDistributor.sendToPlayer(player, new MapPointPacketS2C(position));
    }

    private static void removeFinishedWaypoints(ServerPlayer player, ServerLevel overworld, AirdropPlayerData data) {
        if (data.waypoints.isEmpty()) {
            return;
        }

        long now = overworld.getGameTime();
        var completed = AirdropWaypointRemovals.get(overworld);
        Set<BlockPos> removedPositions = new HashSet<>();
        var iterator = data.waypoints.iterator();
        while (iterator.hasNext()) {
            var waypoint = iterator.next();
            boolean removedEarly = completed.consume(waypoint.id());
            if (!removedEarly && now < waypoint.expiresAt()) {
                continue;
            }

            BlockPos pos = waypoint.position();
            if (!removedEarly && waypoint.announceExpiry()) {
                player.sendSystemMessage(Component.translatable("notification.airdrop_supply.airdrop_invalidate",
                        pos.getX(), pos.getY(), pos.getZ(), player.getScoreboardName()));
            }
            removedPositions.add(pos);
            iterator.remove();
        }

        if (!removedPositions.isEmpty() && NetworkRegistry.hasChannel(player.connection, RemoveMapPointPacketS2C.TYPE.id())) {
            // Multiple active drops may share the same waypoint.
            for (var waypoint : data.waypoints) {
                removedPositions.remove(waypoint.position());
            }
            for (BlockPos pos : removedPositions) {
                PacketDistributor.sendToPlayer(player, new RemoveMapPointPacketS2C(pos));
            }
        }
    }

    private static void tickWorld(ServerTickEvent.Pre event){
        if (event.getServer().getTickCount() % 1200 == 0) {
            ServerLevel overworld = event.getServer().overworld();
            AirdropWaypointRemovals.get(overworld).discardExpired(overworld.getGameTime());
        }
        if(!queue.isEmpty()){
            if(queue.peek().done()){
                queue.poll();
            } else queue.peek().run();
        }
    }

}
