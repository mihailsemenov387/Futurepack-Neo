package net.futurepack.client;

import net.futurepack.client.gui.EScanner.Entrys.IResearchPage;
import net.futurepack.client.gui.EScanner.Entrys.StudyPage;
import net.futurepack.client.gui.EScanner.Entrys.TextPage;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.HashMap;
import java.util.Map;

public class PageDictionary {

    private static final Map<String, IResearchPage> PAGES = new HashMap<>();

    // Это метод инициализации, который мы вызовем вручную
    public static void onClientSetup(FMLClientSetupEvent event) {
        PAGES.clear();

        // Твои страницы
        PAGES.put("start", new TextPage("Добро пожаловать в Futurepack!"));
        PAGES.put("neon", new StudyPage("Изучите свойства неона.", "textures/gui/entries/neon_img.png"));
        PAGES.put("ufo", new StudyPage("Изучите свойства неона.", "textures/gui/entries/neon_img.png"));

        System.out.println("[Futurepack] PageDictionary: " + PAGES.size() + " pages loaded.");
    }

    public static IResearchPage getPage(String nodeId) {
        return PAGES.get(nodeId);
    }
}