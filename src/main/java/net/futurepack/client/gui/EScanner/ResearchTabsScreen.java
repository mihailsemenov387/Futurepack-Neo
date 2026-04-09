package net.futurepack.client.gui.EScanner;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import net.futurepack.client.gui.EScanner.ResearchNode.Status;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ResearchTabsScreen extends Screen {

    private static final ResourceLocation BG_TEXTURE = ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/escanner_bg.png");

    // Размеры корпуса (из нашего прошлого рабочего кода)
    private static final int IMG_W = 185;
    private static final int IMG_H = 235;

    // Внутренняя область (Экран)
    private static final int DISP_X_OFF = 51;
    private static final int DISP_Y_OFF = 48;
    private static final int DISP_W = 130;
    private static final int DISP_H = 173;

    private int leftPos, topPos;

    private Button studyButton;

    // Вкладки как в оригинале (но через Enum для удобства)
    public enum Tab {
        SPACE("Космос", Items.ENDER_EYE),
        ADVENTURE("Приключения", Items.IRON_SWORD),
        LOGS("Логи", Items.WRITABLE_BOOK);

        public final String title;
        public final ItemStack icon;
        Tab(String t, net.minecraft.world.item.Item i) { this.title = t; this.icon = new ItemStack(i); }
    }

    private Tab currentTab = Tab.SPACE;

    private static Set<String> COMPLETED = new HashSet<>();
    private static Set<String> REVEALED = new HashSet<>();
    private final Map<String, ResearchNode> researches = new HashMap<>();

    // Компоненты (то, что в оригинале GuiResearchTreePage)
    private final TreeRenderComponent treeComponent;
    private ResearchNode selectedEntry = null; // Если не null, рисуем текст вместо дерева

    public ResearchTabsScreen() {
        super(Component.literal("Research Overview"));
        this.treeComponent = new TreeRenderComponent(this);
    }

    public Map<String, ResearchNode> getResearches() {
        Map<String, ResearchNode> tabNodes = new HashMap<>();
        for(ResearchNode node : ResearchManager.getNodesForTab(currentTab.name())) {
            tabNodes.put(node.id(), node);
        }
        return tabNodes;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - IMG_W) / 2;
        this.topPos = (this.height - IMG_H) / 2;

        // В оригинале тут был запрос данных у сервера (LocalPlayerResearchHelper.requestServerData)
        // У нас синхронизация идет через пакеты автоматически.
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mX, int mY, float pT) {
        // 1. Фон (затемнение)
        g.fill(0, 0, this.width, this.height, 0x99000000);

        // 2. Корпус устройства
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BG_TEXTURE);
        g.blit(BG_TEXTURE, this.leftPos, this.topPos, 0, 0, IMG_W, IMG_H);

        // 3. Вкладки (как в drawTabs из оригинала)
        drawTabs(g, mX, mY);

        // 4. Содержимое экрана (Экран внутри корпуса)
        int x = this.leftPos + DISP_X_OFF;
        int y = this.topPos + DISP_Y_OFF;

        g.enableScissor(x, y, x + DISP_W, y + DISP_H);

        if (selectedEntry == null) {
            // Рисуем дерево текущей вкладки
            treeComponent.render(g, x, y, DISP_W, DISP_H, mX, mY, currentTab);
        } else {
            // Рисуем страницу текста (как GuiScannerPageResearch в оригинале)
            renderEntryPage(g, x, y);
        }

        g.disableScissor();
        super.render(g, mX, mY, pT);
    }

    private void drawTabs(GuiGraphics g, int mx, int my) {
        int x = this.leftPos - 25; // Слева от корпуса
        int y = this.topPos + 30;

        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            Tab tab = tabs[i];
            boolean active = (tab == currentTab);
            int ty = y + i * 28;
            int tx = active ? x + 4 : x; // Смещение активной вкладки

            // Рисуем базу вкладки
            int color = active ? 0xFF00E5FF : 0xFF333333;
            g.fill(tx, ty, tx + 25, ty + 25, 0xFF000000); // Тень
            g.fill(tx + 1, ty + 1, tx + 24, ty + 24, color);

            // Иконка
            g.renderItem(tab.icon, tx + 4, ty + 4);

            // ФИШКА ИЗ ОРИГИНАЛА: Красная точка "Не прочитано"
            if (hasUnreadInTab(tab)) {
                drawNotification(g, tx + 18, ty + 4);
            }

            // Тултип названия вкладки
            if (mx >= tx && mx <= tx + 25 && my >= ty && my <= ty + 25) {
                g.renderTooltip(this.font, Component.literal(tab.title), mx, my);
            }
        }
    }

    private void drawNotification(GuiGraphics g, int x, int y) {
        // Та самая синусоидальная анимация мерцания из твоего кода
        float pulse = (float) (Math.sin(System.currentTimeMillis() / 100.0) * 0.5 + 0.5);
        int alpha = (int)(150 + (105 * pulse));
        int color = (alpha << 24) | 0xFF0050; // Красный FP-стиль
        g.fill(x, y, x + 4, y + 4, color);
    }

    private boolean hasUnreadInTab(Tab tab) {
        // Тут будет логика проверки: есть ли новые REVEALED, но не COMPLETED
        return false;
    }

//    private void renderEntryPage(GuiGraphics g, int x, int y) {
//        g.fill(x, y, x + DISP_W, y + DISP_H, 0xFF000508); // Темный фон записи
//        g.drawCenteredString(this.font, selectedEntry.title(), x + DISP_W / 2, y + 10, 0x00FFCC);
//        g.drawWordWrap(this.font, selectedEntry.description(), x + 10, y + 30, DISP_W - 20, 0xFFFFFF);
//
//        // Кнопка назад внутри экрана (кодом)
//        g.drawString(this.font, "< Назад", x + 5, y + DISP_H - 12, 0x55FFFF);
//    }


    // Отрисовка страницы (Текст / Картинка)
    private void renderEntryPage(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + DISP_W, y + DISP_H, 0xFF000508);
        g.drawCenteredString(this.font, selectedEntry.title(), x + DISP_W / 2, y + 10, 0x00FFCC);

        if (selectedEntry.pageType() == ResearchNode.PageType.IMAGE) {
            // Рисуем картинку (путь: assets/futurepack/textures/gui/entries/ID.png)
            ResourceLocation img = ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/entries/" + selectedEntry.id() + ".png");
            g.blit(img, x + (DISP_W - 64) / 2, y + 25, 0, 0, 64, 64, 64, 64);
            g.drawWordWrap(this.font, selectedEntry.description(), x + 10, y + 95, DISP_W - 20, 0xFFFFFF);
        } else {
            g.drawWordWrap(this.font, selectedEntry.description(), x + 10, y + 30, DISP_W - 20, 0xFFFFFF);
        }

        // ЛОГИКА КНОПКИ
        updateStudyButton();
    }

    public void openEntry(ResearchNode node) {
        this.selectedEntry = node;
    }



    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        // 1. Клик по табам
        int x = this.leftPos - 25;
        int y = this.topPos + 30;
        for (int i = 0; i < Tab.values().length; i++) {
            int ty = y + i * 28;
            if (mx >= x && mx <= x + 25 && my >= ty && my <= ty + 25) {
                this.currentTab = Tab.values()[i];
                this.selectedEntry = null; // Сброс при смене вкладки
                return true;
            }
        }

        // 2. Клик внутри страницы (кнопка назад)
        if (selectedEntry != null) {
            if (mx >= leftPos + DISP_X_OFF && my >= topPos + DISP_Y_OFF + DISP_H - 15) {
                selectedEntry = null;
                return true;
            }
        }

        // 3. Проброс в дерево
        if (selectedEntry == null) {
            return treeComponent.mouseClicked(mx, my, btn, leftPos + DISP_X_OFF, topPos + DISP_Y_OFF, DISP_W, DISP_H);
        }

        return super.mouseClicked(mx, my, btn);
    }





    // Умная проверка статуса (с учетом requirements)
    public Status getNodeStatus(ResearchNode node) {
        if (COMPLETED.contains(node.id())) return Status.COMPLETED;
        if (node.isHidden() && !REVEALED.contains(node.id())) return Status.HIDDEN;

        // Проверяем требования (Requirements - могут быть из ЛЮБОЙ вкладки)
        boolean reqsMet = node.requirements().isEmpty() ||
                node.requirements().stream().allMatch(COMPLETED::contains);

        return reqsMet ? Status.AVAILABLE : Status.LOCKED;
    }



    private void updateStudyButton() {
        Status status = getNodeStatus(selectedEntry);
        if (status == Status.AVAILABLE) {
            this.studyButton.visible = true;
            // Меняем текст кнопки в зависимости от типа изучения
            Component label = switch(selectedEntry.unlockType()) {
                case ACCEPT -> Component.literal("Принять");
                case MACHINE -> Component.literal("Изучить");
                case INSTANT -> Component.literal("Открыть");
            };
            this.studyButton.setMessage(label);
        } else {
            this.studyButton.visible = false;
        }
    }








    @Override public void renderBackground(GuiGraphics g, int mx, int my, float pt) {}
    @Override public boolean isPauseScreen() { return false; }
}