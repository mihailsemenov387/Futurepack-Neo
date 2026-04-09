package net.futurepack.research;

import net.futurepack.client.gui.EScanner.Entrys.StudyPage;
import net.futurepack.client.gui.EScanner.Entrys.TextPage;
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
        registerTab(new ResearchTab("tech", Component.literal("Приключения"), new ItemStack(Items.IRON_SWORD)));
        registerTab(new ResearchTab("astronomy", Component.literal("Приключения"), new ItemStack(Items.IRON_SWORD)));
        registerTab(new ResearchTab("thermodynamics", Component.literal("Термодинамика"), new ItemStack(Items.NETHER_STAR)));

        // 2. Регистрируем ноды
//        registerNode(new ResearchNode("start", "space",
//                Component.literal("Основы"), Component.literal("Начало пути."),
//                new ItemStack(Items.COMPASS), 0, 0,
//                List.of(), List.of(), false));
//
//        registerNode(new ResearchNode("neon", "space",
//                Component.literal("Неон"), Component.literal("Энергия газов."),
//                new ItemStack(Items.BEACON), 0, -60,
//                List.of("start"), List.of("start"), false));
//
//        registerNode(new ResearchNode("laser_tech_I", "thermodynamics",
//                Component.literal("laser"), Component.literal("LASERS!!!."),
//                new ItemStack(Items.ECHO_SHARD), 0, 0,
//                List.of(), List.of("start", "neon"), false)); // Скрытая нода в другом табе!
//
//        registerNode(new ResearchNode("alien_tech", "adventure",
//                Component.literal("НЛО"), Component.literal("Секрет."),
//                new ItemStack(Items.ECHO_SHARD), 0, 0,
//                List.of(), List.of("start"), true)); // Скрытая нода в другом табе!
//
//        // 1. КОРЕНЬ (Виден сразу, доступен для изучения)
//        registerNode(new ResearchNode("start", "space",
//                Component.literal("Основы"), Component.literal("Нажми 'Изучить', чтобы начать."),
//                new ItemStack(Items.COMPASS), 0, 0,
//                List.of(), List.of(), // Нет родителей, нет требований
//                false));
//
//        // 2. СЛЕДУЮЩИЙ ШАГ (Будет LOCKED (серым), пока не изучишь "start")
//        registerNode(new ResearchNode("neon", "space",
//                Component.literal("Неон"), Component.literal("Теперь изучи это."),
//                new ItemStack(Items.BEACON), 0, -60,
//                List.of("start"),    // Линия от "start"
//                List.of("start"),    // ТРЕБОВАНИЕ изученного "start"
//                false));
//
//        // 3. СЕКРЕТ (Вообще не появится, пока не просканируешь блок железа)
//        registerNode(new ResearchNode("iron_tech", "space",
//                Component.literal("Железо"), Component.literal("Ты нашел это!"),
//                new ItemStack(Items.IRON_INGOT), 60, -60,
//                List.of("start"),
//                List.of("start"),
//                true)); // Скрыто по умолчанию!



        registerNode(new ResearchNode(
                "start", "space",
                Component.literal("Основы"),
                new ItemStack(Items.COMPASS), 0, 0,
                List.of(), List.of(), false,

                new TextPage("Добро пожаловать в Futurepack! Здесь начинается твой путь в космос.") // <-- Передали TextPage
        ));

// 2. Нода с картинкой и кнопкой "Изучить"
        registerNode(new ResearchNode(
                "neon", "space",
                Component.literal("Неон"),
                new ItemStack(Items.BEACON), 0, -60,
                List.of("start"), List.of("start"), false,

                new StudyPage("Изучите свойства неона.", "textures/gui/entries/neon_img.png") // <-- Передали StudyPage
        ));
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