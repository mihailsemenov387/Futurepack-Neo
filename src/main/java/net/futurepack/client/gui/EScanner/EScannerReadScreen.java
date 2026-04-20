package net.futurepack.client.gui.EScanner;

import net.futurepack.client.gui.EScanner.Entrys.IResearchPage;
import net.futurepack.research.ResearchHelper;
import net.futurepack.research.ResearchNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.GameRenderer;

public class EScannerReadScreen extends Screen implements IScannerScreen {
    private final ResearchNode node;
    private IResearchPage page;
    private final Screen backScreen;
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/escanner_bg.png");

    public EScannerReadScreen(ResearchNode node, Screen backScreen) {
        // Проверка на null, чтобы не было краша
        super(node != null ? node.title() : Component.literal("Reading..."));
        this.node = node;
        this.backScreen = backScreen;

        if (node != null) {
            ResearchHelper.setLastReadId(node.id());
        }
    }



    @Override
    protected void init() {
        super.init(); // Всегда вызывай super
        if (node != null) {
            // Берем страницу из словаря
            this.page = net.futurepack.client.PageDictionary.getPage(node.id());

            // ПРОВЕРКА: Если страница существует — инициализируем
            if (this.page != null) {
                this.page.init(this, node, (this.width - 185) / 2 + 51, (this.height - 235) / 2 + 48, 130, 173);
            } else {
                // Если страницы нет, можно вывести в консоль ошибку для дебага
                System.err.println("[Futurepack] Missing Page for node: " + node.id());
            }
        }
    }
    @Override
    public void renderBackground(GuiGraphics g, int mX, int mY, float pT) {}

    @Override
    public void render(GuiGraphics g, int mX, int mY, float pT) {
        g.fill(0, 0, this.width, this.height, 0x99000000);

        int iLeft = (this.width - 185) / 2;
        int iTop = (this.height - 235) / 2;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        g.blit(BG, iLeft, iTop, 0, 0, 185, 235, 256, 256);

        int x = iLeft + 51;
        int y = iTop + 48;
//        g.fill(x, y, x + 130, y + 173, 0xFF000508);

        if (node != null && page != null) {
            g.enableScissor(x, y, x + 130, y + 173);
            page.render(g, mX, mY, pT);
            g.disableScissor();
        }

        g.drawString(this.font, "< Назад", x + 5, y + 173 - 12, 0x55FFFF);
        super.render(g, mX, mY, pT);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 1 || isBackClicked(mx, my)) {
            if (node != null && page != null) page.onClose(this);
            this.minecraft.setScreen(backScreen);
            return true;
        }

        return super.mouseClicked(mx, my, btn);
    }

    private boolean isBackClicked(double mx, double my) { // back button
        int xStart = (this.width - 185) / 2 + 51 + 5;
        int yStart = (this.height - 235) / 2 + 48 + 173 - 15;
        return mx >= xStart && mx <= xStart + 45 && my >= yStart && my <= yStart + 15;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (node != null && page != null) {
            if (page.mouseScrolled(scrollY)) return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }


    @Override
    public void removed() {
        // Если экран закрыли (даже на ESC), и страница была активна — закрываем её официально
        if (page != null) {
            page.onClose(this);
        }
        super.removed();
    }

    public <T extends net.minecraft.client.gui.components.events.GuiEventListener & net.minecraft.client.gui.components.Renderable & net.minecraft.client.gui.narration.NarratableEntry> T addWidgetPublic(T widget) {
        return this.addRenderableWidget(widget);
    }

    public void removeWidgetPublic(net.minecraft.client.gui.components.events.GuiEventListener widget) {
        this.removeWidget(widget);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}