package net.baphy.chessdim.client;

import net.baphy.chessdim.item.CellBrushItem;
import net.baphy.chessdim.network.ModPackets;
import net.baphy.chessdim.registry.ModDimensions;
import net.baphy.chessdim.world.CellType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CellBrushClientHandler {
    public static final Set<ChunkPos> clientSelection = new HashSet<>();
    public static CellType clientType = CellType.DEFAULT;
    private static boolean isHolding = false;
    private static boolean wasHolding = false;
    private static boolean isRemoving = false;
    public static void onClientTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;
        if (!mc.level.dimension().equals(ModDimensions.CHESS_WORLD_KEY)){
            clientSelection.clear();
            return;
        }

        LocalPlayer player = mc.player;
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof CellBrushItem)) return;

        //mc.player.displayClientMessage(Component.nullToEmpty("isHolding: " + isHolding + " | wasHolding: " + wasHolding + " | isRemoving: " + isRemoving), true);
        HitResult hit = player.pick(100.0, 0, true);
        if (hit.getType() == HitResult.Type.BLOCK) {
            if(mc.options.keyUse.isDown() && !player.isShiftKeyDown()){
                isHolding = true;
                BlockPos blockPos = ((BlockHitResult) hit).getBlockPos();
                ChunkPos chunkPos = new ChunkPos(blockPos);
                if(!wasHolding)
                    isRemoving = clientSelection.contains(chunkPos);
                if (isRemoving)
                    clientSelection.remove(chunkPos);
                else
                    clientSelection.add(chunkPos);
            }
            else
                isHolding = false;
        }
        wasHolding = isHolding;
    }

    public static boolean onMouseScroll(double delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return false;
        if (!mc.level.dimension().equals(ModDimensions.CHESS_WORLD_KEY)) return false;
        if (!mc.player.isShiftKeyDown()) return false;

        ItemStack stack = mc.player.getMainHandItem();
        if (!(stack.getItem() instanceof CellBrushItem)) return false;

        clientType = delta > 0 ? clientType.next() : clientType.prev();
        mc.player.displayClientMessage(
                Component.translatable("item.chessdim.cell_brush.preset",
                        Component.translatable(clientType.getTranslationKey())
                                .withStyle(s -> s.withColor(clientType.getTextColor()))),
                true);

        return true;
    }

    public static void onLeftClick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!mc.level.dimension().equals(ModDimensions.CHESS_WORLD_KEY)) return;

        ItemStack stack = mc.player.getMainHandItem();
        if (!(stack.getItem() instanceof CellBrushItem)) return;
        applySelection(mc.player);
    }

    public static void applySelection(Player player) {
        //player.sendSystemMessage(Component.nullToEmpty("DEBUG: " + Arrays.toString(clientSelection.toArray())));
        if (clientSelection.isEmpty()) {
            player.displayClientMessage(Component.translatable("item.chessdim.cell_brush.nothing_selected").withColor(0xFF0000), true);
            return;
        }

        ModPackets.sendApplySelection(
                new ArrayList<>(clientSelection),
                clientType);

        clientSelection.clear();
        player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP,1.0f, 1.0f);
    }


}
