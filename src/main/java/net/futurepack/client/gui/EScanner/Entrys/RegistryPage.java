package net.futurepack.client.gui.EScanner.Entrys;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class RegistryPage {

    public static final Map<String, Supplier<IResearchPage>> AVALIBLE_PAGE_TYPES = new HashMap<>();


    public static void init(){
        register("text", TextPage::new);
        register("study", StudyPage::new);
    }

    private static void register (String type, Supplier<IResearchPage> supplier){
        AVALIBLE_PAGE_TYPES.put(type, supplier);

    }
}
