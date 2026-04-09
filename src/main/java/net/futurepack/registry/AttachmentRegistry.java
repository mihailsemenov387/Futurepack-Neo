package net.futurepack.registry;

import net.futurepack.FuturepackNeo;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashSet;
import java.util.function.Supplier;

public class AttachmentRegistry {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, FuturepackNeo.MODID);

    public static final Supplier<AttachmentType<net.futurepack.research.PlayerResearch>> RESEARCH_DATA = ATTACHMENT_TYPES.register(
            "research", () -> AttachmentType.builder(() -> new net.futurepack.research.PlayerResearch(new HashSet<>(), new HashSet<>()))
                    .serialize(net.futurepack.research.PlayerResearch.CODEC)
                    .copyOnDeath()
                    .build()
    );
}
