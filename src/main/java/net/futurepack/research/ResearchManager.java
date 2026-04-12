package net.futurepack.research;

import net.futurepack.client.gui.EScanner.Entrys.StudyPage;
import net.futurepack.client.gui.EScanner.Entrys.TextPage;
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

        // ==========================================
        // 1. РЕГИСТРАЦИЯ ВКЛАДОК (8 видимых для теста скролла, 1 скрытая)
        // ==========================================
        registerTab(new ResearchTab("basics", Component.literal("Основы"), new ItemStack(Items.BOOK),
                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png"),
                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png")));

        registerTab(new ResearchTab("engineering", Component.literal("Инженерия"), new ItemStack(Items.IRON_PICKAXE),
                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png"),
                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png")));

        registerTab(new ResearchTab("energy", Component.literal("Энергетика"), new ItemStack(Items.REDSTONE),
                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png"),
                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png")));

        registerTab(new ResearchTab("chemistry", Component.literal("Химия"), new ItemStack(Items.POTION),
                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png"),
                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png")));

        registerTab(new ResearchTab("weapons", Component.literal("Оружейная"), new ItemStack(Items.DIAMOND_SWORD),
                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png"),
                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png")));

        registerTab(new ResearchTab("logistics", Component.literal("Логистика"), new ItemStack(Items.MINECART),
                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png"),
                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png")));

        registerTab(new ResearchTab("biology", Component.literal("Биология"), new ItemStack(Items.APPLE),
                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png"),
                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png")));

        registerTab(new ResearchTab("space", Component.literal("Космос"), new ItemStack(Items.ENDER_EYE),
                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png"),
                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png")));

        // Эта вкладка будет невидимой, пока мы не откроем в ней хотя бы одну ноду
        registerTab(new ResearchTab("alien", Component.literal("Аномалии"), new ItemStack(Items.ECHO_SHARD),
                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_bg.png"),
                ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/slot_bg.png")));

        // ==========================================
        // 2. НОДЫ ДЛЯ ВКЛАДКИ: ОСНОВЫ (Тест простого дерева)
        // ==========================================
        registerNode(new ResearchNode("b_start", "basics",
                Component.literal("Пробуждение"),
                new ItemStack(Items.CLOCK), null, 0, 0,
                List.of(), List.of(), false,
                new TextPage("Тест 1: Базовая нода. Должна быть белой (AVAILABLE) сразу."),
                ResearchNode.NodeFrameType.GOLDEN));

        registerNode(new ResearchNode("b_tools", "basics",
                Component.literal("Инструменты"),
                new ItemStack(Items.CRAFTING_TABLE), null, 0, -60,
                List.of("b_start"), List.of("b_start"), false,
                new TextPage("Тест 2: Откроется после Пробуждения."),
                ResearchNode.NodeFrameType.GOLDEN));


        // ==========================================
        // 3. НОДЫ ДЛЯ ВКЛАДКИ: ИНЖЕНЕРИЯ (Тест Кросс-зависимости)
        // ==========================================
        registerNode(new ResearchNode("e_gears", "engineering",
                Component.literal("Шестеренки"),
                new ItemStack(Items.COPPER_INGOT), null, 0, 0,
                List.of(), List.of(), false,
                new TextPage("Это база инженерии. Доступна сразу."),
                ResearchNode.NodeFrameType.HEXAGON));

        registerNode(new ResearchNode("e_motor", "engineering",
                Component.literal("Мотор"),
                new ItemStack(Items.MINECART), null, 0, -60,
                List.of("e_gears"), // Визуальная линия идет ТОЛЬКО от шестеренок
                List.of("e_gears", "b_tools"), // ЛОГИКА: Требует Инструменты из вкладки "Основы"!
                false,
                new StudyPage("Тест 3: Кросс-таб зависимость. Если Инструменты не изучены, эта нода будет серой, даже если Шестеренки изучены.", "textures/gui/entries/motor.png"),
                ResearchNode.NodeFrameType.HEXAGON));


        // ==========================================
        // 4. НОДЫ ДЛЯ ВКЛАДКИ: АНОМАЛИИ (Тест Скрытности)
        // ==========================================
        registerNode(new ResearchNode("a_secret", "alien",
                Component.literal("Неизвестный сигнал"),
                new ItemStack(Items.AMETHYST_SHARD), null, 0, 0,
                List.of(), List.of(),
                true, // СКРЫТО ПО УМОЛЧАНИЮ (Вся вкладка исчезнет)
                new TextPage("Тест 4: Скрытая нода. Появляется только после сканирования."),
                ResearchNode.NodeFrameType.GOLDEN));


        // (Для остальных вкладок просто добавим пустышки, чтобы вкладки отобразились и скролл работал)
        addDummy("energy"); addDummy("chemistry"); addDummy("weapons");
        addDummy("logistics"); addDummy("biology"); addDummy("space");
    }

    private static void addDummy(String tabId) {
        registerNode(new ResearchNode("dummy_" + tabId, tabId, Component.literal("Пустышка " + tabId), new ItemStack(Items.DIRT), null, 0, 0, List.of(), List.of(), false, new TextPage("Текст"), ResearchNode.NodeFrameType.GOLDEN));
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