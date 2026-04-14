package net.futurepack.research;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record ResearchTab(String id, Component title, ItemStack icon, ResourceLocation tab_bg, ResourceLocation custom_icon) {

    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchTab> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ResearchTab::id,
            ComponentSerialization.STREAM_CODEC, ResearchTab::title,
            ItemStack.STREAM_CODEC, ResearchTab::icon,
            ResourceLocation.STREAM_CODEC, ResearchTab::tab_bg,
            ResourceLocation.STREAM_CODEC, ResearchTab::custom_icon,
            ResearchTab::new
    );


}
