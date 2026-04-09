package net.futurepack.network;

import net.futurepack.FuturepackNeo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public record SyncResearchPayload(Set<String> completedIds, Set<String> revealedIds) implements CustomPacketPayload {

    public static final Type<SyncResearchPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FuturepackNeo.MODID, "sync_research"));

    public static final StreamCodec<FriendlyByteBuf, SyncResearchPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8), SyncResearchPayload::completedIds,
            ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8), SyncResearchPayload::revealedIds,
            SyncResearchPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}