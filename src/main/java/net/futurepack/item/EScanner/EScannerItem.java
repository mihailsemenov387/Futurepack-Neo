package net.futurepack.item.EScanner;

import net.futurepack.client.gui.EScanner.EScannerMainScreen;
import net.minecraft.core.BlockPos;
import net.futurepack.network.SyncResearchPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.client.Minecraft;
//import net.futurepack.client.gui.EScanner.EScannerScreen;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

import static net.futurepack.registry.AttachmentRegistry.RESEARCH_DATA;

public class EScannerItem extends Item {

    public EScannerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (!level.isClientSide && player != null) {
            if (state.is(Blocks.IRON_BLOCK)) {
                revealResearch(player, "iron_tech");
                player.sendSystemMessage(Component.literal("§b[E-Scanner]§f Железо просканировано!"));
                return InteractionResult.SUCCESS;
            } else {
                player.sendSystemMessage(Component.literal("§c[E-Scanner]§f Этот блок не содержит данных."));
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            openGui();
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

//    private void openGui() {
//        Minecraft.getInstance().setScreen(new EScannerScreen());
//    }

    private void openGui() {
        Minecraft.getInstance().setScreen(new EScannerMainScreen());
    }


    private void completeResearch(Player player, String id) {
        var data = player.getData(RESEARCH_DATA);

        data.completedIds().add(id);

        PacketDistributor.sendToPlayer(
                (ServerPlayer) player,
                new SyncResearchPayload(data.completedIds(), data.revealedIds())
        );

        System.out.println("DEBUG: research " + id + " sent to: " + player.getName().getString());
    }

    private void revealResearch(Player player, String id) {
        var data = player.getData(RESEARCH_DATA);

        // Добавляем в список "ОБНАРУЖЕНО"
        if (data.revealedIds().add(id)) {
            // Синхронизируем с клиентом
            PacketDistributor.sendToPlayer((ServerPlayer) player,
                    new SyncResearchPayload(data.completedIds(), data.revealedIds()));

            player.sendSystemMessage(Component.literal("§b[E-Scanner]§f Чертеж технологии получен!"));
        }
    }


}