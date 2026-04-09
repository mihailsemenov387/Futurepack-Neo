package net.futurepack.network;


import net.futurepack.client.gui.EScanner.EScannerScreen;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class Networking {

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                SyncResearchPayload.TYPE,
                SyncResearchPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        EScannerScreen.updateClientProgress(payload.completedIds(), payload.revealedIds());
                    });
                }
        );

        registrar.playToServer(
                RequestCompletePayload.TYPE,
                RequestCompletePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    net.minecraft.server.level.ServerPlayer player = (net.minecraft.server.level.ServerPlayer) context.player();
                    String id = payload.researchId();

                    // --- ЛОГИКА НА СЕРВЕРЕ ---
                    // В будущем здесь будет проверка ресурсов/машин.
                    // Сейчас просто проверяем, есть ли такой ID в базе
                    var data = player.getData(net.futurepack.registry.AttachmentRegistry.RESEARCH_DATA);

                    // Добавляем в список изученных
                    if (data.completedIds().add(id)) {
                        // Отправляем обратно пакет синхронизации, чтобы клиент узнал об успехе
                        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                                new SyncResearchPayload(data.completedIds(), data.revealedIds()));
                    }
                })
        );
    }
}