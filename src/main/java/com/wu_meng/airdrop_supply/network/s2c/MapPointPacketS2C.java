package com.wu_meng.airdrop_supply.network.s2c;

import com.wu_meng.airdrop_supply.AirdropSupply;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

@SuppressWarnings("null")
public record MapPointPacketS2C(BlockPos dropPos) implements CustomPacketPayload {
    public static final Type<MapPointPacketS2C> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AirdropSupply.MOD_ID, "map_point"));
    public static final StreamCodec<FriendlyByteBuf, MapPointPacketS2C> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public MapPointPacketS2C decode(@Nonnull FriendlyByteBuf buffer) {
            return new MapPointPacketS2C(buffer.readBlockPos());
        }

        @Override
        public void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull MapPointPacketS2C value) {
            buffer.writeBlockPos(value.dropPos);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MapPointPacketS2C payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist != Dist.CLIENT) {
                return;
            }
            try {
                Class<?> handler = Class.forName("com.wu_meng.airdrop_supply.client.network.ClientMapPointPayloadHandler");
                handler.getMethod("handle", MapPointPacketS2C.class).invoke(null, payload);
            } catch (Exception e) {
                AirdropSupply.LOGGER.error("处理客户端地图标记失败", e);
            }
        });
    }
}
