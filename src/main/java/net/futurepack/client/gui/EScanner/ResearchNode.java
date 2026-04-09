package net.futurepack.client.gui.EScanner;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public record ResearchNode(
        String id,
        Component title,
        Component description,
        ItemStack iconStack,
        int treeX,
        int treeY,
        List<String> parents,
        boolean isHidden // true - невидима до сканирования, false - видна сразу
) {
    // Enum просто хранится тут для удобства
    public enum Status { HIDDEN, LOCKED, AVAILABLE, COMPLETED }
}