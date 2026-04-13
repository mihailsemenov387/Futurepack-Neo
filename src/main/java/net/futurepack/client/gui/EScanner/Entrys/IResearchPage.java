package net.futurepack.client.gui.EScanner.Entrys;

import com.google.gson.JsonObject;
import net.futurepack.client.gui.EScanner.IScannerScreen;
import net.futurepack.research.ResearchNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public interface IResearchPage {
    void init(IScannerScreen  screen, ResearchNode node, int x, int y, int width, int height);
    void readContentFromJson(JsonObject json);


    void render(GuiGraphics g, int mouseX, int mouseY, float partialTick);
    void onClose(IScannerScreen screen);

    default Component getPageTitle(ResearchNode node) {
        return node.title(); // По умолчанию берем из ноды
    }
    default boolean mouseScrolled(double amount) { return false; }

}