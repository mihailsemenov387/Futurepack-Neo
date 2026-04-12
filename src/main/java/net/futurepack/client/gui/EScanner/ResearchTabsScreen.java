package net.futurepack.client.gui.EScanner;

import com.mojang.blaze3d.systems.RenderSystem;
import net.futurepack.network.RequestReadPayload;
import net.futurepack.research.*;
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

public class ResearchTabsScreen extends Screen implements IScannerScreen {

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
    private final EScannerMainScreen parent;


    private int tabScrollOffset = 0;
    private Button btnScrollUp;
    private Button btnScrollDown;


    public ResearchTabsScreen(EScannerMainScreen mainScreen) {
        super(Component.literal("Research Overview"));
        this.treeComponent = new TreeRenderComponent(this);
        this.parent = mainScreen;

        // При открытии выбираем первую видимую вкладку
        this.currentTab = getVisibleTabs().stream().findFirst().orElse(null);
    }

    public String getCurrentTabId() {
        return this.currentTab != null ? this.currentTab.id() : "";
    }

    // Вспомогательный метод для получения ТОЛЬКО видимых вкладок
    private List<ResearchTab> getVisibleTabs() {
        return ResearchManager.getTabs().stream()
                .filter(tab -> ClientResearchState.isTabVisible(tab.id()))
                .toList();
    }

    public void openEntry(ResearchNode node) {
        PacketDistributor.sendToServer(new RequestReadPayload(node.id()));
        this.minecraft.setScreen(new EScannerReadScreen(node, this));
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - IMG_W) / 2;
        this.topPos = (this.height - IMG_H) / 2;

        this.btnScrollUp = Button.builder(Component.literal("▲"), b -> {
            if (tabScrollOffset > 0) tabScrollOffset--;
        }).bounds(this.leftPos - 24, this.topPos + 10, 24, 15).build();

        // Кнопка ВНИЗ
        this.btnScrollDown = Button.builder(Component.literal("▼"), b -> {
            int maxOffset = Math.max(0, getVisibleTabs().size() - 7);
            if (tabScrollOffset < maxOffset) tabScrollOffset++;
        }).bounds(this.leftPos - 24, this.topPos + 225, 24, 15).build();

        this.addRenderableWidget(this.btnScrollUp);
        this.addRenderableWidget(this.btnScrollDown);
    }

    @Override
    public <T extends net.minecraft.client.gui.components.events.GuiEventListener & net.minecraft.client.gui.components.Renderable & net.minecraft.client.gui.narration.NarratableEntry> T addWidgetPublic(T widget) {
        return this.addRenderableWidget(widget);
    }

    @Override
    public void removeWidgetPublic(net.minecraft.client.gui.components.events.GuiEventListener widget) {
        this.removeWidget(widget);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mX, int mY, float pT) {
        g.fill(0, 0, this.width, this.height, 0x99000000);

        int iLeft = this.leftPos;
        int iTop = this.topPos;

        // Рендер дерева
        int x = iLeft + DISP_X_OFF;
        int y = iTop + DISP_Y_OFF;

        g.enableScissor(x, y, x + DISP_W, y + DISP_H);
        if (currentTab != null) {
            treeComponent.render(g, x, y, DISP_W, DISP_H, mX, mY, currentTab);
        }
        g.disableScissor();

        // Оверлей (Рамка)
        RenderSystem.enableBlend();
        g.blit(FRAME_TEXTURE, iLeft, iTop, 0, 0, 256, 256, 256, 256);

        // Тултип ноды (вне Scissor)
        treeComponent.drawTooltip(g, mX, mY);

        RenderButtons();
        renderTabButtons(g);
        renderTabTooltips(g, mX, mY);
        super.render(g, mX, mY, pT);
    }

    private void RenderButtons(){
        int visibleTabsCount = getVisibleTabs().size();
        boolean needsScroll = visibleTabsCount > 7;

        this.btnScrollUp.visible = needsScroll;
        this.btnScrollDown.visible = needsScroll;

        if (needsScroll) {
            int maxOffset = Math.max(0, visibleTabsCount - 7);
            this.btnScrollUp.active = (tabScrollOffset > 0);
            this.btnScrollDown.active = (tabScrollOffset < maxOffset);
        }

    }

    // Одинаковая логика скролла для кнопок, кликов и тултипов!
    private void renderTabButtons(GuiGraphics g) {
        int x = this.leftPos - 24;
        int y = this.topPos + 30;

        List<ResearchTab> visibleTabs = getVisibleTabs();

        // Рисуем не больше 7 штук
        for (int i = 0; i < 7; i++) {
            int index = i + tabScrollOffset;
            if (index >= visibleTabs.size()) break;

            ResearchTab tab = visibleTabs.get(index);
            boolean active = currentTab != null && tab.id().equals(currentTab.id());

            int ty = y + i * 26; // Индекс экрана (i), а не индекс коллекции
            int tx = active ? x + 3 : x;

            g.setColor(active ? 1.0f : 0.7f, active ? 1.0f : 0.7f, active ? 1.0f : 0.7f, 1.0f);
            g.blit(TAB_BG, tx, ty, 0, 0, 24, 24, 24, 24);
            g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            if(tab.icon() != null) {
                g.renderItem(tab.icon(), tx + 4, ty + 4);
            }
            else{
                g.blit(tab.custom_icon(), tx, ty, 0, 0, 24, 24, 24, 24);

            }
            if (ClientResearchState.hasUnreadInTab(tab.id())) {
                renderTabNotification(g, tx + 18, ty + 2);
            }

        }
    }

    private void renderTabTooltips(GuiGraphics g, int mx, int my) {
        int x = this.leftPos - 24;
        int y = this.topPos + 30;

        List<ResearchTab> visibleTabs = getVisibleTabs();

        for (int i = 0; i < 7; i++) {
            int index = i + tabScrollOffset;
            if (index >= visibleTabs.size()) break;

            ResearchTab tab = visibleTabs.get(index);
            boolean active = currentTab != null && tab.id().equals(currentTab.id());

            int ty = y + i * 26;
            int tx = active ? x + 3 : x;

            if (mx >= tx && mx <= tx + 24 && my >= ty && my <= ty + 24) {
                g.renderTooltip(this.font, tab.title(), mx, my);
            }
        }
    }

    private void renderTabNotification(GuiGraphics g, int x, int y) {
        float wave = (float) (Math.sin(System.currentTimeMillis() / 150.0) * 0.5 + 0.5);
        int alpha = (int)(160 + (95 * wave));
        int redColor = (alpha << 24) | 0xFF0000;

        g.pose().pushPose();
        g.pose().translate(0, 0, 300); // Чтобы диод был поверх иконки вкладки

        // Рисуем маленькую лампочку (4x4 пикселя) с черной обводкой
        g.fill(x, y, x + 5, y + 5, 0xFF000000);
        g.fill(x + 1, y + 1, x + 4, y + 4, redColor);

        g.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        // ПКМ - Выход в главное меню
        if (btn == 1) {
            net.minecraft.client.Minecraft.getInstance().setScreen(this.parent);
            return true;
        }

        if (btn == 0) {
            // Проверка клика по табам (ТОЧНАЯ копия логики отрисовки)
            int x = this.leftPos - 24;
            int y = this.topPos + 30;

            List<ResearchTab> visibleTabs = getVisibleTabs();

            for (int i = 0; i < 7; i++) {
                int index = i + tabScrollOffset;
                if (index >= visibleTabs.size()) break;

                ResearchTab tab = visibleTabs.get(index);
                boolean active = currentTab != null && tab.id().equals(currentTab.id());

                int ty = y + i * 26;
                int tx = active ? x + 3 : x;

                if (mx >= tx && mx <= tx + 24 && my >= ty && my <= ty + 24) {
                    if (!active) {
                        this.currentTab = tab;
                    }
                    return true;
                }
            }

            // Клик по дереву
            if (treeComponent.mouseClicked(mx, my, btn, leftPos + DISP_X_OFF, topPos + DISP_Y_OFF, DISP_W, DISP_H)) {
                return true;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    public Map<String, ResearchNode> getResearches() {
        Map<String, ResearchNode> tabNodes = new HashMap<>();
        if (currentTab != null) {
            for (ResearchNode node : ResearchManager.getNodesForTab(currentTab.id())) {
                tabNodes.put(node.id(), node);
            }
        }
        return tabNodes;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (treeComponent.mouseDragged(dx, dy, btn)) return true;
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (treeComponent.mouseScrolled(sy)) return true;
        return super.mouseScrolled(mx, my, sx, sy);
    }

    @Override public void renderBackground(GuiGraphics g, int mx, int my, float pt) {}
    @Override public boolean isPauseScreen() { return false; }
}