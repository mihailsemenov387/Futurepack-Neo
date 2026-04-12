package net.futurepack.client.gui.EScanner.Entrys;

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
    private final Component text;
    private final ResourceLocation image;
    private Button studyButton;

    public StudyPage(String text, String imagePath) {
        this.text = Component.literal(text);
        // Путь указываем относительно папки textures, например "textures/gui/entries/neon.png"
        this.image = ResourceLocation.fromNamespaceAndPath("futurepack", imagePath);
    }

    @Override
    public void init(ResearchTabsScreen screen,ResearchNode node, int x, int y, int width, int height) {
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
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();

        // 1. Заголовок (наш кастомный цвет)
        renderTitle(g, this.node.title(), 0x00FFCC);

        // 2. Иконка предмета (маленькая, слева от заголовка)
        g.renderItem(this.node.icon(), this.x + 8, this.y + 8);

        // 3. Основная картинка (чертеж) - ОДИН РАЗ на высоте y + 25
        g.blit(this.image, this.x + (this.w - 64) / 2, this.y + 25, 0, 0, 64, 64, 64, 64);

        // 4. Описание под картинкой (сдвинули на y + 95, чтобы не перекрывать фото)
        g.drawWordWrap(mc.font, this.text, this.x + 10, this.y + 95, this.w - 20, 0xFFFFFF);

        // 5. Визуальный статус
        if (ClientResearchState.isCompleted(this.node.id())) {
            g.drawCenteredString(mc.font, "✔ ИЗУЧЕНО", this.x + this.w / 2, this.y + this.h - 20, 0x00FFCC);
        }
    }

    @Override
    public void onClose(ResearchTabsScreen screen) {
        if (this.studyButton != null) {
            screen.removeWidgetPublic(this.studyButton);
        }
    }
}