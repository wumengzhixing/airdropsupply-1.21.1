package com.wu_meng.airdrop_supply.network;

import com.wu_meng.airdrop_supply.network.s2c.MapPointPacketS2C;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@SuppressWarnings("null")
public final class NetworkHandler {
    public static final String PROTOCOL_VERSION = "1";

    private NetworkHandler() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(NetworkHandler::registerPayloads);
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(MapPointPacketS2C.TYPE, MapPointPacketS2C.STREAM_CODEC, MapPointPacketS2C::handle);
    }
}
