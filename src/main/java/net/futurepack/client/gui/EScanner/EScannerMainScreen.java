package net.futurepack.client.gui.EScanner;

import com.mojang.blaze3d.systems.RenderSystem;
import net.futurepack.research.ResearchHelper;
import net.futurepack.research.ResearchManager;
import net.futurepack.research.ResearchNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class EScannerMainScreen extends Screen implements IScannerScreen {

    private static final ResourceLocation BG_TEXTURE = ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/escanner_bg.png");
    private static final int IMG_W = 185, IMG_H = 235;
    private int leftPos, topPos;

    // Храним только экран дерева (чтобы не сбрасывать зум)
    private final ResearchTabsScreen research_screen;

    public EScannerMainScreen() {
        super(Component.literal("E-Scanner Main"));
        this.research_screen = new ResearchTabsScreen(this);
    }

//    @Override
//    protected void init() {
//        super.init();
//        this.leftPos = (this.width - IMG_W) / 2;
//        this.topPos = (this.height - IMG_H) / 2;
//
//        if (readingEntry == null) {
//            initMainMenu();
//        } else {
//            // Если мы вернулись в режим чтения, открываем читалку СРАЗУ
//            // Ноды уже прогружены, поэтому null не будет
//            this.minecraft.setScreen(new EScannerReadScreen(readingEntry, this));
//            this.readingEntry = null; // Сбрасываем, чтобы при возврате попасть в меню
//        }
//    }



    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - IMG_W) / 2;
        this.topPos = (this.height - IMG_H) / 2;

        int btnX = this.leftPos + 51 + 10;
        int startY = this.topPos + 48 + 15;
        int btnW = 110;

        // Кнопка "Исследования"
        this.addRenderableWidget(Button.builder(Component.literal("Исследования"), b -> {
            this.minecraft.setScreen(this.research_screen);
        }).bounds(btnX, startY, btnW, 20).build());

        // Кнопка "Продолжить чтение"
        String lastId = ResearchHelper.getLastReadId();
        if (lastId != null) {
            this.addRenderableWidget(Button.builder(Component.literal("Продолжить"), b -> {
                ResearchNode lastNode = ResearchManager.getNode(ResearchHelper.getLastReadId());
                if (lastNode != null) {
                    // Создаем экран ТОЛЬКО СЕЙЧАС, когда нода точно есть
                    this.minecraft.setScreen(new EScannerReadScreen(lastNode, this));
                }
            }).bounds(btnX, startY + 75, btnW, 20).build());
        }

        // Кнопки-заглушки
        this.addRenderableWidget(Button.builder(Component.literal("База данных"), b -> {}).bounds(btnX, startY + 25, btnW, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Настройки"), b -> {}).bounds(btnX, startY + 50, btnW, 20).build());
    }

    @Override
    public void renderBackground(GuiGraphics g, int mX, int mY, float pT) {}

    @Override
    public void render(@NotNull GuiGraphics g, int mX, int mY, float pT) {
        g.fill(0, 0, this.width, this.height, 0x99000000);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        g.blit(BG_TEXTURE, this.leftPos, this.topPos, 0, 0, IMG_W, IMG_H, 256, 256);
//        g.fill(this.leftPos + 51, this.topPos + 48, this.leftPos + 51 + 130, this.topPos + 48 + 173, 0xFF000508);
        super.render(g, mX, mY, pT);
    }

    public <T extends net.minecraft.client.gui.components.events.GuiEventListener & net.minecraft.client.gui.components.Renderable & net.minecraft.client.gui.narration.NarratableEntry> T addWidgetPublic(T widget) {
        return this.addRenderableWidget(widget);
    }

    public void removeWidgetPublic(net.minecraft.client.gui.components.events.GuiEventListener widget) {
        this.removeWidget(widget);
    }
    @Override public boolean isPauseScreen() { return false; }
}