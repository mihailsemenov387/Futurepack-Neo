//package net.futurepack.research;
//
//import net.minecraft.network.chat.Component;
//import net.minecraft.world.item.ItemStack;
//import java.util.List;
//
//public record ResearchNode(
//        String id,
//        String tabId,             // К какому табу принадлежит
//        Component title,
//        Component description,
//        ItemStack icon,
//        int treeX, int treeY,
//        List<String> parents,      // Для отрисовки линий
//        List<String> requirements, // Что реально нужно изучить (может быть из других табов)
//        boolean isHidden           // Скрыто ли до сканирования
//) {
//    public enum Status { HIDDEN, LOCKED, AVAILABLE, COMPLETED }
//}


package net.futurepack.research;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.futurepack.client.gui.EScanner.Entrys.IResearchPage; // Импортируем нашу страницу!
import java.util.List;

public record ResearchNode(
        String id,
        String tabId,
        Component title, // Оставили для тултипов в дереве
        ItemStack icon,
        int treeX, int treeY,
        List<String> parents,
        List<String> requirements,
        boolean isHidden,
        IResearchPage page // Логика отрисовки внутреннего контента
) {
    public enum Status { HIDDEN, LOCKED, AVAILABLE, COMPLETED }
}