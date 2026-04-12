package net.futurepack.client.gui.EScanner;

import com.mojang.blaze3d.systems.RenderSystem;
import net.futurepack.network.RequestCompletePayload;
import net.futurepack.network.RequestReadPayload;
import net.futurepack.research.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResearchTabsScreen extends Screen {

    private static final ResourceLocation FRAME_TEXTURE = ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_overlay.png");
    private static final ResourceLocation TAB_BG = ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/slot_bg.png");
    private static final int IMG_W = 256;
    private static final int IMG_H = 256;

    private static final int DISP_X_OFF = 16;
    private static final int DISP_Y_OFF = 16;
    private static final int DISP_W = 224;
    private static final int DISP_H = 224;

    private int leftPos, topPos;
    private ResearchTab currentTab;
    private final TreeRenderComponent treeComponent;
    private ResearchNode selectedEntry = null;


    public ResearchTabsScreen(EScannerMainScreen mainScreen) {
        super(Component.literal("Research Overview"));
        this.treeComponent = new TreeRenderComponent(this);


//        var allTabs = ResearchManager.getTabs();
//        if (!allTabs.isEmpty()) {
//            this.currentTab = allTabs.iterator().next();
//        }
        this.currentTab = ResearchManager.getTabs().stream()
                .filter(tab -> ClientResearchState.isTabVisible(tab.id()))
                .findFirst()
                .orElse(null);
    }


    public String getCurrentTabId() {
        return this.currentTab.id();

//        var tabs = ResearchManager.getTabs();
//        TODO: fix if empty
//        if (tabs.isEmpty()) return "space";

//        return tabs.iterator().next().id();

    }

    public void openEntry(ResearchNode node) {
        this.selectedEntry = node;
        PacketDistributor.sendToServer(new RequestReadPayload(node.id()));
        if (node.page() != null) {
            node.page().init(this, node, this.leftPos + DISP_X_OFF, this.topPos + DISP_Y_OFF, DISP_W, DISP_H);
        }
    }


    private void closeEntry() {
        if (this.selectedEntry != null && this.selectedEntry.page() != null) {
            this.selectedEntry.page().onClose(this);
        }
        this.selectedEntry = null; // Теперь removed() ничего не сделает, так как тут null
    }

    @Override
    public void removed() {
        if (selectedEntry != null && selectedEntry.page() != null) {
            selectedEntry.page().onClose(this);
        }
        super.removed();
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - IMG_W) / 2;
        this.topPos = (this.height - IMG_H) / 2;

        if (selectedEntry != null && selectedEntry.page() != null) {
            // Проверяем, что screen (this) не null
            selectedEntry.page().init(this, selectedEntry, this.leftPos + DISP_X_OFF, this.topPos + DISP_Y_OFF, DISP_W, DISP_H);
        }
    }

    public <T extends net.minecraft.client.gui.components.events.GuiEventListener & net.minecraft.client.gui.components.Renderable & net.minecraft.client.gui.narration.NarratableEntry> T addWidgetPublic(T widget) {
        return this.addRenderableWidget(widget);
    }

    public void removeWidgetPublic(net.minecraft.client.gui.components.events.GuiEventListener widget) {
        this.removeWidget(widget);
    }


    @Override
    public void render(@NotNull GuiGraphics g, int mX, int mY, float pT) {
        // 1. Фон
        g.fill(0, 0, this.width, this.height, 0x99000000);

        // 2. Рассчитываем позиции СТРОГО целыми числами (Фикс черного пикселя)
        int iLeft = this.leftPos;
        int iTop = this.topPos;

        // 3. Рисуем дерево (Оно теперь само ограничивает скролл)
        int x = iLeft + DISP_X_OFF;
        int y = iTop + DISP_Y_OFF;

        // Важно: Scissor и Fill должны иметь одинаковые координаты
        g.enableScissor(x, y, x + DISP_W, y + DISP_H);
        if (selectedEntry == null) {
            treeComponent.render(g, x, y, DISP_W, DISP_H, mX, mY, currentTab);
        } else {
            renderEntryPage(g, x, y, mX, mY, pT);
        }
        g.disableScissor();

        // 4. Рисуем ОВЕРЛЕЙ ПОВЕРХ дерева
        RenderSystem.enableBlend();
        g.blit(FRAME_TEXTURE, iLeft, iTop, 0, 0, 256, 256, 256, 256);

        // 5. ТУЛТИПЫ (Рисуем в самом конце, вне Scissor и Pose дерева)
        if (selectedEntry == null && treeComponent.getHoveredNode() != null) {
            var node = treeComponent.getHoveredNode();
            var status = ClientResearchState.getStatus(node);
            // Используем ванильный метод отрисовки тултипа
           treeComponent.drawTooltip(g, mX, mY);
//            g.renderTooltip(this.font, List.of(
//                    node.title().getVisualOrderText(),
//                    Component.literal("Статус: " + status).withStyle(net.minecraft.ChatFormatting.GRAY).getVisualOrderText()
//            ), mX, mY);
        }

        renderTabButtons(g);
        renderTabTooltips(g, mX, mY);
        super.render(g, mX, mY, pT);
    }
//    private void renderTabButtons(GuiGraphics g) {
//        int x = this.leftPos - 24; // Пододвинул ближе
//        int y = this.topPos + 30;
//        int i = 0;
//        for (ResearchTab tab : ResearchManager.getTabs()) {
//            if (!ClientResearchState.isTabVisible(tab.id())) continue;
//            boolean active = (tab == currentTab);
//            int ty = y + i * 28;
//            int tx = active ? x + 4 : x;
//
//            int color = active ? 0xFF00E5FF : 0xFF333333;
//            g.fill(tx, ty, tx + 25, ty + 25, 0xFF000000);
//            g.fill(tx + 1, ty + 1, tx + 24, ty + 24, color);
//            g.renderItem(tab.icon(), tx + 4, ty + 4);
//            i++;
//        }
//    }

    private void renderTabButtons(GuiGraphics g) {
        int x = this.leftPos - 24; // Позиция по X (чуть левее корпуса)
        int y = this.topPos + 30;  // Начальная позиция по Y
        int i = 0;

        for (ResearchTab tab : ResearchManager.getTabs()) {
            if (!ClientResearchState.isTabVisible(tab.id())) continue;

            boolean active = (tab.equals(currentTab));
            int ty = y + i * 28;
            int tx = active ? x + 4 : x; // Активная вкладка "выезжает" вперед

            if (active) {
                // Активная вкладка - яркая и золотая
                g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            } else {
                // Неактивная вкладка - приглушенная (серая)
                g.setColor(0.8f, 0.8f, 0.8f, 1.0f);
            }

            // Рисуем рамку slot_bg (размер 26x26)
            // Параметры: текстура, x, y, u, v, ширина, высота, ширина_файла, высота_файла
            // Убедись, что последние два числа соответствуют реальному размеру твоего PNG (например 18, 18)
            g.blit(TAB_BG, tx, ty, 0, 0, 26, 26, 26, 26);

            g.setColor(1.0f, 1.0f, 1.0f, 1.0f);

            g.renderItem(tab.icon(), tx + 5, ty + 5);

            i++;
        }
    }



    private void renderTabTooltips(GuiGraphics g, int mx, int my) {
        int x = this.leftPos - 24;
        int y = this.topPos + 30;
        int i = 0;
        for (ResearchTab tab : ResearchManager.getTabs()) {
            if (!ClientResearchState.isTabVisible(tab.id())) continue;
            boolean active = (tab == currentTab);
            int ty = y + i * 28;
            int tx = active ? x + 4 : x;

            if (mx >= tx && mx <= tx + 25 && my >= ty && my <= ty + 25) {
                g.renderTooltip(this.font, tab.title(), mx, my);
            }
            i++;
        }
    }

//    private void renderEntryPage(GuiGraphics g, int x, int y, int mX, int mY, float pT)  {
//        g.drawCenteredString(this.font, selectedEntry.title(), x + DISP_W / 2, y + 10, 0x00FFCC);
//
//        // ЗАМЕНИ ВЕСЬ ТЕКСТ НА ЭТУ СТРОКУ:
//        if (selectedEntry.page() != null) {
//            selectedEntry.page().render(g, mX, mY, pT);
//        }
//
//        g.drawString(this.font, "< Назад", x + 5, y + DISP_H - 12, 0x55FFFF);
//
//
//    }

    private void renderEntryPage(GuiGraphics g, int x, int y, int mX, int mY, float pT) {
        // Спрашиваем заголовок у СТРАНИЦЫ, а не у ноды напрямую

        if (selectedEntry.page() != null) {
            selectedEntry.page().render(g, mX, mY, pT);
        }
        g.drawString(this.font, "< Назад", x + 5, y + DISP_H - 12, 0x55FFFF);
    }


    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        // 1. ПРАВАЯ КНОПКА: Всегда работает как "Назад"



        if (btn == 1) {
            handleBackAction();
            return true;
        }



        // Дальше обрабатываем только ЛЕВУЮ КНОПКУ (0)
        if (btn != 0) return super.mouseClicked(mx, my, btn);

        // 2. ЕСЛИ ОТКРЫТА ЗАПИСЬ
        if (selectedEntry != null) {
            return handleEntryClick(mx, my, btn);
        }

        // 3. ЕСЛИ МЫ В ДЕРЕВЕ
        return handleTreeClick(mx, my, btn);
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ ОБРАБОТЧИКИ ---

    private void handleBackAction() {
        if (selectedEntry != null) {
            closeEntry();
        } else {
            net.minecraft.client.Minecraft.getInstance().setScreen(new net.futurepack.client.gui.EScanner.EScannerMainScreen());
        }
    }

    private boolean handleEntryClick(double mx, double my, int btn) {
        int x = this.leftPos + DISP_X_OFF;
        int y = this.topPos + DISP_Y_OFF;

        // Клик по кнопке "Назад" в тексте
        if (mx >= x && mx <= x + 45 && my >= y + DISP_H - 15) {
            closeEntry();
            return true;
        }

        // Позволяем работать кнопке "Изучить" (studyButton)
        return super.mouseClicked(mx, my, btn);
    }

    private boolean handleTreeClick(double mx, double my, int btn) {
        // Проверка вкладок
        if (handleTabClick(mx, my)) return true;

        // Проверка нод в дереве
        return treeComponent.mouseClicked(mx, my, btn, leftPos + DISP_X_OFF, topPos + DISP_Y_OFF, DISP_W, DISP_H);
    }

    private boolean handleTabClick(double mx, double my) {
        int x = this.leftPos - 24;
        int y = this.topPos + 30;
        int i = 0;

        for (net.futurepack.research.ResearchTab tab : net.futurepack.research.ResearchManager.getTabs()) {
            if (!ClientResearchState.isTabVisible(tab.id())) continue;
            int ty = y + i * 28;
            int tx = (tab.equals(currentTab)) ? x + 4 : x;

            if (mx >= tx && mx <= tx + 26 && my >= ty && my <= ty + 26) {
                this.currentTab = tab;
                return true;
            }
            i++;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (selectedEntry == null) return treeComponent.mouseDragged(dx, dy, btn);
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (selectedEntry == null) {
            treeComponent.mouseScrolled(sy);
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    @Override public void renderBackground(GuiGraphics g, int mx, int my, float pt) {}
    @Override public boolean isPauseScreen() { return false; }
}