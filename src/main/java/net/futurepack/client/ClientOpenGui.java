package net.futurepack.client;

import net.futurepack.client.gui.EScanner.EScannerMainScreen;
import net.minecraft.client.Minecraft;

public class ClientOpenGui {
    public static void openMainScreen() {
        Minecraft.getInstance().setScreen(new EScannerMainScreen());
    }
}