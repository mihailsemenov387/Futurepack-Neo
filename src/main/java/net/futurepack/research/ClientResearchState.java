package net.futurepack.research;

import java.util.HashSet;
import java.util.Set;

public class ClientResearchState {
    private static final Set<String> COMPLETED = new HashSet<>();
    private static final Set<String> REVEALED = new HashSet<>();

    // Вызывается из пакета (SyncResearchPayload)
    public static void update(Set<String> completed, Set<String> revealed) {
        COMPLETED.clear(); COMPLETED.addAll(completed);
        REVEALED.clear();  REVEALED.addAll(revealed);
    }

    // Вызывается при выходе из мира (в ClientEvents)
    public static void clear() {
        COMPLETED.clear();
        REVEALED.clear();
    }

    // УМНАЯ ЛОГИКА СТАТУСА (Отвязана от GUI!)
    public static ResearchNode.Status getStatus(ResearchNode node) {
        if (node == null) return ResearchNode.Status.HIDDEN;

        // 1. Проверяем, изучено ли (Completed)
        if (COMPLETED.contains(node.id())) {
            return ResearchNode.Status.COMPLETED;
        }

        // 2. Проверяем, скрыто ли (Hidden)
        // Используем данные напрямую из объекта node
        if (node.isHidden() && !REVEALED.contains(node.id())) {
            return ResearchNode.Status.HIDDEN;
        }

        // 3. Проверяем требования (Requirements)
        boolean reqsMet = node.requirements().isEmpty() ||
                node.requirements().stream().allMatch(COMPLETED::contains);

        return reqsMet ? ResearchNode.Status.AVAILABLE : ResearchNode.Status.LOCKED;
    }


    public static ResearchNode.Status getStatus(String nodeId) {
        return getStatus(ResearchManager.getNode(nodeId));
    }
}