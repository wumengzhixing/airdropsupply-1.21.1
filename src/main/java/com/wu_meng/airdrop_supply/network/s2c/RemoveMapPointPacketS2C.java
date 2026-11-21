package com.wu_meng.airdrop_supply.network.s2c;

import com.wu_meng.airdrop_supply.AirdropSupply;
import com.wu_meng.airdrop_supply.client.network.ClientMapPointPayloadHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RemoveMapPointPacketS2C(BlockPos dropPos) implements CustomPacketPayload {
    public static final Type<RemoveMapPointPacketS2C> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AirdropSupply.MOD_ID, "remove_map_point"));
    public static final StreamCodec<ByteBuf, RemoveMapPointPacketS2C> STREAM_CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, RemoveMapPointPacketS2C::dropPos, RemoveMapPointPacketS2C::new);

    @Override
    public Type<RemoveMapPointPacketS2C> type() {
        return TYPE;
    }

    public static void handle(RemoveMapPointPacketS2C payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT && ModList.get().isLoaded("xaerominimap")) {
                ClientMapPointPayloadHandler.queueRemoval(payload.dropPos());
            }
        });
    }
}
