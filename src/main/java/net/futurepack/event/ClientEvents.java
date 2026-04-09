package net.futurepack.event;

import net.futurepack.client.gui.EScanner.EScannerScreen;
import net.futurepack.client.gui.EScanner.EScannerScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import java.util.HashSet;

public class ClientEvents {

    @SubscribeEvent
    public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        EScannerScreen.updateClientProgress(new HashSet<>(), new HashSet<>());
        System.out.println("[Futurepack] Данные исследований успешно сброшены.");
    }
}
