package net.futurepack.research;

import net.futurepack.registry.AttachmentRegistry;
import net.minecraft.client.Minecraft;

import java.util.List;


public class ClientResearchState {

    public static PlayerResearch getData() {
        if (Minecraft.getInstance().player == null) return null;
        return Minecraft.getInstance().player.getData(AttachmentRegistry.RESEARCH_DATA);
    }
    public static ResearchNode.Status getStatus(ResearchNode node) {
        if (node == null) return ResearchNode.Status.HIDDEN;

        var data = getData();

        if (data.completedIds().contains(node.id())) return ResearchNode.Status.COMPLETED;

        if (node.isHidden() && !data.revealedIds().contains(node.id())) return ResearchNode.Status.HIDDEN;

        boolean reqsMet = node.requirements().isEmpty() ||
                node.requirements().stream().allMatch(id -> data.completedIds().contains(id));

        return reqsMet ? ResearchNode.Status.AVAILABLE : ResearchNode.Status.LOCKED;
    }

    public static ResearchNode.Status getStatus(String nodeId) {
        return getStatus(ResearchManager.getNode(nodeId));
    }

    public static boolean isRead(String id) {
        var data = getData();
        if (data == null) return true;

        ResearchNode.Status status = getStatus(id);

        if (status == ResearchNode.Status.LOCKED || status == ResearchNode.Status.HIDDEN) {
            return true;
        }

        return data.readIds().contains(id) || data.completedIds().contains(id);
    }
    public static boolean isCompleted(String id) {
        return getData().completedIds().contains(id);
    }

    public static boolean isTabVisible(String tabId) {
        // Вкладка видна, если в ней есть ХОТЯ БЫ ОДНА нода, статус которой НЕ HIDDEN
        // (То есть она либо LOCKED, либо AVAILABLE, либо COMPLETED)

        return ResearchManager.getNodesForTab(tabId).stream()
                .anyMatch(node -> (getStatus(node) != ResearchNode.Status.HIDDEN && getStatus(node) != ResearchNode.Status.LOCKED) );
    }

    public static boolean hasUnreadInTab(String tabId) {
        return ResearchManager.getNodesForTab(tabId).stream().anyMatch(node -> {
            // Ищем ноду, которая ДОСТУПНА (Available), но НЕ ПРОЧИТАНА
            return getStatus(node) == ResearchNode.Status.AVAILABLE && !isRead(node.id());
        });
    }

}