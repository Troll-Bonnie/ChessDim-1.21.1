package net.baphy.chessdim.item;

import net.baphy.chessdim.registry.ModDimensions;
import net.baphy.chessdim.attachment.PlayerDimData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class ChessKeyItem extends Item {
    public ChessKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResultHolder.success(player.getItemInHand(hand));

        ServerPlayer serverPlayer = (ServerPlayer) player;
        ServerLevel currentLevel = (ServerLevel) level;

        if (currentLevel.dimension().equals(ModDimensions.CHESS_WORLD_KEY)) {
            PlayerDimData.saveChessDataAndReturn(serverPlayer);
        } else {
            ServerLevel chessLevel = serverPlayer.getServer().getLevel(ModDimensions.CHESS_WORLD_KEY);
            if (chessLevel == null) {
                player.sendSystemMessage(Component.literal("§cError: no dimension!"));
                return InteractionResultHolder.fail(player.getItemInHand(hand));
            }
            PlayerDimData.saveOverworldDataAndTeleport(serverPlayer, chessLevel);
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
