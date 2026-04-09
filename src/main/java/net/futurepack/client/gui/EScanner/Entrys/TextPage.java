package net.futurepack.client.gui.EScanner.Entrys;

import net.futurepack.client.gui.EScanner.ResearchTabsScreen;
import net.futurepack.research.ClientResearchState;
import net.futurepack.research.ResearchNode;
import net.futurepack.network.RequestCompletePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class TextPage extends AbstractResearchPage {
    private final Component content;

    public TextPage(String text) {
        this.content = Component.literal(text);
    }

    @Override
    public void render(GuiGraphics g, ResearchNode node, int mouseX, int mouseY, float partialTick) {
        // Рисуем заголовок белым цветом
        renderTitle(g, node.title(), 0xFFFFFF);

        g.drawWordWrap(Minecraft.getInstance().font, content, x + 10, y + 30, w - 20, 0xCCCCCC);
    }

    @Override
    public void onClose(ResearchTabsScreen screen, ResearchNode node) {
        // УМНАЯ ПРОВЕРКА: завершаем только если нода была готова к изучению
        if (ClientResearchState.getStatus(node) == ResearchNode.Status.AVAILABLE) {
            PacketDistributor.sendToServer(new RequestCompletePayload(node.id()));
        }
    }
}