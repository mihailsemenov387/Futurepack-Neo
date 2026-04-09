package net.futurepack.network;

import net.futurepack.FuturepackNeo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RequestCompletePayload(String researchId) implements CustomPacketPayload {

    // Свой собственный уникальный TYPE
    public static final Type<RequestCompletePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(FuturepackNeo.MODID, "request_complete")
    );

    // Кодек для передачи только одной строки (ID исследования)
    public static final StreamCodec<FriendlyByteBuf, RequestCompletePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, RequestCompletePayload::researchId,
            RequestCompletePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}