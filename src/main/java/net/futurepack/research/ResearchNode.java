package net.futurepack.research;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.futurepack.client.gui.EScanner.Entrys.IResearchPage; // Импортируем нашу страницу!
import java.util.List;

public record ResearchNode(
        String id,
        String tabId,
        Component title, // Оставили для тултипов в дереве
        ItemStack icon,
        ResourceLocation customIcon,
        int treeX, int treeY,
        List<String> links,
        List<String> requirements,
        boolean isHidden,
        IResearchPage page,
        NodeFrameType frame// Логика отрисовки внутреннего контента

) {
    public enum Status { HIDDEN, LOCKED, AVAILABLE, COMPLETED }
    public enum NodeFrameType {
        HEXAGON,
        GOLDEN,
        ERK
    }

    public static ResourceLocation getFramePathByType(NodeFrameType type) {
        return switch (type) {
            case GOLDEN -> ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/slot_so_bg.png");
            case HEXAGON -> ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/slot_bg.png");
            default -> ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/slot_erk_bg.png");
        };
    }

}