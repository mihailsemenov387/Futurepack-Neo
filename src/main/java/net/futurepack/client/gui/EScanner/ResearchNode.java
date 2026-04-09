package net.futurepack.client.gui.EScanner;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public record ResearchNode(
        String id,
        String tabId,              // К какой вкладке относится
        Component title,
        Component description,
        ItemStack iconStack,
        int treeX, int treeY,
        List<String> parents,       // Для линий
        List<String> requirements,  // Для логики (могут быть из других вкладок)
        boolean isHidden,           // Скрыто до сканирования
        UnlockType unlockType,      // Как завершается
        PageType pageType           // Что внутри
) {
    public enum Status { HIDDEN, LOCKED, AVAILABLE, COMPLETED }
    public enum UnlockType { ACCEPT, MACHINE, INSTANT }
    public enum PageType { TEXT, IMAGE }
}