package net.futurepack.client.gui.EScanner;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.*;

public class ResearchManager {
    private static final Map<String, ResearchNode> ALL_NODES = new HashMap<>();
    private static final Map<String, List<ResearchNode>> BY_TAB = new HashMap<>();

    public static void init() {
        ALL_NODES.clear();
        BY_TAB.clear();

        // ВКЛАДКА: КОСМОС
        add(new ResearchNode("start", "SPACE", Component.literal("Основы"), Component.literal("Ваш путь в космос."),
                new ItemStack(Items.COMPASS), 0, 0, List.of(), List.of(), false,
                ResearchNode.UnlockType.ACCEPT, ResearchNode.PageType.TEXT));

        add(new ResearchNode("neon", "SPACE", Component.literal("Неон"), Component.literal("Энергия газов."),
                new ItemStack(Items.BEACON), 0, -60, List.of("start"), List.of("start"), false,
                ResearchNode.UnlockType.MACHINE, ResearchNode.PageType.IMAGE));

        // ВКЛАДКА: ПРИКЛЮЧЕНИЯ (Скрытая нода)
        add(new ResearchNode("ancient_ruins", "ADVENTURE", Component.literal("Руины"), Component.literal("Тайны прошлого."),
                new ItemStack(Items.RECOVERY_COMPASS), 0, 0, List.of(), List.of("start"), true,
                ResearchNode.UnlockType.INSTANT, ResearchNode.PageType.TEXT));
    }

    private static void add(ResearchNode node) {
        ALL_NODES.put(node.id(), node);
        BY_TAB.computeIfAbsent(node.tabId(), k -> new ArrayList<>()).add(node);
    }

    public static List<ResearchNode> getNodesForTab(String tabId) {
        return BY_TAB.getOrDefault(tabId, List.of());
    }

    public static ResearchNode get(String id) { return ALL_NODES.get(id); }
}