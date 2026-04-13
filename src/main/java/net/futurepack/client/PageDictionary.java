package net.futurepack.client;

import net.futurepack.client.gui.EScanner.Entrys.IResearchPage;
import net.futurepack.client.gui.EScanner.Entrys.TextPage;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PageDictionary {

    private static final Map<String, Supplier<IResearchPage>> PAGE_SUPPLIERS = new HashMap<>();
    private static final Map<String, IResearchPage> CACHE = new HashMap<>();

    public static void onClientSetup(FMLClientSetupEvent event) {
//        TODO: remove in next refactoring

    }


    public static IResearchPage getPage(String nodeId) {
        if (CACHE.containsKey(nodeId)) {
            return CACHE.get(nodeId);
        }

        Supplier<IResearchPage> supplier = PAGE_SUPPLIERS.get(nodeId);
        if (supplier != null) {
            IResearchPage page = supplier.get();
            CACHE.put(nodeId, page);
            return page;
        }

        return new TextPage("§cОшибка: Страница для '" + nodeId + "' не существует.");
    }

    public static void clear() {
        PAGE_SUPPLIERS.clear();
        CACHE.clear();
    }


    public static void registerPage(String id, Supplier<IResearchPage> supplier) {
        PAGE_SUPPLIERS.put(id, supplier);
    }



}