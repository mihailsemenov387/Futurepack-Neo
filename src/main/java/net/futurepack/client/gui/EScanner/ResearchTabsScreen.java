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
import java.util.Map;

public class ResearchTabsScreen extends Screen {

    private static final ResourceLocation BG_TEXTURE = ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/escanner_bg.png");

    private static final int IMG_W = 185;
    private static final int IMG_H = 235;
    private static final int DISP_X_OFF = 51;
    private static final int DISP_Y_OFF = 48;
    private static final int DISP_W = 130;
    private static final int DISP_H = 173;

    private int leftPos, topPos;
    private ResearchTab currentTab;
    private final TreeRenderComponent treeComponent;
    private ResearchNode selectedEntry = null;


    public ResearchTabsScreen() {
        super(Component.literal("Research Overview"));
        this.treeComponent = new TreeRenderComponent(this);

        var allTabs = ResearchManager.getTabs();
        if (!allTabs.isEmpty()) {
            this.currentTab = allTabs.iterator().next();
        }
    }

    public String getCurrentTabId() {
        return this.currentTab != null ? this.currentTab.id() : "space";
    }

    public void openEntry(ResearchNode node) {
        this.selectedEntry = node;
//        ClientResearchState.markAsRead(node.id());
//        ResearchHelper.updateProgress(Minecraft.getInstance().player , "alien_tech", ResearchHelper.ProgressType.READ);
        PacketDistributor.sendToServer(new RequestReadPayload(node.id()));
        if (node.page() != null) {
            node.page().init(this, node, this.leftPos + DISP_X_OFF, this.topPos + DISP_Y_OFF, DISP_W, DISP_H);
        }
    }


    private void closeEntry() {
        if (this.selectedEntry != null && this.selectedEntry.page() != null) {
            this.selectedEntry.page().onClose(this, this.selectedEntry);
        }
        this.selectedEntry = null; // Теперь removed() ничего не сделает, так как тут null
    }

    @Override
    public void removed() {
        if (selectedEntry != null && selectedEntry.page() != null) {
            selectedEntry.page().onClose(this, selectedEntry);
        }
        super.removed();
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - IMG_W) / 2;
        this.topPos = (this.height - IMG_H) / 2;

        if (selectedEntry != null && selectedEntry.page() != null) {
            selectedEntry.page().init(this, selectedEntry,this.leftPos + DISP_X_OFF, this.topPos + DISP_Y_OFF, DISP_W, DISP_H);
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

        // 2. Корпус (Возвращаем полную версию blit с размерами 256, 256)
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BG_TEXTURE);
        g.blit(BG_TEXTURE, this.leftPos, this.topPos, 0, 0, IMG_W, IMG_H, 256, 256);

        // 3. Вкладки (Рисуем кнопки)
        renderTabButtons(g);

        // 4. Экран
        int x = this.leftPos + DISP_X_OFF;
        int y = this.topPos + DISP_Y_OFF;


        // Рисуем черную подложку ПЕРЕД ножницами
//        g.fill(x, y, x + DISP_W, y + DISP_H, 0xFF000508);

        g.enableScissor(x, y, x + DISP_W, y + DISP_H);
        if (selectedEntry == null) {
            if (currentTab != null) {
                treeComponent.render(g, x, y, DISP_W, DISP_H, mX, mY, currentTab);
            }
        } else {
            renderEntryPage(g, x, y, mX, mY, pT);
        }
        g.disableScissor(); // Закрыли область обрезки

        // 4. Оверлей (вне Scissor)
        if (selectedEntry == null) {
            // Просим дерево нарисовать тултипы ПОВЕРХ корпуса
            treeComponent.drawTooltip(g, mX, mY);
        }

        renderTabTooltips(g, mX, mY); // Тултипы вкладок тоже здесь

        super.render(g, mX, mY, pT);
    }

    private void renderTabButtons(GuiGraphics g) {
        int x = this.leftPos - 24; // Пододвинул ближе
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
            i++;
        }
    }

    private void renderTabTooltips(GuiGraphics g, int mx, int my) {
        int x = this.leftPos - 24;
        int y = this.topPos + 30;
        int i = 0;
        for (ResearchTab tab : ResearchManager.getTabs()) {
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
            selectedEntry.page().render(g, selectedEntry, mX, mY, pT);
        }
        g.drawString(this.font, "< Назад", x + 5, y + DISP_H - 12, 0x55FFFF);
    }


    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (selectedEntry == null && btn == 0) {
            int x = this.leftPos - 24;
            int y = this.topPos + 30;
            int i = 0;
            for (ResearchTab tab : ResearchManager.getTabs()) {
                int ty = y + i * 28;
                int tx = (tab == currentTab) ? x + 4 : x;
                if (mx >= tx && mx <= tx + 25 && my >= ty && my <= ty + 25) {
                    this.currentTab = tab;
                    return true;
                }
                i++;
            }
            if (treeComponent.mouseClicked(mx, my, btn, leftPos + DISP_X_OFF, topPos + DISP_Y_OFF, DISP_W, DISP_H)) {
                return true;
            }
        }
        if (selectedEntry != null && btn == 0) {

            int x = this.leftPos + DISP_X_OFF;
            int y = this.topPos + DISP_Y_OFF;
            if (mx >= x && mx <= x + 40 && my >= y + DISP_H - 15) {
                closeEntry();
                return true;
            }
        }
        return super.mouseClicked(mx, my, btn);
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