package net.futurepack.research;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.*;

public class ResearchManager {
    private static final Map<String, ResearchTab> TABS = new LinkedHashMap<>();
    private static final Map<String, ResearchNode> NODES = new HashMap<>();
    private static final Map<String, List<ResearchNode>> NODES_BY_TAB = new HashMap<>();

    public static void init() {
        TABS.clear(); NODES.clear(); NODES_BY_TAB.clear();

        // 1. ТАБЫ
        registerTab(new ResearchTab("basics", Component.literal("Основы"), new ItemStack(Items.BOOK),
                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png"),
                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png")));
        // ... зарегистрируй остальные табы (engineering, energy и т.д.) здесь ...

        // 2. НОДЫ (Без страниц!)
        add("start", "basics", "Основы", Items.COMPASS, 0, 0, List.of(), List.of(), false, ResearchNode.NodeFrameType.ERK);
        add("ufo", "basics", "Неон", Items.BEACON, 30, -60, List.of("start"), List.of("start"), false, ResearchNode.NodeFrameType.HEXAGON);
        add("iron_tech", "basics", "Железо", Items.IRON_INGOT, 60, -60, List.of("start"), List.of("start"), true, ResearchNode.NodeFrameType.HEXAGON);

        // ... твои остальные ноды ...
    }

    private static void add(String id, String tab, String name, net.minecraft.world.item.Item icon, int x, int y, List<String> links, List<String> reqs, boolean hidden, ResearchNode.NodeFrameType frame) {
        registerNode(new ResearchNode(id, tab, Component.literal(name), new ItemStack(icon), null, x, y, links, reqs, hidden, frame));
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

    public static Collection<ResearchTab> getTabs() { return TABS.values(); }
    public static ResearchTab getTab(String id) { return TABS.get(id); }
    public static ResearchNode getNode(String id) { return NODES.get(id); }
    public static List<ResearchNode> getNodesForTab(String tabId) { return NODES_BY_TAB.getOrDefault(tabId, List.of()); }
    public static Set<String> getAllIds() { return NODES.keySet(); }
}