package net.futurepack.client.gui.EScanner;

import net.futurepack.research.ClientResearchState;
import net.futurepack.research.ResearchManager;
import net.futurepack.research.ResearchNode;
import net.futurepack.research.ResearchNode.Status;
import net.futurepack.research.ResearchTab; // Правильный импорт
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.client.player.LocalPlayer;

import java.util.List;

public class TreeRenderComponent {
    private static final int COLOR_CYAN = 0xFF00E5FF;
    private static final int COLOR_GRID = 0x2200E5FF;

    private double scrollX = 0;
    private double scrollY = 0;
    private float zoom = 1.0f;

    private final ResearchTabsScreen parent;

    public TreeRenderComponent(ResearchTabsScreen parent) {
        this.parent = parent;
    }

    // Заменили ResearchTabsScreen.Tab на просто ResearchTab
    public void render(GuiGraphics g, int x, int y, int width, int height, int mX, int mY, ResearchTab currentTab) {
        renderTechGrid(g, x, y, width, height);

        g.pose().pushPose();
        g.pose().translate(x + width / 2.0f, y + height / 2.0f, 0);
        g.pose().scale(zoom, zoom, 1.0f);
        g.pose().translate((int)scrollX, (int)scrollY, 0);

        // Получаем ноды только для текущей вкладки
        List<ResearchNode> nodes = ResearchManager.getNodesForTab(currentTab.id());

        for (ResearchNode node : nodes) {
            Status status = ClientResearchState.getStatus(node);
            if (status == Status.HIDDEN) continue;

            for (String pId : node.parents()) {
                ResearchNode p = ResearchManager.getNode(pId);
                if (p != null && ClientResearchState.getStatus(p) != Status.HIDDEN) {
                    int color = (status == Status.COMPLETED) ? COLOR_CYAN : 0xFF333333;
                    renderTechLine(g, p.treeX(), p.treeY(), node.treeX(), node.treeY(), color);
                }
            }
        }

        ResearchNode hovered = null;
        for (ResearchNode node : nodes) {
            Status status = ClientResearchState.getStatus(node);
            if (status != Status.HIDDEN) {
                renderTechNode(g, node, status);
            }

            if (isMouseOverNode(node, mX, mY, x, y, width, height)) {
                hovered = node;
            }
        }

        g.pose().popPose();

        if (hovered != null) {
            Status status = ClientResearchState.getStatus(hovered);
            g.renderTooltip(parent.getMinecraft().font, List.of(
                    hovered.title().getVisualOrderText(),
                    Component.literal("Статус: " + status).withStyle(ChatFormatting.GRAY).getVisualOrderText()
            ), mX, mY);
        }
    }

    private void renderTechGrid(GuiGraphics g, int x, int y, int width, int height) {
        g.fill(x, y, x + width, y + height, 0xFF00080A);
        float gridSize = 20 * zoom;
        float offX = (float) ((scrollX * zoom) % gridSize);
        float offY = (float) ((scrollY * zoom) % gridSize);
        for (float gx = offX; gx < width; gx += gridSize) {
            g.fill((int)(x + gx), y, (int)(x + gx + 1), y + height, COLOR_GRID);
        }
        for (float gy = offY; gy < height; gy += gridSize) {
            g.fill(x, (int)(y + gy), x + width, (int)(y + gy + 1), COLOR_GRID);
        }
    }

    private void renderTechLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int midX = x1 + (x2 - x1) / 2;
        int glow = (color & 0x00FFFFFF) | 0x33000000;
        g.fill(x1, y1 - 1, midX + 1, y1 + 1, glow);
        g.fill(x1, y1, midX, y1 + 1, color);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        g.fill(midX - 1, minY, midX + 1, maxY, glow);
        g.fill(midX, minY, midX + 1, maxY, color);
        g.fill(midX, y2 - 1, x2, y2 + 1, glow);
        g.fill(midX, y2, x2, y2 + 1, color);
    }

    private void renderTechNode(GuiGraphics g, ResearchNode node, Status status) {
        int x = node.treeX();
        int y = node.treeY();
        int color = (status == Status.COMPLETED) ? COLOR_CYAN : (status == Status.AVAILABLE ? 0xFFFFFFFF : 0xFF444444);
        g.fill(x - 11, y - 11, x + 11, y + 11, 0xFF000000);
        g.fill(x - 10, y - 10, x + 10, y + 10, color);
        g.fill(x - 9, y - 9, x + 9, y + 9, 0xFF00080A);
        g.fill(x - 11, y - 11, x - 7, y - 10, color);
        g.fill(x + 7, y - 11, x + 11, y - 10, color);
        g.fill(x - 11, y + 10, x - 7, y + 11, color);
        g.fill(x + 7, y + 10, x + 11, y + 11, color);

        // В рекорде ResearchNode поле называется icon()
        g.renderItem(node.icon(), x - 8, y - 8);
    }

    public boolean mouseClicked(double mX, double mY, int btn, int dX, int dY, int dW, int dH) {
        if (btn == 0) {
            // Ищем ноды именно в текущем табе через менеджер
            for (ResearchNode node : ResearchManager.getNodesForTab(parent.getCurrentTabId())) {
                if (isMouseOverNode(node, mX, mY, dX, dY, dW, dH)) {
                    Status status = ClientResearchState.getStatus(node);
                    if (status == Status.AVAILABLE || status == Status.COMPLETED) {
                        parent.openEntry(node);
                        return true;
                    }
                    if (status == Status.LOCKED) {
                        LocalPlayer player = parent.getMinecraft().player;
                        if (player != null) {
                            player.displayClientMessage(Component.literal("§cДанные зашифрованы. Изучите предыдущие узлы."), true);
                        }
                    }
                    return false;
                }
            }
        }
        return false;
    }

    private boolean isMouseOverNode(ResearchNode node, double mX, double mY, int dX, int dY, int dW, int dH) {
        if (mX < dX || mX > dX + dW || mY < dY || mY > dY + dH) return false;
        double rX = mX - (dX + dW / 2.0);
        double rY = mY - (dY + dH / 2.0);
        double cX = (rX / zoom) - scrollX;
        double cY = (rY / zoom) - scrollY;
        return cX >= node.treeX() - 10 && cX <= node.treeX() + 10 && cY >= node.treeY() - 10 && cY <= node.treeY() + 10;
    }

    public boolean mouseDragged(double dX, double dY, int btn) {
        if (btn == 0) {
            this.scrollX += dX / zoom;
            this.scrollY += dY / zoom;
            return true;
        }
        return false;
    }

    public void mouseScrolled(double amount) {
        this.zoom = Mth.clamp(this.zoom + (float) amount * 0.1f, 0.5f, 2.0f);
    }
}