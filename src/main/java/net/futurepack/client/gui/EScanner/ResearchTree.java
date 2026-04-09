package net.futurepack.client.gui.EScanner;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;


public class ResearchTree extends Screen {

    public ResearchTree (){
        super(Component.literal("E-Scanner"));
        initNodes();

    }

    private void initNodes() {
        // Пример стартового дерева
        researches.put("start", new ResearchNode("start", Component.literal("Основы"),
                Component.literal("Начало технологического пути в Futurepack."),
                new ItemStack(Items.COMPASS), 0, 0, List.of(),false));

        researches.put("neon", new ResearchNode("neon", Component.literal("Неоновая Энергия"),
                Component.literal("Использование инертных газов для питания машин."),
                new ItemStack(Items.BEACON), -40, -60, List.of("start"),  false));

        researches.put("coil", new ResearchNode("coil", Component.literal("Катушка Теслы"),
                Component.literal("Беспроводная передача энергии."),
                new ItemStack(Items.LIGHTNING_ROD), 40, -60, List.of("start"),  false));

        researches.put("iron_tech", new ResearchNode("iron_tech", Component.literal("Катушка Теслы"),
                Component.literal("Беспроводная передача энергии."),
                new ItemStack(Items.OAK_BUTTON), -80, -140, List.of("coil"),  true));


    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - IMG_W) / 2;
        this.topPos = (this.height - IMG_H) / 2;

        this.backButton = Button.builder(Component.literal("Назад"), b -> {
            this.selectedEntry = null;
            b.visible = false;
        }).bounds(getDispX() + 2, getDispY() + 2, 40, 14).build();

        this.backButton.visible = false;
        this.addRenderableWidget(this.backButton);


        this.studyButton = Button.builder(Component.literal("Изучить"), b -> {
            if (selectedEntry != null) {
                // Шлем пакет на сервер, что мы изучили технологию
                sendCompletePacket(selectedEntry.id());
                b.active = false; // Выключаем кнопку после нажатия
            }
        }).bounds(getDispX() + 30, getDispY() + 140, 70, 20).build();

        this.studyButton.visible = false;
        this.addRenderableWidget(this.studyButton);
    }


}
