package net.futurepack;

import net.futurepack.client.PageDictionary;
import net.futurepack.event.ClientEvents;
import net.futurepack.event.ServerEvents;
import net.futurepack.network.Networking;
import net.futurepack.registry.AttachmentRegistry;
import net.futurepack.registry.ItemRegistry;
import net.futurepack.research.ResearchManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.futurepack.research.ResearchCommand;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(FuturepackNeo.MODID)
public class FuturepackNeo {
    public static final String MODID = "futurepack";
    public static final Logger LOGGER = LogUtils.getLogger();


    public FuturepackNeo(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        ResearchManager.init();

        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> {
            ResearchCommand.register(event.getDispatcher());
        });

        ItemRegistry.ITEMS.register(modEventBus);
        AttachmentRegistry.ATTACHMENT_TYPES.register(modEventBus);

        modEventBus.addListener(Networking::register);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);


        NeoForge.EVENT_BUS.register(ServerEvents.class);

        // 2. Регистрация клиентских событий ТОЛЬКО на клиенте
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.register(ClientEvents.class);
        }

        if (FMLEnvironment.dist == Dist.CLIENT) {
            // 2. Явно подписываем метод словаря на шину мода (MOD bus)
            // Это заменяет @EventBusSubscriber и bus = MOD
            modEventBus.addListener(PageDictionary::onClientSetup);
        }

    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }


//    @SubscribeEvent
//    public void onServerStarting(ServerStartingEvent event) {
//        LOGGER.info("HELLO from server starting");
//    }
}
