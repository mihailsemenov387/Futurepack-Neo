package net.futurepack.client.gui.EScanner;

import com.mojang.blaze3d.systems.RenderSystem;
import net.futurepack.research.ResearchNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class EScannerMainScreen extends Screen {

    private static final ResourceLocation BG_TEXTURE = ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/escanner_bg.png");

    private static final int IMG_W = 185;
    private static final int IMG_H = 235;
    private int leftPos, topPos;

    private final  ResearchTabsScreen research_screen;
    private ResearchNode readingEntry = null;
    public EScannerMainScreen() {
        super(Component.literal("E-Scanner Main"));
        this.research_screen = new ResearchTabsScreen(this);
//        this.readingEntry = research_screen.selectedEntry;
    }



    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - IMG_W) / 2;
        this.topPos = (this.height - IMG_H) / 2;

        int btnX = this.leftPos + 51 + 10; // Отступ внутри черного экрана
        int startY = this.topPos + 48 + 15;
        int btnW = 110;
        int btnH = 20;

        // 1. Кнопка "Исследования"
        this.addRenderableWidget(Button.builder(Component.literal("Исследования"), b -> {
            // ОТКРЫВАЕМ НОВОЕ ОКНО И ПЕРЕДАЕМ ТУДА ССЫЛКУ НА ЭТОТ ЭКРАН (чтобы можно было вернуться)
            this.minecraft.setScreen(this.research_screen);
        }).bounds(btnX, startY, btnW, btnH).build());

        // 2. Кнопка "Результаты сканирования" (Пока заглушка)
        this.addRenderableWidget(Button.builder(Component.literal("База данных"), b -> {
            this.minecraft.player.displayClientMessage(Component.literal("§7Модуль в разработке..."), true);
        }).bounds(btnX, startY + 25, btnW, btnH).build());

        // 3. Кнопка "Настройки" (Пока заглушка)
        this.addRenderableWidget(Button.builder(Component.literal("Настройки"), b -> {
            this.minecraft.player.displayClientMessage(Component.literal("§7Модуль в разработке..."), true);
        }).bounds(btnX, startY + 50, btnW, btnH).build());
    }

    @Override
    public void renderBackground(GuiGraphics g, int mX, int mY, float pT) { }

    @Override
    public void render(@NotNull GuiGraphics g, int mX, int mY, float pT) {
        g.fill(0, 0, this.width, this.height, 0x99000000);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        g.blit(BG_TEXTURE, this.leftPos, this.topPos, 0, 0, IMG_W, IMG_H);

        // Рисуем черный фон экрана
        g.fill(this.leftPos + 51, this.topPos + 48, this.leftPos + 51 + 130, this.topPos + 48 + 173, 0xFF000508);

        // Отрисовка кнопок
        super.render(g, mX, mY, pT);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}