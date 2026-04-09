package net.futurepack.research;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.futurepack.registry.AttachmentRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;

public class ResearchCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("research")
                .requires(source -> source.hasPermission(2)) // Только для админов (OP)

                // 1. Команда REVEAL (показать ноду)
                .then(Commands.literal("reveal")
                        .then(Commands.argument("nodeId", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(ResearchManager.getAllIds(), builder))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> update(ctx, ResearchHelper.ProgressType.REVEAL)))
                                .executes(ctx -> update(ctx, ResearchHelper.ProgressType.REVEAL))
                        )
                )

                // 2. Команда COMPLETE (завершить)
                .then(Commands.literal("complete")
                        .then(Commands.argument("nodeId", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(ResearchManager.getAllIds(), builder))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> update(ctx, ResearchHelper.ProgressType.COMPLETE)))
                                .executes(ctx -> update(ctx, ResearchHelper.ProgressType.COMPLETE))
                        )
                )

                // 3. Команда RESET (полный сброс прогресса)
                .then(Commands.literal("reset")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ResearchCommand::reset))
                        .executes(ResearchCommand::reset)
                )
        );
    }

    private static int update(CommandContext<CommandSourceStack> ctx, ResearchHelper.ProgressType type) {
        try {
            String nodeId = StringArgumentType.getString(ctx, "nodeId");
            ServerPlayer target;
            try {
                target = EntityArgument.getPlayer(ctx, "player");
            } catch (Exception e) {
                target = ctx.getSource().getPlayerOrException();
            }

            // --- ФИКС ТУТ: Создаем финальную копию для лямбды ---
            final ServerPlayer finalTarget = target;

            ResearchHelper.updateProgress(finalTarget, nodeId, type);

            // Теперь используем finalTarget
            ctx.getSource().sendSuccess(() -> Component.literal("§b[Futurepack]§f Исследование " + nodeId + " обновлено для " + finalTarget.getName().getString()), true);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("Ошибка выполнения команды."));
            return 0;
        }
    }

    private static int reset(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer target;
            try {
                target = EntityArgument.getPlayer(ctx, "player");
            } catch (Exception e) {
                target = ctx.getSource().getPlayerOrException();
            }

            // --- ФИКС ТУТ: Создаем финальную копию ---
            final ServerPlayer finalTarget = target;

            finalTarget.setData(AttachmentRegistry.RESEARCH_DATA, new PlayerResearch(new HashSet<>(), new HashSet<>(), new HashSet<>()));

            ctx.getSource().sendSuccess(() -> Component.literal("§b[Futurepack]§f Прогресс игрока " + finalTarget.getName().getString() + " сброшен!"), true);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
}