package net.futurepack.network;

import net.futurepack.FuturepackNeo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RequestReadPayload(String readId) implements CustomPacketPayload {

    // Свой собственный уникальный TYPE
    public static final Type<RequestReadPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(FuturepackNeo.MODID, "request_read")
    );

    // Кодек для передачи только одной строки (ID исследования)
    public static final StreamCodec<FriendlyByteBuf, RequestReadPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, RequestReadPayload::readId,
            RequestReadPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}