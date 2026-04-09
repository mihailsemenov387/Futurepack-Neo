package net.futurepack.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public record PlayerResearch(Set<String> completedIds, Set<String> revealedIds) {

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
                    ).fieldOf("revealed").forGetter(PlayerResearch::revealedIds)

            ).apply(instance, PlayerResearch::new)
    );

    public boolean isUnlocked(String id) {
        return completedIds.contains(id);
    }

    public boolean isRevealed(String id) {
        return revealedIds.contains(id);
    }
}