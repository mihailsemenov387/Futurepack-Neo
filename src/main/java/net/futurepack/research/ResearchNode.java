package net.futurepack.research;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.futurepack.client.gui.EScanner.Entrys.IResearchPage; // Импортируем нашу страницу!
import java.util.List;

public record ResearchNode(
        String id,
        String tabId,
        Component title, // Оставили для тултипов в дереве
        ItemStack icon,
        ResourceLocation customIcon,
        int treeX, int treeY,
        List<String> links,
        List<String> requirements,
        boolean isHidden,
        NodeFrameType frame// Логика отрисовки внутреннего контента

) {
    public enum Status { HIDDEN, LOCKED, AVAILABLE, COMPLETED }
    public enum NodeFrameType {
        HEXAGON,
        GOLDEN,
        ERK
    }

    public static ResourceLocation getFramePathByType(NodeFrameType type) {
        return switch (type) {
            case GOLDEN -> ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/slot_so_bg.png");
            case HEXAGON -> ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/slot_bg.png");
            default -> ResourceLocation.fromNamespaceAndPath("futurepack", "textures/gui/slot_erk_bg.png");
        };
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchNode> STREAM_CODEC = StreamCodec.of(
            (buf, node) -> {
                // ПОРЯДОК ЗАПИСИ (должен совпадать с порядком чтения!)
                ByteBufCodecs.STRING_UTF8.encode(buf, node.id());
                ByteBufCodecs.STRING_UTF8.encode(buf, node.tabId());
                ComponentSerialization.STREAM_CODEC.encode(buf, node.title());
                ItemStack.STREAM_CODEC.encode(buf, node.icon());
                ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).encode(buf, java.util.Optional.ofNullable(node.customIcon()));
                ByteBufCodecs.VAR_INT.encode(buf, node.treeX());
                ByteBufCodecs.VAR_INT.encode(buf, node.treeY());
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).encode(buf, node.links());
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).encode(buf, node.requirements());
                ByteBufCodecs.BOOL.encode(buf, node.isHidden());
                ByteBufCodecs.VAR_INT.encode(buf, node.frame().ordinal());
            },
            (buf) -> {
                // ПОРЯДОК ЧТЕНИЯ
                String id = ByteBufCodecs.STRING_UTF8.decode(buf);
                String tabId = ByteBufCodecs.STRING_UTF8.decode(buf);
                Component title = ComponentSerialization.STREAM_CODEC.decode(buf);
                ItemStack icon = ItemStack.STREAM_CODEC.decode(buf);
                ResourceLocation customIcon = ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).decode(buf).orElse(null);
                int x = ByteBufCodecs.VAR_INT.decode(buf);
                int y = ByteBufCodecs.VAR_INT.decode(buf);
                List<String> links = ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).decode(buf);
                List<String> requirements = ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).decode(buf);
                boolean hidden = ByteBufCodecs.BOOL.decode(buf);
                NodeFrameType frame = NodeFrameType.values()[ByteBufCodecs.VAR_INT.decode(buf)];

                return new ResearchNode(id, tabId, title, icon, customIcon, x, y, links, requirements, hidden, frame);
            }
    );


}