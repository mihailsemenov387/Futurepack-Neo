package net.futurepack.research;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public record ResearchNode(
        String id,
        String tabId,             // К какому табу принадлежит
        Component title,
        Component description,
        ItemStack icon,
        int treeX, int treeY,
        List<String> parents,      // Для отрисовки линий
        List<String> requirements, // Что реально нужно изучить (может быть из других табов)
        boolean isHidden           // Скрыто ли до сканирования
) {
    public enum Status { HIDDEN, LOCKED, AVAILABLE, COMPLETED }
}