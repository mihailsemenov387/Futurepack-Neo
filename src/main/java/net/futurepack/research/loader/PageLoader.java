package net.futurepack.research.loader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.futurepack.client.PageDictionary;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.Map;

public class PageLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    public PageLoader() {
        // Путь: data/<modid>/futurepack/research/pages/
        super(GSON, "research/pages");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        PageDictionary.clear(); // Очищаем кэш перед загрузкой

        if (object.isEmpty()) {
            System.out.println("[Futurepack] WARNING: No JSON files found in data/futurepack/research/");
            return;
        }
        object.forEach((location, element) -> {
            try {
                JsonObject json = element.getAsJsonObject();
                String nodeId = location.getPath(); // Имя файла станет ID ноды
                String type = json.get("type").getAsString();
                JsonObject data = json.getAsJsonObject("data");

                // Регаем в твой словарь
                PageDictionary.registerPageFromJson(nodeId, type, data);
            } catch (Exception e) {
                System.err.println("[Futurepack] Failed to load page: " + location + " Error: " + e.getMessage());
            }
        });
        System.err.println("Futurepack: Loaded Pages!.");
    }
}