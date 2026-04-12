package net.futurepack.research;

import net.futurepack.registry.AttachmentRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import java.util.HashSet;
import java.util.Set;

public class ResearchHelper {

    private static String lastReadId = null;

    public static void setLastReadId(String id) { lastReadId = id; }
    public static String getLastReadId() { return lastReadId; }


    // Вызывать на сервере, когда нужно что-то изучить/открыть
    public static void updateProgress(Player player, String id, ProgressType type) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        // 1. Берем текущие данные
        var data = serverPlayer.getData(AttachmentRegistry.RESEARCH_DATA);

        // 2. Копируем текущие списки (создаем новые, чтобы не менять старый рекорд)
        Set<String> completed = new HashSet<>(data.completedIds());
        Set<String> revealed = new HashSet<>(data.revealedIds());
        Set<String> read = new HashSet<>(data.readIds());

        // 3. Выбираем, что менять
        boolean changed = false;
        switch (type) {
            case COMPLETE -> changed = completed.add(id);
            case REVEAL  -> changed = revealed.add(id);
            case READ    -> changed = read.add(id);
        }

        // 4. Если реально что-то изменилось — ПРИМЕНЯЕМ
        if (changed) {
            serverPlayer.setData(AttachmentRegistry.RESEARCH_DATA,
                    new PlayerResearch(completed, revealed, read));
        }
    }

    public enum ProgressType {
        REVEAL, COMPLETE, READ
    }
}