package net.futurepack.network;


import net.futurepack.registry.AttachmentRegistry;
import net.futurepack.research.ClientResearchState;
import net.futurepack.research.PlayerResearch;
import net.futurepack.research.ResearchHelper;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.HashSet;
import java.util.Set;

public class Networking {

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

//        registrar.playToClient(
//                SyncResearchPayload.TYPE,
//                SyncResearchPayload.STREAM_CODEC,
//                (payload, context) -> {
//                    context.enqueueWork(() -> {
//                        ClientResearchState.update(payload.completedIds(), payload.revealedIds());
//                    });
//                }
//        );

//        registrar.playToServer(
//                RequestCompletePayload.TYPE,
//                RequestCompletePayload.STREAM_CODEC,
//                (payload, context) -> context.enqueueWork(() -> {
//                    net.minecraft.server.level.ServerPlayer player = (net.minecraft.server.level.ServerPlayer) context.player();
//                    String id = payload.researchId();
//
//                    // --- ЛОГИКА НА СЕРВЕРЕ ---
//                    // В будущем здесь будет проверка ресурсов/машин.
//                    // Сейчас просто проверяем, есть ли такой ID в базе
//
//                    // Добавляем в список изученных
//                    var data = player.getData(AttachmentRegistry.RESEARCH_DATA);
//                    Set<String> newCompleted = new HashSet<>(data.completedIds());
//                    newCompleted.add(id);
//
//                    player.setData(AttachmentRegistry.RESEARCH_DATA, new PlayerResearch(data.revealedIds(), newCompleted, data.readIds()));
//                })
//        );

        registrar.playToServer(
                RequestCompletePayload.TYPE,
                RequestCompletePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    ResearchHelper.updateProgress(context.player(), payload.researchId(), ResearchHelper.ProgressType.COMPLETE);
                })
        );

        registrar.playToServer(
                RequestReadPayload.TYPE,
                RequestReadPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    ResearchHelper.updateProgress(context.player(), payload.readId(), ResearchHelper.ProgressType.READ);
                })
        );

    }
}