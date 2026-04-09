package net.futurepack.research;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public record ResearchTab(String id, Component title, ItemStack icon) {}