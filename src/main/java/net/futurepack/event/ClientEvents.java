package net.futurepack.event;

//import net.futurepack.client.gui.EScanner.EScannerScreen;
import net.futurepack.client.gui.EScanner.ResearchTabsScreen;
import net.futurepack.research.ClientResearchState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import java.util.HashSet;

public class ClientEvents {

    @SubscribeEvent
    public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
//        ClientResearchState.clear();
//        System.out.println("[Futurepack] Данные исследований успешно сброшены.");
    }
}
