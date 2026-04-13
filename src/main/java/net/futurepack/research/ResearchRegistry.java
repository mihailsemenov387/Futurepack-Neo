package net.futurepack.research;

import net.futurepack.client.gui.EScanner.Entrys.StudyPage;
import net.futurepack.client.gui.EScanner.Entrys.TextPage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.*;

public class ResearchRegistry {
    private static final Map<String, ResearchTab> TABS = new LinkedHashMap<>();
    private static final Map<String, ResearchNode> NODES = new HashMap<>();
    private static final Map<String, List<ResearchNode>> NODES_BY_TAB = new HashMap<>();

    public static void registerTab(ResearchTab tab) {
        if (TABS.containsKey(tab.id())) throw new IllegalStateException("Duplicate Tab ID: " + tab.id());
        TABS.put(tab.id(), tab);
        NODES_BY_TAB.put(tab.id(), new ArrayList<>());
    }

    public static void registerNode(ResearchNode node) {
        if (NODES.containsKey(node.id())) throw new IllegalStateException("Duplicate Node ID: " + node.id());
        if (!TABS.containsKey(node.tabId())) throw new IllegalArgumentException("Unknown Tab: " + node.tabId());

        NODES.put(node.id(), node);
        NODES_BY_TAB.get(node.tabId()).add(node);
    }



    public static void init() {
        TABS.clear(); NODES.clear(); NODES_BY_TAB.clear();


//
//        // 1. Сначала регистрируем ТАБЫ
//        registerTab(new ResearchTab("start", Component.literal("Основы"), new ItemStack(Items.BOOK),
//                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png"),
//                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png")));
//
//        registerTab(new ResearchTab("space", Component.literal("Основы"), new ItemStack(Items.BOOK),
//                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png"),
//                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png")));
//
////         2. Теперь регистрируем НОДЫ через твое новое API
//        ResearchSystem.create("start").tab("start")
//                .pos(0, 0)
//                .title("Начало пути")
//                .icon(Items.COMPASS)
//                .frame(ResearchNode.NodeFrameType.ERK)
//                .page(() -> new TextPage("Добро пожаловать в мир Futurepack!"))
//                .build();
//
//        ResearchSystem.create("ufo").tab("space")
//                .pos(30, -60).requirements("start")
//                .title("Технологии пришельцев")
//                .icon(Items.BEACON)
//                .frame(ResearchNode.NodeFrameType.HEXAGON).hidden(true)
//                .page(() -> new StudyPage("Эти данные были получены из обломков...", "textures/gui/entries/ufo.png"))
//                .build();
    }

    private static void add(String id, String tab, String name, net.minecraft.world.item.Item icon, int x, int y, List<String> links, List<String> reqs, boolean hidden, ResearchNode.NodeFrameType frame) {
        registerNode(new ResearchNode(id, tab, Component.literal(name), new ItemStack(icon), null, x, y, links, reqs, hidden, frame));
    }



    public static Collection<ResearchTab> getTabs() { return TABS.values(); }
    public static ResearchTab getTab(String id) { return TABS.get(id); }
    public static ResearchNode getNode(String id) { return NODES.get(id); }
    public static List<ResearchNode> getNodesForTab(String tabId) { return NODES_BY_TAB.getOrDefault(tabId, List.of()); }
    public static Set<String> getAllIds() { return NODES.keySet(); }


    public static void registerNodeViaAPI(ResearchNode node){
        if (NODES.containsKey(node.id())) {
            throw new IllegalStateException("ОШИБКА РЕГИСТРАЦИИ: Нода с ID '" + node.id() + "' уже существует!");
        }

        // 2. ПРОВЕРКА СУЩЕСТВОВАНИЯ ТАБА (Внешний ключ)
        if (!TABS.containsKey(node.tabId())) {
            throw new IllegalArgumentException("ОШИБКА: Попытка привязать ноду '" + node.id() + "' к несуществующей вкладке '" + node.tabId() + "'!");
        }

        // Если всё ок — вызываем внутреннюю регистрацию
        registerNode(node);
    }
}