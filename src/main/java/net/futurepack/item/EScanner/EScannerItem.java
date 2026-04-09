package net.futurepack.item.EScanner;

import net.futurepack.client.gui.EScanner.EScannerMainScreen;
import net.futurepack.research.ResearchHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class EScannerItem extends Item {

    public EScannerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockState state = level.getBlockState(context.getClickedPos());

        if (player == null) return InteractionResult.PASS;

        if (!level.isClientSide) {
            if (state.is(Blocks.IRON_BLOCK)) {
                ResearchHelper.updateProgress(player, "iron_tech", ResearchHelper.ProgressType.REVEAL);
                ResearchHelper.updateProgress(player, "alien_tech", ResearchHelper.ProgressType.REVEAL);

                player.sendSystemMessage(Component.literal("§b[E-Scanner]§f Данные получены: Железо и НЛО"));
                return InteractionResult.SUCCESS;
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

    private void openGui() {
        Minecraft.getInstance().setScreen(new EScannerMainScreen());
    }
}