package net.futurepack.client.gui.EScanner.Entrys;

import com.google.gson.JsonObject;
import net.futurepack.client.gui.EScanner.IScannerScreen;
import net.futurepack.research.ResearchNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public abstract class AbstractResearchPage implements IResearchPage {
    protected int x, y, w, h;
    protected ResearchNode node;
    protected double scrollAmount = 0;

    @Override
    public void init(IScannerScreen screen, ResearchNode node, int x, int y, int width, int height) {
        this.x = x; this.y = y; this.w = width; this.h = height;
        this.node = node;
        this.scrollAmount = 0;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // 1. Сначала рассчитываем границы (ДО ТРАНСЛЯЦИИ)
        int contentHeight = getContentHeight();
        int visibleHeight = h - 45;
        double maxScroll = Math.max(0, contentHeight - visibleHeight);

        // 2. Сразу зажимаем скролл в рамки (Убирает отскок)
        this.scrollAmount = Mth.clamp(this.scrollAmount, 0, maxScroll);

        // 3. Рисуем заголовок (статично)
        renderTitle(g, node.title(), 0xFFFFFF);

        // 4. Отрисовка контента
        g.enableScissor(x, y + 28, x + w, y + h - 5);
        g.pose().pushPose();

        // Теперь translate использует УЖЕ зажатое (clamped) значение
        g.pose().translate(0, -((int)this.scrollAmount), 0);
        renderPageContent(g, mouseX, (int)(mouseY + scrollAmount), partialTick);

        g.pose().popPose();
        g.disableScissor();

        if (maxScroll > 0) {
            drawScrollbar(g, maxScroll, visibleHeight);
        }
    }



    // Добавляем новый абстрактный метод
    protected abstract int getContentHeight();

    protected abstract void renderPageContent(GuiGraphics g, int mouseX, int mouseY, float partialTick);


//    TODO: remove or fix height
    protected void renderTitle(GuiGraphics g, Component title, int color) {
        g.drawCenteredString(Minecraft.getInstance().font, title, this.x + this.w / 2, this.y + 10, color);
    }

    private void drawScrollbar(GuiGraphics g, double maxScroll, int visibleHeight) {
        int scrollbarX = x + w - 3;
        int yStart = y + 30;
        int yEnd = y + h - 10;
        int trackHeight = yEnd - yStart;
        int barHeight = Math.max(10, (int)((double)visibleHeight / (visibleHeight + maxScroll) * trackHeight));
        int barY = yStart + (int)((trackHeight - barHeight) * (scrollAmount / maxScroll));
        g.fill(scrollbarX, yStart, scrollbarX + 2, yEnd, 0x33FFFFFF);
        g.fill(scrollbarX, barY, scrollbarX + 2, barY + barHeight, 0xFF00E5FF);
    }

    @Override
    public boolean mouseScrolled(double amount) {
        this.scrollAmount -= amount * 12.0;
        return true;
    }
}