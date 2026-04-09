package net.futurepack.client.gui.EScanner;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;

public class TreeRenderComponent {

    private static final int COLOR_CYAN = 0xFF00E5FF;
    private static final int COLOR_GRID = 0x2200E5FF;

    // --- ЛОКАЛЬНАЯ КАМЕРА ---
    private double scrollX = 0;
    private double scrollY = 0;
    private float zoom = 1.0f;

    private final ResearchTabsScreen parent;

    public TreeRenderComponent(ResearchTabsScreen parent) {
        this.parent = parent;
    }

    // --- ГЛАВНЫЙ МЕТОД ОТРИСОВКИ ---
    public void render(GuiGraphics g, int x, int y, int width, int height, int mX, int mY, ResearchTabsScreen.Tab currentTab) {
        // 1. Фон-сетка (движется за камерой)
        renderTechGrid(g, x, y, width, height);

        // 2. Настраиваем матрицу трансформации
        g.pose().pushPose();
        g.pose().translate(x + width / 2.0f, y + height / 2.0f, 0);
        g.pose().scale(zoom, zoom, 1.0f);
        g.pose().translate((int)scrollX, (int)scrollY, 0);

        // ПОЛУЧАЕМ НОДЫ (Пока берем все из экрана, потом заменим на ResearchManager.getNodesForTab)
        var nodes = parent.getResearches().values();

        // 3. Рисуем линии (Сначала линии, чтобы они были ПОД нодами)
        for (ResearchNode node : nodes) {
            ResearchNode.Status status = parent.getNodeStatus(node);

            // Если нода скрыта, не рисуем к ней линии
            if (status == ResearchNode.Status.HIDDEN) continue;

            for (String pId : node.parents()) {
                ResearchNode p = parent.getResearches().get(pId);
                if (p != null && parent.getNodeStatus(p) != ResearchNode.Status.HIDDEN) {
                    int color = (status == ResearchNode.Status.COMPLETED) ? COLOR_CYAN : 0xFF333333;
                    renderTechLine(g, p.treeX(), p.treeY(), node.treeX(), node.treeY(), color);
                }
            }
        }

        // 4. Рисуем сами узлы (Ноды)
        ResearchNode hovered = null;
        for (ResearchNode node : nodes) {
            ResearchNode.Status status = parent.getNodeStatus(node);
            if (status != ResearchNode.Status.HIDDEN) {
                renderTechNode(g, node, status);
            }

            // Проверяем наведение (с учетом трансформации матрицы)
            if (isMouseOverNode(node, mX, mY, x, y, width, height)) {
                hovered = node;
            }
        }

        g.pose().popPose(); // Возвращаем матрицу в норму

        // 5. Рисуем тултипы поверх всего (после popPose, чтобы текст не скейлился и не мылился)
        if (hovered != null) {
            ResearchNode.Status status = parent.getNodeStatus(hovered);
            g.renderTooltip(parent.getMinecraft().font, List.of(
                    hovered.title().getVisualOrderText(),
                    Component.literal("Статус: " + status).withStyle(ChatFormatting.GRAY).getVisualOrderText()
            ), mX, mY);
        }
    }

    // --- ОТРИСОВКА ЭЛЕМЕНТОВ ---

    private void renderTechGrid(GuiGraphics g, int x, int y, int width, int height) {
        g.fill(x, y, x + width, y + height, 0xFF00080A); // Глубокий черный фон

        float gridSize = 20 * zoom;
        float offX = (float) ((scrollX * zoom) % gridSize);
        float offY = (float) ((scrollY * zoom) % gridSize);

        // Вертикальные
        for (float gx = offX; gx < width; gx += gridSize) {
            g.fill((int)(x + gx), y, (int)(x + gx + 1), y + height, COLOR_GRID);
        }
        // Горизонтальные
        for (float gy = offY; gy < height; gy += gridSize) {
            g.fill(x, (int)(y + gy), x + width, (int)(y + gy + 1), COLOR_GRID);
        }
    }

    private void renderTechLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int midX = x1 + (x2 - x1) / 2;
        int glow = (color & 0x00FFFFFF) | 0x33000000; // Прозрачное свечение

        // Горизонталь 1
        g.fill(x1, y1 - 1, midX, y1 + 1, glow);
        g.fill(x1, y1, midX, y1 + 1, color);
        // Вертикаль
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        g.fill(midX - 1, minY, midX + 1, maxY, glow);
        g.fill(midX, minY, midX + 1, maxY, color);
        // Горизонталь 2
        g.fill(midX, y2 - 1, x2, y2 + 1, glow);
        g.fill(midX, y2, x2, y2 + 1, color);
    }

    private void renderTechNode(GuiGraphics g, ResearchNode node, ResearchNode.Status status) {
        int x = node.treeX();
        int y = node.treeY();
        int color = (status == ResearchNode.Status.COMPLETED) ? COLOR_CYAN : (status == ResearchNode.Status.AVAILABLE ? 0xFFFFFFFF : 0xFF444444);

        g.fill(x - 11, y - 11, x + 11, y + 11, 0xFF000000); // Тень
        g.fill(x - 10, y - 10, x + 10, y + 10, color);      // Рамка
        g.fill(x - 9, y - 9, x + 9, y + 9, 0xFF00080A);     // Центр

        // Уголки (хай-тек декор)
        g.fill(x - 11, y - 11, x - 7, y - 10, color);
        g.fill(x + 7, y - 11, x + 11, y - 10, color);
        g.fill(x - 11, y + 10, x - 7, y + 11, color);
        g.fill(x + 7, y + 10, x + 11, y + 11, color);

        g.renderItem(node.iconStack(), x - 8, y - 8);
    }

    // --- ОБРАБОТКА ВВОДА ---

    public boolean mouseDragged(double mX, double mY, int btn, double dX, double dY) {
        if (btn == 0) {
            this.scrollX += dX / zoom;
            this.scrollY += dY / zoom;
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mX, double mY, double sX, double sY) {
        this.zoom = Mth.clamp(this.zoom + (float) sY * 0.1f, 0.5f, 2.0f);
        return true;
    }

    public boolean mouseClicked(double mX, double mY, int btn, int dX, int dY, int dW, int dH) {
        if (btn == 0) {
            for (ResearchNode node : parent.getResearches().values()) {
                if (isMouseOverNode(node, mX, mY, dX, dY, dW, dH)) {
                    ResearchNode.Status status = parent.getNodeStatus(node);

                    // Если нода доступна или изучена - говорим главному экрану открыть её!
                    if (status == ResearchNode.Status.AVAILABLE || status == ResearchNode.Status.COMPLETED) {
                        parent.openEntry(node);
                        return true;
                    }
                    if (status == ResearchNode.Status.LOCKED) {
                        parent.getMinecraft().player.displayClientMessage(
                                Component.literal("§cДанные зашифрованы. Изучите предыдущие узлы."), true);
                    }
                    return false;
                }
            }
        }
        return false;
    }

    private boolean isMouseOverNode(ResearchNode node, double mX, double mY, int dX, int dY, int dW, int dH) {
        // Проверка: не кликнули ли мы за пределами ножниц (Scissor)
        if (mX < dX || mX > dX + dW || mY < dY || mY > dY + dH) return false;

        // Обратная трансформация координат мыши в координаты дерева
        double rX = mX - (dX + dW / 2.0);
        double rY = mY - (dY + dH / 2.0);
        double cX = (rX / zoom) - scrollX;
        double cY = (rY / zoom) - scrollY;

        // Размер кликабельной зоны (10 пикселей от центра)
        return cX >= node.treeX() - 10 && cX <= node.treeX() + 10 &&
                cY >= node.treeY() - 10 && cY <= node.treeY() + 10;
    }
}