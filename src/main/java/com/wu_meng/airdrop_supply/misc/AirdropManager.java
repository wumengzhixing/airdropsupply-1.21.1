package com.wu_meng.airdrop_supply.misc;

import com.wu_meng.airdrop_supply.entry.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayDeque;
import java.util.Queue;

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
            var it = data.airdropDespawnInfo.iterator();
            while (it.hasNext()){
                var p = it.next();
                if(overworld.getGameTime()>=p.getFirst()){
                    var pos = p.getSecond();
                    if (pos == null) {
                        it.remove();
                        continue;
                    }
                    player.sendSystemMessage(Component.translatable("notification.airdrop_supply.airdrop_invalidate",pos.getX(),pos.getY(),pos.getZ(),player.getScoreboardName()));
                    it.remove();
                }
            }

            if(data.nextAirdropCountdown==0){
                queue.add(new AirdropMission(player));
                data.nextAirdropCountdown = Configuration.AIRDROP_SPAWN_INTERVAL.get();
            }
            data.nextAirdropCountdown--;
        }
    }

    private static void tickWorld(ServerTickEvent.Pre event){
        if(!queue.isEmpty()){
            if(queue.peek().done()){
                queue.poll();
            } else queue.peek().run();
        }
    }

}
