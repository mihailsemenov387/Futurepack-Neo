package net.futurepack.client.gui.EScanner;

import com.mojang.blaze3d.systems.RenderSystem;
import net.futurepack.research.ClientResearchState;
import net.futurepack.research.ResearchRegistry;
import net.futurepack.research.ResearchNode;
import net.futurepack.research.ResearchNode.Status;
import net.futurepack.research.ResearchTab; // Правильный импорт
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.client.player.LocalPlayer;

import java.util.List;

public class TreeRenderComponent {
    private static final int COLOR_CYAN = 0xFF00E5FF;
    private static final int COLOR_GRID = 0x2200E5FF;

    private double scrollX = 0;
    private double scrollY = 0;
    private float zoom = 1.0f;
    private static final int BG_W = 3500;
    private static final int BG_H = 1083;

    private ResearchNode hoveredNode = null;
    private final ResearchTabsScreen parent;

    public TreeRenderComponent(ResearchTabsScreen parent) {
        this.parent = parent;
    }

    // Заменили ResearchTabsScreen.Tab на просто ResearchTab
//    public void render(GuiGraphics g, int x, int y, int width, int height, int mX, int mY, ResearchTab currentTab) {
//        this.hoveredNode = null; // Сбрасываем перед каждым кадром
//
//        renderTechGrid(g, x, y, width, height);
//
//
//
//        g.pose().pushPose();
//        g.pose().translate(x + width / 2.0f, y + height / 2.0f, 0);
//        g.pose().scale(zoom, zoom, 1.0f);
//        g.pose().translate((int)scrollX, (int)scrollY, 0);
//
//        List<ResearchNode> nodes = ResearchManager.getNodesForTab(currentTab.id());
//
//        // 1. Рисуем линии
//        for (ResearchNode node : nodes) {
//            Status status = ClientResearchState.getStatus(node);
//            if (status == Status.HIDDEN) continue;
//            for (String pId : node.links()) {
//                ResearchNode p = ResearchManager.getNode(pId);
//                if (p != null && ClientResearchState.getStatus(pId) != Status.HIDDEN) {
//                    int color = (status == Status.COMPLETED) ? COLOR_CYAN : 0xFF333333;
//                    renderTechLine(g, p.treeX(), p.treeY(), node.treeX(), node.treeY(), color);
//                }
//            }
//        }
//
//        // 2. Рисуем ноды и ЗАПОМИНАЕМ наведение
//        for (ResearchNode node : nodes) {
//            Status status = ClientResearchState.getStatus(node);
//            if (status == Status.HIDDEN) continue;
//
//            renderTechNode(g, node, status);
//            // Проверяем мышь
//            if (isMouseOverNode(node, mX, mY, x, y, width, height)) {
//                this.hoveredNode = node;
//            }
//        }
//        g.pose().popPose();
//    }

    public void render(GuiGraphics g, int x, int y, int width, int height, int mX, int mY, ResearchTab currentTab) {
        this.hoveredNode = null;

        // 1. Ограничиваем скролл, чтобы не выходить за границы картинки
        // Математика: край картинки минус половина ширины экрана (с учетом зума)
        double limitX = Math.max(0, (BG_W - width / zoom) / 2.0);
        double limitY = Math.max(0, (BG_H - height / zoom) / 2.0);
        scrollX = Mth.clamp(scrollX, -limitX, limitX);
        scrollY = Mth.clamp(scrollY, -limitY, limitY);

        // 2. Фон (рисуем черную пустоту, если картинка вдруг не прогрузится)
        g.fill(x, y, x + width, y + height, 0xFF000000);

        g.pose().pushPose();
        // Используем (int) для всех translate, чтобы убрать "дрожание" в 1 пиксель
        g.pose().translate(x + width / 2, y + height / 2, 0);
        g.pose().scale(zoom, zoom, 1.0f);
        g.pose().translate((int)scrollX, (int)scrollY, 0);

        // 3. Отрисовка гигантского фона
        if (currentTab.tab_bg() != null) {
            g.setColor(0.6f, 0.6f, 0.6f, 1.0f);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            g.blit(currentTab.tab_bg(), -BG_W / 2, -BG_H / 2, 0, 0, BG_W, BG_H, BG_W, BG_H);
        }

        // 4. Линии и Ноды
        List<ResearchNode> nodes = ResearchRegistry.getNodesForTab(currentTab.id());
        for (ResearchNode node : nodes) {
            var status = ClientResearchState.getStatus(node);
            if (status == ResearchNode.Status.HIDDEN) continue;

            // Линии
            for (String pId : node.links()) {
                ResearchNode p = ResearchRegistry.getNode(pId);
                if (p != null && ClientResearchState.getStatus(p) != ResearchNode.Status.HIDDEN) {

//                    int lineColor = (status == Status.COMPLETED || status == Status.AVAILABLE) ? 0xFFFFFFFF : 0xFF333333;
                    int lineColor = (status == Status.COMPLETED || status == Status.AVAILABLE) ? 0xFFE6CC00 : 0xFF2B2031;
                    renderTechLine(g, p.treeX(), p.treeY(), node.treeX(), node.treeY(),
                            lineColor); // COLOR LINE
                }
            }
        }

        for (ResearchNode node : nodes) {
            var status = ClientResearchState.getStatus(node);
            if (status == ResearchNode.Status.HIDDEN) continue;

            renderTechNode(g, node, status);
            if (isMouseOverNode(node, mX, mY, x, y, width, height)) {
                this.hoveredNode = node;
            }
        }

        g.pose().popPose();
    }

    // НОВЫЙ МЕТОД: Рисует тултип ПОВЕРХ всего
    public void drawTooltip(GuiGraphics g, int mX, int mY) {
        if (this.hoveredNode != null) {
            Status status = ClientResearchState.getStatus(hoveredNode);
            g.renderTooltip(parent.getMinecraft().font, List.of(
                    hoveredNode.title().getVisualOrderText(),
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

        int glowColor = (color & 0x00FFFFFF) | 0x33000000;

        // Рисуем сначала "ореол" свечения (ширина 3 пикселя)
        drawGlowLine(g, x1, y1, midX, y1, glowColor); // Горизонталь 1
        drawGlowLine(g, midX, y1, midX, y2, glowColor); // Вертикаль
        drawGlowLine(g, midX, y2, x2, y2, glowColor); // Горизонталь 2

        g.fill(x1, y1, midX, y1 + 1, color);
        g.fill(midX, Math.min(y1, y2), midX + 1, Math.max(y1, y2), color);
        g.fill(midX, y2, x2, y2 + 1, color);
    }

    private void drawGlowLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        // Рисуем чуть более толстую и прозрачную подложку
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        g.fill(minX - 1, minY - 1, maxX + 1, maxY + 1, color);
    }
//    private void renderTechNode(GuiGraphics g, ResearchNode node, Status status) {
//        int x = node.treeX();
//        int y = node.treeY();
//
//        // 1. Настройка яркости для всей ноды (Тинтинг)
//        if (status == Status.COMPLETED) {
//            g.setColor(1.0f, 1.0f, 1.0f, 1.0f); // Максимальная яркость
//        } else if (status == Status.AVAILABLE) {
//            g.setColor(0.6f, 0.6f, 0.6f, 1.0f); // Серый (еще не изучено)
//        } else {
//            g.setColor(0.25f, 0.25f, 0.25f, 1.0f); // Почти черный (заблокировано)
//        }
//
//        // 2. Рисуем рамку (она примет цвет из g.setColor автоматически)
//        renderFrameByType(g, x, y, node.frame(), (status == Status.LOCKED) ? 0xFF333333 : 0xFFFFFFFF);
//
//        // 3. Рисуем иконку
//        com.mojang.blaze3d.platform.Lighting.setupFor3DItems();
//
//        if (node.customIcon() != null) {
//            g.blit(node.customIcon(), x - 8, y - 8, 0, 0, 16, 16, 16, 16);
//        } else {
//            g.renderItem(node.icon(), x - 8, y - 8);
//        }
//
//        // Сброс цвета для тултипов и прочего
//        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
//
//        // 4. ТОТ САМЫЙ КРАСНЫЙ ДИОД (только для AVAILABLE и непрочитанных)
//        if (!ClientResearchState.isRead(node.id()) && status == Status.AVAILABLE) {
//            renderDiode(g, x, y);
//        }
//    }

    private void renderTechNode(GuiGraphics g, ResearchNode node, Status status) {
        int x = node.treeX();
        int y = node.treeY();

        float brightness;

        if (status == Status.COMPLETED) {
            brightness = 1.0f; // Изучено — всегда горит ярко
        } else if (status == Status.AVAILABLE) {
            // ПУЛЬСАЦИЯ для доступных нод
            // Синус дает значение от -1 до 1.
            // Превращаем его в диапазон от 0.4 (темный) до 0.9 (светло-серый)
            float wave = (float) (Math.sin(System.currentTimeMillis() / 400.0) * 0.25 + 0.65);
            brightness = wave;
        } else {
            brightness = 0.25f; // Заблокировано — всегда почти черное
        }

        // Применяем яркость ко всей последующей отрисовке (рамка + иконка)
        g.setColor(brightness, brightness, brightness, 1.0f);

        // 1. Рисуем рамку (она примет цвет из g.setColor)
        renderFrameByType(g, x, y, node.frame(), 0xFFFFFFFF);

        // 2. Рисуем иконку
        com.mojang.blaze3d.platform.Lighting.setupFor3DItems();
        if (node.customIcon() != null) {
            g.blit(node.customIcon(), x - 8, y - 8, 0, 0, 16, 16, 16, 16);
        } else {
            // Примечание: renderItem не всегда идеально слушается g.setColor,
            // но на рамке эффект будет виден отлично.
            g.renderItem(node.icon(), x - 8, y - 8);
        }

        // Сброс цвета, чтобы не покрасить остальной интерфейс
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        // 3. Красный диод для непрочитанных (оставляем его логику мерцания отдельно)
        if (!ClientResearchState.isRead(node.id()) && status == Status.AVAILABLE) {
            renderDiode(g, x, y);
        }
    }


    private void renderFrameByType(GuiGraphics g, int x, int y, ResearchNode.NodeFrameType type, int colorWithAlpha) {
        // Для текстур (blit) прозрачность возьмется из g.setColor, установленного ранее
        ResourceLocation frameTex = ResearchNode.getFramePathByType(type);
        g.blit(frameTex, x - 11, y - 11, 0, 0, 22, 22, 22, 22);
    }

    private void renderDiode(GuiGraphics g, int x, int y) {
        // Та самая формула мигания
        float wave = (float) (Math.sin(System.currentTimeMillis() / 150.0) * 0.5 + 0.5);
        int alpha = (int)(160 + (95 * wave)); // От 160 до 255 (чтобы не гасла совсем)

        int redColor = (alpha << 24) | 0xFF0000;
        int darkBase = 0xFF000000;

        g.pose().pushPose();
        g.pose().translate(0, 0, 300); // Поверх иконки

        // Улучшенный старый диод: черная подложка + мигающий центр
        // Координаты: верхний правый угол рамки (x+7, y-11)
        g.fill(x + 7, y - 11, x + 12, y - 6, darkBase); // Корпус диода
        g.fill(x + 8, y - 10, x + 11, y - 7, redColor); // Светящийся элемент

        g.pose().popPose();
    }
    public boolean mouseClicked(double mX, double mY, int btn, int dX, int dY, int dW, int dH) {
        if (btn == 0) {
            // Ищем ноды именно в текущем табе через менеджер
            for (ResearchNode node : ResearchRegistry.getNodesForTab(parent.getCurrentTabId())) {
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
        double cX = (rX / zoom) - (int)scrollX;
        double cY = (rY / zoom) - (int)scrollY;

        return cX >= node.treeX() - 11 && cX <= node.treeX() + 11 &&
                cY >= node.treeY() - 11 && cY <= node.treeY() + 11;
    }

    public ResearchNode getHoveredNode() { return hoveredNode; }




    public boolean mouseDragged(double dX, double dY, int btn) {
        if (btn == 0) {
            this.scrollX += dX / zoom;
            this.scrollY += dY / zoom;
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double amount) {
        this.zoom = Mth.clamp(this.zoom + (float) amount * 0.1f, 0.5f, 2.0f);
        return true;
    }
}