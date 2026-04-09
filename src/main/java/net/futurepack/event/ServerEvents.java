package net.futurepack.event;

import net.futurepack.registry.AttachmentRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class ServerEvents {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var data = player.getData(AttachmentRegistry.RESEARCH_DATA);
//            PacketDistributor.sendToPlayer(player,
//                    new SyncResearchPayload(data.completedIds(), data.revealedIds(), data.readIds()));
            System.out.println("DEBUG: Исследования игрока " + player.getName().getString() + " синхронизированы.");
        }
    }
}
