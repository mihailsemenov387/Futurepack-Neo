package net.futurepack.client;

import com.google.gson.JsonObject;
import net.futurepack.client.gui.EScanner.Entrys.IResearchPage;
import net.futurepack.client.gui.EScanner.Entrys.RegistryPage;
import net.futurepack.client.gui.EScanner.Entrys.StudyPage;
import net.futurepack.client.gui.EScanner.Entrys.TextPage;
import net.futurepack.network.SyncResearchPayload;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PageDictionary {

    private static final Map<String, Supplier<IResearchPage>> PAGE_SUPPLIERS = new HashMap<>();
    private static final Map<String, SyncResearchPayload.PageEntry> RAW_SYNC_DATA = new HashMap<>();
    private static final Map<String, IResearchPage> CACHE = new HashMap<>();

    public static void onClientSetup(FMLClientSetupEvent event) {
//        TODO: remove in next refactoring

    }

    public static Map<String, SyncResearchPayload.PageEntry> getRawDataForSync() {
        return RAW_SYNC_DATA;
    }

    public static void registerPageFromJson(String nodeId, String type, JsonObject data) {
        RAW_SYNC_DATA.put(nodeId, new SyncResearchPayload.PageEntry(type, data));


        Supplier<IResearchPage> constructor = RegistryPage.AVALIBLE_PAGE_TYPES.get(type);

        if (constructor != null) {
            registerPage(nodeId, () -> {
                IResearchPage page = constructor.get(); // Вызывает new StudyPage()
                page.readContentFromJson(data);        // Заполняет данными
                return page;
            });
        } else {
            System.err.println("[Futurepack] Ошибка: Неизвестный тип страницы '" + type + "' для ноды " + nodeId);
        }

    }

    public static IResearchPage getPage(String nodeId) {
        if (CACHE.containsKey(nodeId)) return CACHE.get(nodeId);
        Supplier<IResearchPage> supplier = PAGE_SUPPLIERS.get(nodeId);
        if (supplier != null) {
            IResearchPage page = supplier.get();
            CACHE.put(nodeId, page);
            return page;
        }
        return new TextPage("§cОшибка: Контент не найден для " + nodeId);
    }

    public static void clear() {
        PAGE_SUPPLIERS.clear();
        RAW_SYNC_DATA.clear();
        CACHE.clear();
    }


    public static void registerPage(String id, Supplier<IResearchPage> supplier) {
        PAGE_SUPPLIERS.put(id, supplier);
    }



}