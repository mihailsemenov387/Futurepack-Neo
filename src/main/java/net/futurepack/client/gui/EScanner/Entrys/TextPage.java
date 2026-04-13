package net.futurepack.client.gui.EScanner.Entrys;

import net.futurepack.client.gui.EScanner.IScannerScreen;
import net.futurepack.research.ClientResearchState;
import net.futurepack.research.ResearchNode;
import net.futurepack.network.RequestCompletePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class TextPage extends AbstractResearchPage {
    private Component content;

    public TextPage(String text) {
        this.content = Component.literal(text);
    }

    public TextPage(){

    }

    @Override
    protected int getContentHeight() {
        // Считаем высоту текста заранее
        return Minecraft.getInstance().font.split(content, w - 20).size() * 9 + 15;
    }

    @Override
    protected void renderPageContent(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.drawWordWrap(Minecraft.getInstance().font, content, x + 10, y + 30, w - 20, 0x00000000);
    }
    @Override
    public void onClose(IScannerScreen screen) {
        if (ClientResearchState.getStatus(this.node) == ResearchNode.Status.AVAILABLE) {
            PacketDistributor.sendToServer(new RequestCompletePayload(this.node.id()));
        }
    }
}