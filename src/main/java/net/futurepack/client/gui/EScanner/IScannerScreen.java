package net.futurepack.client.gui.EScanner;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public interface IScannerScreen {
    <T extends GuiEventListener & Renderable & NarratableEntry> T addWidgetPublic(T widget);

    void removeWidgetPublic(GuiEventListener widget);
}