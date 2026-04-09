package net.futurepack.client.gui.EScanner.Entrys;

import net.futurepack.research.ResearchNode;
import net.minecraft.client.gui.GuiGraphics;
import net.futurepack.client.gui.EScanner.ResearchTabsScreen;
import net.minecraft.network.chat.Component;

public interface IResearchPage {
    void init(ResearchTabsScreen screen, ResearchNode node, int x, int y, int width, int height);
    void render(GuiGraphics g, ResearchNode node, int mouseX, int mouseY, float partialTick);
    void onClose(ResearchTabsScreen screen, ResearchNode node);

    default Component getPageTitle(ResearchNode node) {
        return node.title(); // По умолчанию берем из ноды
    }

}