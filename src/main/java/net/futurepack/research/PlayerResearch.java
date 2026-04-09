package net.futurepack.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public record PlayerResearch(Set<String> completedIds, Set<String> revealedIds, Set<String> readIds) {

    public static final Codec<PlayerResearch> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    // Кодек для списка "Изучено"
                    Codec.STRING.listOf().xmap(
                            list -> (Set<String>) new HashSet<>(list), // Из List в Set
                            set -> new ArrayList<>(set)               // Из Set в List
                    ).fieldOf("unlocked").forGetter(PlayerResearch::completedIds),

                    // Кодек для списка "Обнаружено"
                    Codec.STRING.listOf().xmap(
                            list -> (Set<String>) new HashSet<>(list), // Из List в Set
                            set -> new ArrayList<>(set)               // Из Set в List
                    ).fieldOf("revealed").forGetter(PlayerResearch::revealedIds),

                    // Codec for read
                    Codec.STRING.listOf().xmap(
                            list -> (Set<String>) new HashSet<>(list), // Из List в Set
                            set -> new ArrayList<>(set)               // Из Set в List
                    ).fieldOf("read").forGetter(PlayerResearch::readIds)

            ).apply(instance, PlayerResearch::new)
    );


    public static final StreamCodec<FriendlyByteBuf, PlayerResearch> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8), PlayerResearch::completedIds,
            ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8), PlayerResearch::revealedIds,
            ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8), PlayerResearch::readIds,
            PlayerResearch::new
    );


}