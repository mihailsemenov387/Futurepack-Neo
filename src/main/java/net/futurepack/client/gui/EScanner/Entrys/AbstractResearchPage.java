package net.futurepack.client.gui.EScanner.Entrys;

import net.futurepack.client.gui.EScanner.ResearchTabsScreen;
import net.futurepack.research.ResearchNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public abstract class AbstractResearchPage implements IResearchPage {
    protected int x, y, w, h;
    protected  ResearchNode node;

    @Override
    public void init(ResearchTabsScreen screen, ResearchNode node, int x, int y, int width, int height) {
        this.x = x; this.y = y; this.w = width; this.h = height;
        this.node = node;
    }

    protected void renderTitle(GuiGraphics g, Component title, int color) {
        g.drawCenteredString(Minecraft.getInstance().font, title, this.x + this.w / 2, this.y + 10, color);
    }
}