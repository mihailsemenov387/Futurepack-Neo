package net.futurepack.research;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.*;

public class ResearchManager {
    private static final Map<String, ResearchTab> TABS = new LinkedHashMap<>();
    private static final Map<String, ResearchNode> NODES = new HashMap<>();
    private static final Map<String, List<ResearchNode>> NODES_BY_TAB = new HashMap<>();

    public static void init() {
        TABS.clear(); NODES.clear(); NODES_BY_TAB.clear();

        // 1. Регистрируем табы
        registerTab(new ResearchTab("space", Component.literal("Космос"), new ItemStack(Items.ENDER_EYE)));
        registerTab(new ResearchTab("adventure", Component.literal("Приключения"), new ItemStack(Items.IRON_SWORD)));

        // 2. Регистрируем ноды
        registerNode(new ResearchNode("start", "space",
                Component.literal("Основы"), Component.literal("Начало пути."),
                new ItemStack(Items.COMPASS), 0, 0,
                List.of(), List.of(), false));

        registerNode(new ResearchNode("neon", "space",
                Component.literal("Неон"), Component.literal("Энергия газов."),
                new ItemStack(Items.BEACON), 0, -60,
                List.of("start"), List.of("start"), false));

        registerNode(new ResearchNode("alien_tech", "adventure",
                Component.literal("НЛО"), Component.literal("Секрет."),
                new ItemStack(Items.ECHO_SHARD), 0, 0,
                List.of(), List.of("start"), true)); // Скрытая нода в другом табе!
    }

    private static void registerTab(ResearchTab tab) {
        TABS.put(tab.id(), tab);
        NODES_BY_TAB.put(tab.id(), new ArrayList<>());
    }

    private static void registerNode(ResearchNode node) {
        NODES.put(node.id(), node);
        if (NODES_BY_TAB.containsKey(node.tabId())) {
            NODES_BY_TAB.get(node.tabId()).add(node);
        }
    }

    // Удобные методы доступа для GUI и Сервера
    public static Collection<ResearchTab> getTabs() { return TABS.values(); }
    public static ResearchTab getTab(String id) { return TABS.get(id); }
    public static ResearchNode getNode(String id) { return NODES.get(id); }
    public static List<ResearchNode> getNodesForTab(String tabId) { return NODES_BY_TAB.getOrDefault(tabId, List.of()); }
}