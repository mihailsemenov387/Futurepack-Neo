package net.futurepack.client.gui.EScanner;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class ResearchTabsScreen extends Screen {

    // --- ТЕКСТУРА САМОГО ОКНА ИССЛЕДОВАНИЙ ---
    private static final ResourceLocation BG_TEXTURE = ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/research_tabs_bg.png");

    // --- РАЗМЕРЫ ОКНА ---
    private static final int IMG_W = 220; // Чуть шире, чтобы влезли вкладки
    private static final int IMG_H = 235;

    // --- РАЗМЕРЫ ВНУТРЕННЕЙ ОБЛАСТИ (где будет рисоваться дерево) ---
    private static final int DISP_X_OFF = 30;
    private static final int DISP_Y_OFF = 20;
    private static final int DISP_W = 180;
    private static final int DISP_H = 195;

    private int leftPos, topPos;

    // --- СИСТЕМА ВКЛАДОК ---
    public enum Tab {
        SPACE("Космос", Items.ENDER_EYE),
        ADVENTURE("Приключения", Items.IRON_SWORD),
        MACHINES("Технологии", Items.REDSTONE);

        public final String title;
        public final ItemStack icon;

        Tab(String title, Item iconItem) {
            this.title = title;
            this.icon = new ItemStack(iconItem);
        }
    }

    private Tab currentTab = Tab.SPACE;

    // --- КОМПОНЕНТ ДЕРЕВА (Делегат) ---
    // Это тот самый отдельный класс, куда переедет логика нод, зума и линий
    private final TreeRenderComponent treeComponent;

    public ResearchTabsScreen() {
        super(Component.literal("Research Tabs"));

        // Создаем компонент-отрисовщик.
        // В будущем тут будет класс-генератор, который выдает данные в зависимости от вкладки.
        this.treeComponent = new TreeRenderComponent();
        this.treeComponent.loadDataForTab(this.currentTab);
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - IMG_W) / 2;
        this.topPos = (this.height - IMG_H) / 2;

        // Здесь можно добавить кнопку "Закрыть" или "Назад в главное меню"
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Убираем ванильный блюр
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 1. Темный фон мира
        guiGraphics.fill(0, 0, this.width, this.height, 0x99000000);

        // 2. Отрисовка подложки (корпуса)
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(BG_TEXTURE, this.leftPos, this.topPos, 0, 0, IMG_W, IMG_H);

        // 3. Отрисовка кнопок-вкладок (Табов)
        renderTabs(guiGraphics, mouseX, mouseY);

        // 4. Отрисовка самого дерева (вызываем ВНЕШНИЙ компонент)
        int dispX = this.leftPos + DISP_X_OFF;
        int dispY = this.topPos + DISP_Y_OFF;

        // Компонент сам решает, как рисовать линии и ноды, мы даем ему только координаты холста
        treeComponent.render(guiGraphics, dispX, dispY, DISP_W, DISP_H, mouseX, mouseY);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderTabs(GuiGraphics g, int mX, int mY) {
        // Рисуем вкладки слева от основного окна
        int tabX = this.leftPos - 28;
        int startY = this.topPos + 20;

        for (int i = 0; i < Tab.values().length; i++) {
            Tab tab = Tab.values()[i];
            boolean isSelected = (tab == currentTab);

            // Если вкладка выбрана, она чуть сдвигается вправо
            int currentX = isSelected ? tabX + 4 : tabX;
            int currentY = startY + (i * 32);

            // Цвета вкладки
            int bgColor = isSelected ? 0xFF00E5FF : 0xFF333333; // Бирюзовый для активной, серый для неактивной
            int borderColor = 0xFF000000;

            // Рисуем квадратик вкладки
            g.fill(currentX, currentY, currentX + 30, currentY + 30, borderColor);
            g.fill(currentX + 1, currentY + 1, currentX + 29, currentY + 29, bgColor);

            // Иконка
            g.renderItem(tab.icon, currentX + 6, currentY + 6);

            // Если мышка наведена на вкладку - рисуем тултип с названием
            if (mX >= currentX && mX <= currentX + 30 && mY >= currentY && mY <= currentY + 30) {
                g.renderTooltip(this.font, Component.literal(tab.title), mX, mY);
            }
        }
    }

    // --- ПРОБРОС СОБЫТИЙ В КОМПОНЕНТ ДЕРЕВА ---

    @Override
    public boolean mouseClicked(double mX, double mY, int btn) {
        // 1. Проверяем, не кликнули ли мы по вкладке
        int tabX = this.leftPos - 28;
        int startY = this.topPos + 20;

        for (int i = 0; i < Tab.values().length; i++) {
            int currentY = startY + (i * 32);
            int currentX = (Tab.values()[i] == currentTab) ? tabX + 4 : tabX;

            if (mX >= currentX && mX <= currentX + 30 && mY >= currentY && mY <= currentY + 30) {
                if (currentTab != Tab.values()[i]) {
                    currentTab = Tab.values()[i];
                    // ЗАГРУЖАЕМ ДАННЫЕ НОВОЙ ВКЛАДКИ
                    treeComponent.loadDataForTab(currentTab);
                }
                return true;
            }
        }

        // 2. Если клик не по вкладкам, передаем его компоненту дерева
        int dispX = this.leftPos + DISP_X_OFF;
        int dispY = this.topPos + DISP_Y_OFF;

        if (treeComponent.mouseClicked(mX, mY, btn, dispX, dispY, DISP_W, DISP_H)) {
            return true;
        }

        return super.mouseClicked(mX, mY, btn);
    }

    @Override
    public boolean mouseDragged(double mX, double mY, int btn, double dX, double dY) {
        if (treeComponent.mouseDragged(mX, mY, btn, dX, dY)) return true;
        return super.mouseDragged(mX, mY, btn, dX, dY);
    }

    @Override
    public boolean mouseScrolled(double mX, double mY, double sX, double sY) {
        if (treeComponent.mouseScrolled(mX, mY, sX, sY)) return true;
        return super.mouseScrolled(mX, mY, sX, sY);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}