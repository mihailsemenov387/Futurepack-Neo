package net.futurepack.client.gui.EScanner.Entrys;

import com.google.gson.JsonObject;
import net.futurepack.client.gui.EScanner.EScannerMainScreen;
import net.futurepack.client.gui.EScanner.IScannerScreen;
import net.futurepack.client.gui.EScanner.ResearchTabsScreen;
import net.futurepack.research.ClientResearchState;
import net.futurepack.research.ResearchNode;
import net.futurepack.network.RequestCompletePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class StudyPage extends AbstractResearchPage {
    private  Component text;
    private  ResourceLocation image;
    private Button studyButton;

    public StudyPage(String text, String imagePath) {
        this.text = Component.literal(text);
        // Путь указываем относительно папки textures, например "textures/gui/entries/neon.png"
        this.image = ResourceLocation.fromNamespaceAndPath("futurepack", imagePath);
    }

    public StudyPage(){

    }

    @Override
    protected int getContentHeight() {
        int textHeight = Minecraft.getInstance().font.split(this.text, w - 20).size() * 9;
        return 85 + textHeight + 20; // Иконка + Текст + Отступ
    }


    @Override
    public void init(IScannerScreen screen, ResearchNode node, int x, int y, int width, int height) {
        super.init(screen, node, x, y, width, height);

        // Создаем кнопку "Изучить"
        this.studyButton = Button.builder(Component.literal("Изучить"), b -> {
            PacketDistributor.sendToServer(new RequestCompletePayload(this.node.id()));
            b.visible = false;
        }).bounds(this.x + (this.w - 80) / 2, this.y + this.h - 25, 80, 20).build();

        screen.addWidgetPublic(this.studyButton);

        // Скрываем кнопку, если уже изучено
        if (ClientResearchState.isCompleted(this.node.id())) {
            this.studyButton.visible = false;
        }
    }

    @Override
    public void readContentFromJson(JsonObject json) {
        this.text = Component.literal(json.get("text").getAsString());
        this.image =  ResourceLocation.fromNamespaceAndPath("futurepack",json.get("image").getAsString());
    }


    @Override
    protected void renderPageContent(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Рисуем картинку
        g.blit(this.image, this.x + (this.w - 64) / 2, this.y + 15, 0, 0, 64, 64, 64, 64);
        // Рисуем текст
        g.drawWordWrap(Minecraft.getInstance().font, this.text, this.x + 10, this.y + 85, this.w - 20, 0xFFFFFF);
    }

    @Override
    public void onClose(IScannerScreen screen) {
        if (this.studyButton != null) {
            screen.removeWidgetPublic(this.studyButton);
        }
    }
}