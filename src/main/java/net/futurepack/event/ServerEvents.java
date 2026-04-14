package net.futurepack.event;

import net.futurepack.FuturepackNeo;
import net.futurepack.client.PageDictionary;
import net.futurepack.network.SyncResearchPayload;
import net.futurepack.registry.AttachmentRegistry;
import net.futurepack.research.ResearchNode;
import net.futurepack.research.ResearchRegistry;
import net.futurepack.research.ResearchTab;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerEvents {

//    @SubscribeEvent
//    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
//        if (event.getEntity() instanceof ServerPlayer player) {
//            var data = player.getData(AttachmentRegistry.RESEARCH_DATA);
//            PacketDistributor.sendToPlayer(player,
//                    new SyncResearchPayload(data.completedIds(), data.revealedIds(), data.readIds()));
//            System.out.println("Hello from server");
//        }
//    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 1. Собираем все данные из реестров сервера
            List<ResearchTab> tabs = new ArrayList<>(ResearchRegistry.getTabs());
            List<ResearchNode> nodes = new ArrayList<>(ResearchRegistry.getNodes());
            Map<String, SyncResearchPayload.PageEntry> pages = PageDictionary.getRawDataForSync();

            // 2. Отправляем пакет именно этому игроку
            PacketDistributor.sendToPlayer(player, new SyncResearchPayload(tabs, nodes, pages));

            FuturepackNeo.LOGGER.info("Futurepack: Sent initial sync packet to " + player.getName().getString());
        }
    }
}
