package com.wu_meng.airdrop_supply.network.s2c;

import com.wu_meng.airdrop_supply.AirdropSupply;
import com.wu_meng.airdrop_supply.client.network.ClientPlayerAnimationPayloadHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public record PlayPlayerAnimationPacketS2C(int playerId) implements CustomPacketPayload {
    public static final Type<PlayPlayerAnimationPacketS2C> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AirdropSupply.MOD_ID, "play_player_animation"));

    public static final StreamCodec<FriendlyByteBuf, PlayPlayerAnimationPacketS2C> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PlayPlayerAnimationPacketS2C decode(@Nonnull FriendlyByteBuf buffer) {
            return new PlayPlayerAnimationPacketS2C(buffer.readVarInt());
        }

        @Override
        public void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull PlayPlayerAnimationPacketS2C value) {
            buffer.writeVarInt(value.playerId());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PlayPlayerAnimationPacketS2C payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ClientPlayerAnimationPayloadHandler.handle(payload);
            }
        });
    }
}
