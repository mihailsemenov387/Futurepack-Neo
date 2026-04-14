package net.futurepack.network;

import com.google.gson.JsonObject;
import net.futurepack.FuturepackNeo;
import net.futurepack.client.PageDictionary;
import net.futurepack.research.ResearchNode;
import net.futurepack.research.ResearchRegistry;
import net.futurepack.research.ResearchTab;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper; // ВАЖНО
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record SyncResearchPayload(List<ResearchTab> tabs, List<ResearchNode> nodes, Map<String, PageEntry> pages) implements CustomPacketPayload {
    public static final Type<SyncResearchPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("futurepack", "sync_research"));

    // Кодек для JsonObject (через строку)
    private static final StreamCodec<RegistryFriendlyByteBuf, JsonObject> JSON_CODEC = StreamCodec.of(
            (buf, json) -> ByteBufCodecs.STRING_UTF8.encode(buf, json.toString()),
            (buf) -> net.minecraft.util.GsonHelper.parse(ByteBufCodecs.STRING_UTF8.decode(buf))
    );

    public record PageEntry(String type, JsonObject data) {
        public static final StreamCodec<RegistryFriendlyByteBuf, PageEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, PageEntry::type,
                JSON_CODEC, PageEntry::data,
                PageEntry::new
        );
    }
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncResearchPayload> STREAM_CODEC = StreamCodec.composite(
            ResearchTab.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncResearchPayload::tabs,
            ResearchNode.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncResearchPayload::nodes,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, PageEntry.STREAM_CODEC), SyncResearchPayload::pages,
            SyncResearchPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ResearchRegistry.clear();
            PageDictionary.clear();
            tabs.forEach(ResearchRegistry::registerTab);
            nodes.forEach(ResearchRegistry::registerNodeViaAPI);
            pages.forEach((id, p) -> PageDictionary.registerPageFromJson(id, p.type(), p.data()));
            FuturepackNeo.LOGGER.info("Futurepack: Synced from server!");
        });
    }
}