package net.futurepack.client.gui.EScanner;

import com.mojang.blaze3d.systems.RenderSystem;
import net.futurepack.network.RequestCompletePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


import java.util.*;

import static net.futurepack.client.gui.EScanner.ResearchNode.Status;

public class EScannerScreen extends Screen {

    // --- РЕСУРСЫ И ЦВЕТА ---
    private static final ResourceLocation BG_TEXTURE = ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/escanner_bg.png");

    private static final int COLOR_CYAN = 0xFF00E5FF;
    private static final int COLOR_DARK_BG = 0xFF00080A;
    private static final int COLOR_GRID = 0x2200E5FF;

    // --- ГЕОМЕТРИЯ ПЛАНШЕТА ---
    private static final int IMG_W = 185;
    private static final int IMG_H = 235;
    private static final int DISP_X_OFF = 51;
    private static final int DISP_Y_OFF = 48;
    private static final int DISP_W = 130;
    private static final int DISP_H = 173;

    // --- СОСТОЯНИЕ ---
    private int leftPos, topPos;
    private double scrollX = 0, scrollY = 0;
    private float zoom = 1.0f;

    private final Map<String, ResearchNode> researches = new HashMap<>();
    private ResearchNode selectedEntry = null;
    private Button backButton;

    // Прогресс игрока (синхронизируется с сервера)
    private static Set<String> COMPLETED = new HashSet<>();
    private static Set<String> REVEALED = new HashSet<>();

    private Button studyButton;


    public static void updateClientProgress(Set<String> completed, Set<String> revealed) {
        COMPLETED = completed;
        REVEALED = revealed;
    }

    public EScannerScreen() {
        super(Component.literal("E-Scanner"));
        initNodes();
    }




    private void sendCompletePacket(String id) {
        // NeoForge 1.21.1 способ отправки на сервер
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new RequestCompletePayload(id));
    }
    private int getDispX() { return this.leftPos + DISP_X_OFF; }
    private int getDispY() { return this.topPos + DISP_Y_OFF; }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x99000000);

        // background
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(BG_TEXTURE, this.leftPos, this.topPos, 0, 0, IMG_W, IMG_H);

        renderDisplay(guiGraphics, mouseX, mouseY);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderDisplay(GuiGraphics g, int mouseX, int mouseY) {
        int x = getDispX();
        int y = getDispY();

        g.enableScissor(x, y, x + DISP_W, y + DISP_H);

        // FSM
        if (this.selectedEntry == null) {
            renderGrid(g, x, y);
            renderTree(g, x, y, mouseX, mouseY);
        } else {
            g.fill(x, y, x + DISP_W, y + DISP_H, 0xFF000508);
            renderEntry(g, x, y);
        }

        g.disableScissor();
    }

    private void renderGrid(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + DISP_W, y + DISP_H, 0xFF00080A);

        float gridSize = 20 * zoom;
        float offX = (float) ((scrollX * zoom) % gridSize);
        float offY = (float) ((scrollY * zoom) % gridSize);

        for (float gx = offX; gx < DISP_W; gx += gridSize) {
            g.fill((int)(x + gx), y, (int)(x + gx + 1), y + DISP_H, COLOR_GRID);
        }
        for (float gy = offY; gy < DISP_H; gy += gridSize) {
            g.fill(x, (int)(y + gy), x + DISP_W, (int)(y + gy + 1), COLOR_GRID);
        }
    }

    private void renderTree(GuiGraphics g, int x, int y, int mX, int mY) {
        g.pose().pushPose();
        g.pose().translate(x + DISP_W / 2.0f, y + DISP_H / 2.0f, 0);
        g.pose().scale(zoom, zoom, 1.0f);
        g.pose().translate((int)scrollX, (int)scrollY, 0);

        // Рисуем линии
        for (ResearchNode node : researches.values()) {
            Status status = getNodeStatus(node);
            for (String pId : node.parents()) {
                ResearchNode p = researches.get(pId);
                if (p != null) {
                    int color = (status == Status.COMPLETED) ? COLOR_CYAN : 0xFF333333;
                    renderLine(g, p.treeX(), p.treeY(), node.treeX(), node.treeY(), color);
                }
            }
        }

        // Рисуем узлы
        ResearchNode hovered = null;
        for (ResearchNode node : researches.values()) {
            Status status = getNodeStatus(node);
            if(status != Status.HIDDEN) {renderNode(g, node, status);}

            if (isMouseOverNode(node, mX, mY, x, y, DISP_W, DISP_H)) {
                hovered = node;
            }
        }

        g.pose().popPose();

        if (hovered != null) {
            Status status = getNodeStatus(hovered);
            g.renderTooltip(this.font, List.of(
                    hovered.title().getVisualOrderText(),
                    Component.literal("Статус: " + status).withStyle(ChatFormatting.GRAY).getVisualOrderText()
            ), mX, mY);
        }
    }

    private Status getNodeStatus(ResearchNode node) {
//        if (CLIENT_PROGRESS.contains(node.id())) return Status.AVAILABLE;
//        boolean parentsDone = node.parents().isEmpty() || node.parents().stream().allMatch(CLIENT_PROGRESS::contains);
//        return parentsDone ? Status.AVAILABLE : Status.LOCKED;

        // 1. ПРИОРИТЕТ: Если технология уже в списке изученных
        if (COMPLETED.contains(node.id())) {
            return Status.COMPLETED; // Светится зеленым
        }

        // 2. ПРОВЕРКА НА СКРЫТОСТЬ (HIDDEN):
        // Нода скрыта, если она "секретная" по умолчанию И её нет в списке REVEALED
        if (node.isHidden() && !REVEALED.contains(node.id())) {
            return Status.HIDDEN; // Вообще не рисуем
        }

        // --- Если мы дошли до сюда, значит нода ВИДИМАЯ (будет отрисована) ---

        // 3. ДОСТУПНОСТЬ ЧТЕНИЯ: Можно читать, если родители в COMPLETED
        boolean parentsFinished = node.parents().isEmpty() ||
                node.parents().stream().allMatch(COMPLETED::contains);

        return parentsFinished ? ResearchNode.Status.AVAILABLE : ResearchNode.Status.LOCKED;
    }

    private void renderLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        // midX - точка изгиба линии
        int midX = x1 + (x2 - x1) / 2;
        int glow = (color & 0x00FFFFFF) | 0x33000000;

        // 1. Горизонтальная линия от родителя до середины
        g.fill(x1, y1 - 1, midX, y1 + 1, glow); // свечение
        g.fill(x1, y1, midX, y1 + 1, color);

        // 2. Вертикальная линия в середине (ОШИБКА БЫЛА ТУТ)
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        g.fill(midX - 1, minY, midX + 1, maxY, glow); // свечение
        g.fill(midX, minY, midX + 1, maxY, color);

        // 3. Горизонтальная линия от середины до текущей ноды
        g.fill(midX, y2 - 1, x2, y2 + 1, glow); // свечение
        g.fill(midX, y2, x2, y2 + 1, color);
    }

    private void renderNode(GuiGraphics g, ResearchNode node, ResearchNode.Status status) {
        int x = node.treeX();
        int y = node.treeY();
        int color = (status == ResearchNode.Status.COMPLETED) ? COLOR_CYAN : (status == Status.AVAILABLE ? 0xFFFFFFFF : 0xFF444444);

        // Рамка с уголками
        g.fill(x - 11, y - 11, x + 11, y + 11, 0xFF000000); // Тень
        g.fill(x - 10, y - 10, x + 10, y + 10, color);      // Рамка
        g.fill(x - 9, y - 9, x + 9, y + 9, 0xFF00080A);    // Фон центра

        // Декор уголков
        g.fill(x - 11, y - 11, x - 7, y - 10, color);
        g.fill(x + 7, y - 11, x + 11, y - 10, color);
        g.fill(x - 11, y + 10, x - 7, y + 11, color);
        g.fill(x + 7, y + 10, x + 11, y + 11, color);

        g.renderItem(node.iconStack(), x - 8, y - 8);
    }

//    private void renderEntry(GuiGraphics g, int x, int y) {
//        g.drawCenteredString(this.font, selectedEntry.title(), x + DISP_W / 2, y + 25, COLOR_CYAN);
//        g.drawWordWrap(this.font, selectedEntry.description(), x + 10, y + 45, DISP_W - 20, 0xFFFFFFFF);
//        this.backButton.visible = true;
//    }


    private void renderEntry(GuiGraphics g, int x, int y) {
        g.drawCenteredString(this.font, selectedEntry.title(), x + DISP_W / 2, y + 25, 0x00FFCC);
        g.drawWordWrap(this.font, selectedEntry.description(), x + 10, y + 45, DISP_W - 20, 0xFFFFFF);

        // Показываем кнопку назад всегда
        this.backButton.visible = true;

        // ЛОГИКА КНОПКИ "ИЗУЧИТЬ"
        ResearchNode.Status status = getNodeStatus(selectedEntry);
        if (status == ResearchNode.Status.AVAILABLE) {
            this.studyButton.visible = true;
            this.studyButton.active = true;
        } else {
            this.studyButton.visible = false;
        }
    }

    // --- УПРАВЛЕНИЕ ---

    @Override
    public boolean mouseDragged(double mX, double mY, int btn, double dX, double dY) {
        if (this.selectedEntry == null && btn == 0) {
            this.scrollX += dX / zoom;
            this.scrollY += dY / zoom;
            return true;
        }
        return super.mouseDragged(mX, mY, btn, dX, dY);
    }

    @Override
    public boolean mouseScrolled(double mX, double mY, double sX, double sY) {
        if (this.selectedEntry == null) {
            this.zoom = Mth.clamp(this.zoom + (float) sY * 0.1f, 0.5f, 2.0f);
            return true;
        }
        return super.mouseScrolled(mX, mY, sX, sY);
    }

//    @Override
//    public boolean mouseClicked(double mX, double mY, int btn) {
//        if (this.selectedEntry == null && btn == 0) {
//            for (ResearchNode node : researches.values()) {
//                if (isMouseOverNode(node, (int)mX, (int)mY, getDispX(), getDispY(), DISP_W, DISP_H)) {
//                    this.selectedEntry = node;
//                    return true;
//                }
//            }
//        }
//        return super.mouseClicked(mX, mY, btn);
//    }
//

    @Override
    public boolean mouseClicked(double mX, double mY, int btn) {
        if (this.selectedEntry == null && btn == 0) {
            for (ResearchNode node : researches.values()) {
                if (isMouseOverNode(node, (int)mX, (int)mY, getDispX(), getDispY(), DISP_W, DISP_H)) {

                    ResearchNode.Status status = getNodeStatus(node);

                    if (status == ResearchNode.Status.AVAILABLE || status == ResearchNode.Status.COMPLETED) {
                        // Открываем экран чтения
                        this.selectedEntry = node;
                        return true;
                    }

                    if (status == ResearchNode.Status.LOCKED) {
                        this.minecraft.player.displayClientMessage(
                                Component.literal("§cДанные зашифрованы. Нужно изучить предыдущие узлы."), true);
                    }
                    return false;
                }
            }
        }
        // Если мы уже внутри записи, клики обрабатываются кнопками (backButton, studyButton)
        return super.mouseClicked(mX, mY, btn);
    }

    private boolean isMouseOverNode(ResearchNode node, int mX, int mY, int dX, int dY, int dW, int dH) {
        if (mX < dX || mX > dX + dW || mY < dY || mY > dY + dH) return false;
        double rX = mX - (dX + dW / 2.0);
        double rY = mY - (dY + dH / 2.0);
        double cX = (rX / zoom) - scrollX;
        double cY = (rY / zoom) - scrollY;
        return cX >= node.treeX() - 10 && cX <= node.treeX() + 10 && cY >= node.treeY() - 10 && cY <= node.treeY() + 10;
    }

    @Override
    public boolean isPauseScreen() { return false; }

}