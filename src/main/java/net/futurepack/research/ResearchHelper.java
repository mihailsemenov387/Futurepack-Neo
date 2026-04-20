package net.futurepack.research;

import net.futurepack.registry.AttachmentRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;

public class ResearchHelper {

    private static String lastReadId = null;

    public static void setLastReadId(String id) { lastReadId = id; }
    public static String getLastReadId() { return lastReadId; }

      // Вызывать на сервере, когда нужно что-то изучить/открыть

//    public static void updateProgress(Player player, String id, ProgressType type) {
//        if (!(player instanceof ServerPlayer serverPlayer)) return;
//
//        ResearchNode node = ResearchRegistry.getNode(id);
//        if (node == null) return;
//
//        var data = serverPlayer.getData(net.futurepack.registry.AttachmentRegistry.RESEARCH_DATA);
//
//        // 1. ЗАПОМИНАЕМ, видел ли игрок эту вкладку ДО того, как мы выдали ему ноду
//        boolean wasTabVisible = isTabVisible(data, node.tabId());
//
//        Set<String> completed = new java.util.HashSet<>(data.completedIds());
//        Set<String> revealed = new java.util.HashSet<>(data.revealedIds());
//        Set<String> read = new java.util.HashSet<>(data.readIds());
//
//        boolean changed = false;
//        switch (type) {
//            case COMPLETE -> changed = completed.add(id);
//            case REVEAL  -> changed = revealed.add(id);
//            case READ    -> changed = read.add(id);
//        }
//
//        if (changed) {
//            PlayerResearch newData = new PlayerResearch(completed, revealed, read);
//            serverPlayer.setData(net.futurepack.registry.AttachmentRegistry.RESEARCH_DATA, newData);
//
//            // 2. ПРОВЕРЯЕМ, видит ли он вкладку ПОСЛЕ обновления
//            boolean isTabVisibleNow = isTabVisible(newData, node.tabId());
//
//            // 3. МАГИЯ: Если раньше не видел, а теперь видит — значит открылся новый раздел!
//            if (!wasTabVisible && isTabVisibleNow) {
//                net.futurepack.research.ResearchTab tab = ResearchRegistry.getTab(node.tabId());
//                if (tab != null) {
//                    serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
//                            "§6[E-Scanner]§f Расшифрован новый раздел данных: §e" + tab.title().getString()));
//
//                    // ЗВУК: Проигрываем торжественный звук (например, как при повышении уровня)
//                    serverPlayer.level().playSound(null, serverPlayer.blockPosition(),
//                            net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP,
//                            net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 1.0f);
//                }
//            }
//
//            // 4. Обычные сообщения об открытиях (не пишем при чтении, чтобы не спамить)
//            if (type == ProgressType.REVEAL) {
//                serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
//                        "§b[E-Scanner]§f Обнаружена аномалия: §3" + node.title().getString()));
//            } else if (type == ProgressType.COMPLETE) {
//                serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
//                        "§a[E-Scanner]§f Технология освоена: §2" + node.title().getString()));
//            }
//        }
//    }

    public static void updateProgress(Player player, String id, ProgressType type) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        ResearchNode node = ResearchRegistry.getNode(id);
        if (node == null) return;

        // 1. Берем текущие (старые) данные
        PlayerResearch oldData = serverPlayer.getData(AttachmentRegistry.RESEARCH_DATA);

        // 2. Копируем списки в новые HashSet (Record неизменяем, поэтому создаем новые копии)
        Set<String> completed = new HashSet<>(oldData.completedIds());
        Set<String> revealed = new HashSet<>(oldData.revealedIds());
        Set<String> read = new HashSet<>(oldData.readIds());

        boolean changed = false;
        switch (type) {
            case COMPLETE -> changed = completed.add(id);
            case REVEAL  -> changed = revealed.add(id);
            case READ    -> changed = read.add(id);
        }

        // 3. Если в данных реально что-то поменялось
        if (changed) {
            PlayerResearch newData = new PlayerResearch(completed, revealed, read);
            serverPlayer.setData(AttachmentRegistry.RESEARCH_DATA, newData);

            // 4. МАГИЯ ПОЯВЛЕНИЯ: Проверяем ВСЕ вкладки
            // Проходим циклом по всем табам, чтобы заметить открытие новых веток
            for (ResearchTab tab : ResearchRegistry.getTabs()) {
                boolean wasVisible = isTabVisible(oldData, tab.id());
                boolean isNowVisible = isTabVisible(newData, tab.id());

                // Если раньше вкладка была скрыта, а теперь стала доступна
                if (!wasVisible && isNowVisible) {
                    serverPlayer.sendSystemMessage(Component.literal(
                            "§6[E-Scanner]§f Расшифрован новый раздел данных: §e" + tab.title().getString()));

                    serverPlayer.level().playSound(null, serverPlayer.blockPosition(),
                            SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.0f);
                }
            }

            // 5. Обычные сообщения об открытии конкретной ноды
            if (type == ProgressType.REVEAL) {
                serverPlayer.sendSystemMessage(Component.literal(
                        "§b[E-Scanner]§f Обнаружена новое исследование: §3" + node.title().getString()));
            } else if (type == ProgressType.COMPLETE) {
                serverPlayer.sendSystemMessage(Component.literal(
                        "§a[E-Scanner]§f Технология освоена: §2" + node.title().getString()));
            }
        }
    }


    private static boolean isTabVisible(PlayerResearch data, String tabId) {
        for (ResearchNode n : ResearchRegistry.getNodesForTab(tabId)) {
            // 1. Если нода изучена - вкладка видна.
            if (data.completedIds().contains(n.id())) return true;

            // 2. Если требования выполнены...
            if (data.completedIds().containsAll(n.requirements())) {
                // ...и нода НЕ скрытая - вкладка видна сразу
                if (!n.isHidden()) return true;

                // ...а если скрытая - то только если её уже просканировали
                if (data.revealedIds().contains(n.id())) return true;
            }
        }
        return false;
    }

    public enum ProgressType {
        REVEAL, COMPLETE, READ
    }

    // TODO: new logic for default auto detection
    public static Component parseSmartText(String input) {
        if (input == null || input.isEmpty()) return Component.empty();

        // Префикс \T - принудительный текст
        if (input.startsWith("\\T")) {
            return Component.literal(input.substring(2).stripLeading());
        }

        // Префикс \L - принудительная локализация
        if (input.startsWith("\\L")) {
            return Component.translatable(input.substring(2).trim());
        }

        // Логика по умолчанию (авто-определение)
        if (input.contains(".")) {
            return Component.translatable(input);
        }
        return Component.literal(input);
    }
}