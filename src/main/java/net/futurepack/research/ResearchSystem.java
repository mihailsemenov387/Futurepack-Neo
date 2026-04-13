package net.futurepack.research;

import com.google.gson.JsonObject;
import net.futurepack.client.PageDictionary;
import net.futurepack.client.gui.EScanner.Entrys.IResearchPage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import java.util.List;
import java.util.function.Supplier;

public class ResearchSystem {

    public static class NodeBuilder {
        private final String id;
        private String tabId;
        private Component title;
        private ItemStack icon;
        private ResourceLocation customIcon;
        private int x, y;
        private List<String> links = List.of();
        private List<String> requirements = List.of();
        private boolean isHidden;
        private ResearchNode.NodeFrameType frame;

        public NodeBuilder(String id) {
            this.id = id;
            this.title = Component.literal(id);
        }

        public NodeBuilder tab(String tabId) { this.tabId = tabId; return this; }
        public NodeBuilder title(String name) { this.title = Component.literal(name); return this; }
        public NodeBuilder icon(Item item) { this.icon = new ItemStack(item); return this; }
        public NodeBuilder pos(int x, int y) { this.x = x; this.y = y; return this; }
        public NodeBuilder customIcon(ResourceLocation customIcon){ this.customIcon = customIcon; return this;}

        public NodeBuilder frame(ResearchNode.NodeFrameType frame) {
            this.frame = frame;
            return this;
        }

        public NodeBuilder hidden(boolean hidden) {
            this.isHidden = hidden;
            return this;
        }

        public NodeBuilder parents(String... ids) {
            this.links = List.of(ids);

            return this;
        }

        public NodeBuilder requirements(String... ids) {
            this.requirements = List.of(ids);

            return this;
        }


        // Настройка страницы (ТОЛЬКО КЛИЕНТ)
        public NodeBuilder page(Supplier<IResearchPage> pageSupplier) {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                PageDictionary.registerPage(this.id, pageSupplier);
            }
            return this;
        }

        public NodeBuilder page(String type, JsonObject data) {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                PageDictionary.registerPageFromJson(this.id, type, data);
            }
            return this;
        }



        public void build() {
            if (ResearchRegistry.getNode(this.id) != null) {
                throw new IllegalStateException("Research ID '" + this.id + "' already exists!");
            }
            ResearchNode node = new ResearchNode(id, tabId, title, icon, customIcon, x, y, links, requirements, isHidden, frame);

            ResearchRegistry.registerNodeViaAPI(node);
        }
    }



    public static NodeBuilder create(String id) {
        return new NodeBuilder(id);
    }
}