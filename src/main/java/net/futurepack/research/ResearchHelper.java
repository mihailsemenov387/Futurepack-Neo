package net.futurepack.research;

import net.futurepack.registry.AttachmentRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import java.util.HashSet;
import java.util.Set;

public class ResearchHelper {

    private static String lastReadId = null;

    public static void setLastReadId(String id) { lastReadId = id; }
    public static String getLastReadId() { return lastReadId; }


//    public static void updateProgress(Player player, String id, ProgressType type) {
//        if (!(player instanceof ServerPlayer serverPlayer)) return;
//
//        // 1. Берем текущие данные
//        var data = serverPlayer.getData(AttachmentRegistry.RESEARCH_DATA);
//
//        // 2. Копируем текущие списки (создаем новые, чтобы не менять старый рекорд)
//        Set<String> completed = new HashSet<>(data.completedIds());
//        Set<String> revealed = new HashSet<>(data.revealedIds());
//        Set<String> read = new HashSet<>(data.readIds());
//
//        // 3. Выбираем, что менять
//        boolean changed = false;
//        switch (type) {
//            case COMPLETE -> changed = completed.add(id);
//            case REVEAL  -> changed = revealed.add(id);
//            case READ    -> changed = read.add(id);
//        }
//
//        // 4. Если реально что-то изменилось — ПРИМЕНЯЕМ
//        if (changed) {
//            serverPlayer.setData(AttachmentRegistry.RESEARCH_DATA,
//                    new PlayerResearch(completed, revealed, read));
//
//            ResearchNode node = ResearchManager.getNode(id);
//            if (node != null) {
//                if (type == ProgressType.REVEAL) {
//                    // При обнаружении (например, сканером)
//                    serverPlayer.sendSystemMessage(Component.literal("§b[E-scanner]§f Обнаружены новые данные: §e" + node.title().getString()));
//                }
//                else if (type == ProgressType.COMPLETE) {
//                    // При полном изучении
//                    serverPlayer.sendSystemMessage(Component.literal("§b[E-scanner]§f Технология освоена: §a" + node.title().getString()));
//                }
//            }
//
//        }
//    }

      // Вызывать на сервере, когда нужно что-то изучить/открыть

    public static void updateProgress(Player player, String id, ProgressType type) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        ResearchNode node = ResearchManager.getNode(id);
        if (node == null) return;

        var data = serverPlayer.getData(net.futurepack.registry.AttachmentRegistry.RESEARCH_DATA);

        // 1. ЗАПОМИНАЕМ, видел ли игрок эту вкладку ДО того, как мы выдали ему ноду
        boolean wasTabVisible = isTabVisible(data, node.tabId());

        Set<String> completed = new java.util.HashSet<>(data.completedIds());
        Set<String> revealed = new java.util.HashSet<>(data.revealedIds());
        Set<String> read = new java.util.HashSet<>(data.readIds());

        boolean changed = false;
        switch (type) {
            case COMPLETE -> changed = completed.add(id);
            case REVEAL  -> changed = revealed.add(id);
            case READ    -> changed = read.add(id);
        }

        if (changed) {
            PlayerResearch newData = new PlayerResearch(completed, revealed, read);
            serverPlayer.setData(net.futurepack.registry.AttachmentRegistry.RESEARCH_DATA, newData);

            // 2. ПРОВЕРЯЕМ, видит ли он вкладку ПОСЛЕ обновления
            boolean isTabVisibleNow = isTabVisible(newData, node.tabId());

            // 3. МАГИЯ: Если раньше не видел, а теперь видит — значит открылся новый раздел!
            if (!wasTabVisible && isTabVisibleNow) {
                net.futurepack.research.ResearchTab tab = ResearchManager.getTab(node.tabId());
                if (tab != null) {
                    serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "§6[E-Scanner]§f Расшифрован новый раздел данных: §e" + tab.title().getString()));

                    // ЗВУК: Проигрываем торжественный звук (например, как при повышении уровня)
                    serverPlayer.level().playSound(null, serverPlayer.blockPosition(),
                            net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP,
                            net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 1.0f);
                }
            }

            // 4. Обычные сообщения об открытиях (не пишем при чтении, чтобы не спамить)
            if (type == ProgressType.REVEAL) {
                serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§b[E-Scanner]§f Обнаружена аномалия: §3" + node.title().getString()));
            } else if (type == ProgressType.COMPLETE) {
                serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§a[E-Scanner]§f Технология освоена: §2" + node.title().getString()));
            }
        }
    }

    private static boolean isTabVisible(PlayerResearch data, String tabId) {
        return ResearchManager.getNodesForTab(tabId).stream().anyMatch(n ->
                !n.isHidden() || data.revealedIds().contains(n.id()) || data.completedIds().contains(n.id())
        );
    }


    public enum ProgressType {
        REVEAL, COMPLETE, READ
    }
}