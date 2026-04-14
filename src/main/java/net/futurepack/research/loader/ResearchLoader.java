package net.futurepack.research.loader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.futurepack.client.PageDictionary;
import net.futurepack.network.SyncResearchPayload;
import net.futurepack.research.ResearchRegistry;
import net.futurepack.research.ResearchSystem;
import net.futurepack.research.ResearchTab;
import net.futurepack.research.ResearchNode;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Map;
import static net.futurepack.FuturepackNeo.LOGGER;

public class ResearchLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    public ResearchLoader() {
        super(GSON, "research");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        // Очищаем дерево перед перезагрузкой
        ResearchRegistry.init();
        if (object.isEmpty()) {
            System.out.println("[Futurepack] WARNING: No JSON files found in data/futurepack/research/");
            return;
        }

        object.forEach((location, element) -> {
            // Пропускаем папку pages, её читает другой лоадер
            if (location.getPath().startsWith("pages/")) return;

            try {
                JsonObject root = element.getAsJsonObject();
                String tabId = location.getPath();

                // 1. Регистрируем вкладку
                ResearchRegistry.registerTab(new ResearchTab(
                        tabId,
                        Component.translatable(root.get("title").getAsString()),
                        new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(root.get("icon").getAsString()))),
                        ResourceLocation.parse(root.get("bg").getAsString()),
                        ResourceLocation.parse(root.get("bg").getAsString())
                ));

                // 2. Читаем ноды этой вкладки
                JsonArray nodes = root.getAsJsonArray("nodes");
                nodes.forEach(nodeEl -> {
                    JsonObject n = nodeEl.getAsJsonObject();
                    String nodeId = n.get("id").getAsString();

                    var builder = ResearchSystem.create(nodeId)
                            .tab(tabId)
                            .title(n.get("title").getAsString())
                            .pos(n.get("x").getAsInt(), n.get("y").getAsInt())
                            .icon(BuiltInRegistries.ITEM.get(ResourceLocation.parse(n.get("icon").getAsString())))
//                            .frame(ResearchNode.NodeFrameType.valueOf(n.get("frame").getAsString()));
                            .frame(ResearchNode.NodeFrameType.valueOf(n.get("frame").getAsString().toUpperCase()));

                    if (n.has("requirements")) {
                        n.getAsJsonArray("requirements").forEach(req -> builder.requirements(req.getAsString()));
                    }
                    if (n.has("links")) {
                        n.getAsJsonArray("links").forEach(link -> builder.links(link.getAsString()));
                    }
                    if (n.has("hidden")) {
                        builder.hidden(n.get("hidden").getAsBoolean());
                    }


                    builder.build();


                });
            } catch (Exception e) {
                System.err.println("[Futurepack] Error parsing research file: " + location + " -> " + e.getMessage());
            }
        });

        if (ServerLifecycleHooks.getCurrentServer() != null) {
            var packet = new SyncResearchPayload(
                    new ArrayList<>(ResearchRegistry.getTabs()),
                    new ArrayList<>(ResearchRegistry.getNodes()),
                    PageDictionary.getRawDataForSync()
            );
            PacketDistributor.sendToAllPlayers(packet);
        }
        System.err.println("Futurepack: Loaded research!.");
    }
}