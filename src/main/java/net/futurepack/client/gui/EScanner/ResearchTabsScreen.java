package net.futurepack.client.gui.EScanner;

import com.mojang.blaze3d.systems.RenderSystem;
import net.futurepack.research.ClientResearchState;
import net.futurepack.research.ResearchManager;
import net.futurepack.research.ResearchNode;
import net.futurepack.research.ResearchNode.Status;
import net.futurepack.research.ResearchTab;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ResearchTabsScreen extends Screen {

    private static final ResourceLocation BG_TEXTURE = ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/escanner_bg.png");

    private static final int IMG_W = 185;
    private static final int IMG_H = 235;

    private static final int DISP_X_OFF = 51;
    private static final int DISP_Y_OFF = 48;
    private static final int DISP_W = 130;
    private static final int DISP_H = 173;

    private int leftPos, topPos;

    // Текущая выбранная вкладка (объект)
    private ResearchTab currentTab;

    // Компонент отрисовки дерева
    private final TreeRenderComponent treeComponent;

    // Выбранная запись (для режима чтения текста)
    private ResearchNode selectedEntry = null;

    public ResearchTabsScreen() {
        super(Component.literal("Research Overview"));
        this.treeComponent = new TreeRenderComponent(this);

        // Устанавливаем начальную вкладку (первую из существующих)
        var allTabs = ResearchManager.getTabs();
        if (!allTabs.isEmpty()) {
            this.currentTab = allTabs.iterator().next();
        }
    }

    // Метод для TreeRenderComponent, чтобы он знал, какие ноды рисовать
    public Map<String, ResearchNode> getResearches() {
        Map<String, ResearchNode> tabNodes = new HashMap<>();
        if (currentTab != null) {
            for (ResearchNode node : ResearchManager.getNodesForTab(currentTab.id())) {
                tabNodes.put(node.id(), node);
            }
        }
        return tabNodes;
    }

    // Прослойка для получения статуса из глобального стейта
    public Status getNodeStatus(ResearchNode node) {
        return ClientResearchState.getStatus(node);
    }

    public String getCurrentTabId() {
        return this.currentTab != null ? this.currentTab.id() : "";
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - IMG_W) / 2;
        this.topPos = (this.height - IMG_H) / 2;
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mX, int mY, float pT) {
        // 1. Фон
        g.fill(0, 0, this.width, this.height, 0x99000000);

        // 2. Корпус
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BG_TEXTURE);
        g.blit(BG_TEXTURE, this.leftPos, this.topPos, 0, 0, IMG_W, IMG_H);

        // 3. Вкладки
        drawTabs(g, mX, mY);

        // 4. Экран
        int x = this.leftPos + DISP_X_OFF;
        int y = this.topPos + DISP_Y_OFF;

        g.enableScissor(x, y, x + DISP_W, y + DISP_H);

        if (selectedEntry == null) {
            if (currentTab != null) {
                treeComponent.render(g, x, y, DISP_W, DISP_H, mX, mY, currentTab);
            }
        } else {
            renderEntryPage(g, x, y);
        }

        g.disableScissor();
        super.render(g, mX, mY, pT);
    }

    private void drawTabs(GuiGraphics g, int mx, int my) {
        int x = this.leftPos - 25;
        int y = this.topPos + 30;

        int i = 0;
        for (ResearchTab tab : ResearchManager.getTabs()) {
            boolean active = (tab == currentTab);
            int ty = y + i * 28;
            int tx = active ? x + 4 : x;

            int color = active ? 0xFF00E5FF : 0xFF333333;
            g.fill(tx, ty, tx + 25, ty + 25, 0xFF000000);
            g.fill(tx + 1, ty + 1, tx + 24, ty + 24, color);

            g.renderItem(tab.icon(), tx + 4, ty + 4);

            if (mx >= tx && mx <= tx + 25 && my >= ty && my <= ty + 25) {
                g.renderTooltip(this.font, tab.title(), mx, my);
            }
            i++;
        }
    }

    private void renderEntryPage(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + DISP_W, y + DISP_H, 0xFF000508);
        g.drawCenteredString(this.font, selectedEntry.title(), x + DISP_W / 2, y + 10, 0x00FFCC);
        g.drawWordWrap(this.font, selectedEntry.description(), x + 10, y + 30, DISP_W - 20, 0xFFFFFF);
        g.drawString(this.font, "< Назад", x + 5, y + DISP_H - 12, 0x55FFFF);
    }

    public void openEntry(ResearchNode node) {
        this.selectedEntry = node;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (selectedEntry == null && btn == 0) {
            // Клик по табам
            int x = this.leftPos - 25;
            int y = this.topPos + 30;

            int i = 0;
            for (ResearchTab tab : ResearchManager.getTabs()) {
                int ty = y + i * 28;
                if (mx >= x && mx <= x + 25 && my >= ty && my <= ty + 25) {
                    this.currentTab = tab;
                    return true;
                }
                i++;
            }

            // Клик по дереву
            if (treeComponent.mouseClicked(mx, my, btn, leftPos + DISP_X_OFF, topPos + DISP_Y_OFF, DISP_W, DISP_H)) {
                return true;
            }
        }

        // Клик "Назад" в режиме чтения
        if (selectedEntry != null && btn == 0) {
            int x = this.leftPos + DISP_X_OFF;
            int y = this.topPos + DISP_Y_OFF;
            if (mx >= x && mx <= x + 40 && my >= y + DISP_H - 15) {
                selectedEntry = null;
                return true;
            }
        }

        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (selectedEntry == null) {
            return treeComponent.mouseDragged(dx, dy, btn);
        }
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