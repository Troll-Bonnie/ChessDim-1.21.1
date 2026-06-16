package net.baphy.schematicon.item;

import net.baphy.schematicon.registry.ModDimensions;
import net.baphy.schematicon.attachment.PlayerDimData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ChessKeyItem extends Item {
    public ChessKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (level.isClientSide()) return InteractionResultHolder.success(player.getItemInHand(hand));

        ServerPlayer serverPlayer = (ServerPlayer) player;
        ServerLevel currentLevel = (ServerLevel) level;

        if (currentLevel.dimension().equals(ModDimensions.SCHEMATICON_KEY)) {
            PlayerDimData.saveSchemDataAndReturn(serverPlayer);
        } else {
            ServerLevel chessLevel = Objects.requireNonNull(serverPlayer.getServer()).getLevel(ModDimensions.SCHEMATICON_KEY);
            if (chessLevel == null) {
                player.sendSystemMessage(Component.literal("§cError: no dimension!"));
                return InteractionResultHolder.fail(player.getItemInHand(hand));
            }
            PlayerDimData.saveOverworldDataAndTeleport(serverPlayer, chessLevel);
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
