package net.baphy.chessdim.client;

import net.baphy.chessdim.item.CellBrushItem;
import net.baphy.chessdim.network.ModPackets;
import net.baphy.chessdim.registry.ModDimensions;
import net.baphy.chessdim.world.CellType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CellBrushClientHandler {
    private static int holdTickCounter = 0;
    public static final Set<ChunkPos> clientSelection = new HashSet<>();
    private static CellType clientType = CellType.DEFAULT;

    public static void onClientTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;
        if (!mc.level.dimension().equals(ModDimensions.CHESS_WORLD_KEY)){
            clientSelection.clear();
            return;
        }

        LocalPlayer player = mc.player;
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof CellBrushItem)) return;

        spawnSelectionParticles(mc, stack);
        holdTickCounter++;
        if (holdTickCounter >= 5) {
            holdTickCounter = 0;
            HitResult hit = player.pick(100.0, 0, true);
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = ((BlockHitResult) hit).getBlockPos();
                ChunkPos chunkPos = new ChunkPos(blockPos);
                if (mc.options.keyAttack.isDown() && !player.isShiftKeyDown()) {
                    clientSelection.remove(chunkPos);
                }
                else if (mc.options.keyUse.isDown() && !player.isShiftKeyDown()) {
                    clientSelection.add(chunkPos);
                }
            }
        }
    }

    private static void spawnSelectionParticles(Minecraft mc, ItemStack stack) {
        if (clientSelection.isEmpty()) return;
        int color = clientType.getParticleColor();

        for (ChunkPos pos : clientSelection) {
            if(!clientSelection.contains(new ChunkPos(pos.x, pos.z-1)))
                spawnParticleLine(mc, pos, 'n', color);
            if(!clientSelection.contains(new ChunkPos(pos.x, pos.z+1)))
                spawnParticleLine(mc, pos, 's', color);
            if(!clientSelection.contains(new ChunkPos(pos.x-1, pos.z)))
                spawnParticleLine(mc, pos, 'w', color);
            if(!clientSelection.contains(new ChunkPos(pos.x+1, pos.z)))
                spawnParticleLine(mc, pos, 'e', color);

        }

    }

    private static void spawnParticleLine(Minecraft mc, ChunkPos pos, char side, int color){
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8)  & 0xFF) / 255f;
        float b = (color         & 0xFF) / 255f;
        double intervalL = 0.2;
        double intervalWH = 0.2;
        int thickness = 1;
        double minX = (double)pos.getMinBlockX();
        double minZ = (double)pos.getMinBlockZ();
        double maxX = (double)pos.getMaxBlockX()+1.0;
        double maxZ = (double)pos.getMaxBlockZ()+1.0;
        double y = 1.5;
        switch (side){
            case 'n':
                for (int i = -thickness; i <= thickness; i++)
                    for (int j = -thickness; j <= thickness; j++)
                        for (double l = minX; l <= maxX; l+=intervalL)
                            spawnParticle(mc, l, y + i*intervalWH, minZ + j*intervalWH, r, g, b);
                break;
            case 's':
                for (int i = -thickness; i <= thickness; i++)
                    for (int j = -thickness; j <= thickness; j++)
                        for (double l = minX; l <= maxX; l+=intervalL)
                            spawnParticle(mc, l, y + i*intervalWH, maxZ + j*intervalWH, r, g, b);
                break;
            case 'w':
                for (int i = -thickness; i <= thickness; i++)
                    for (int j = -thickness; j <= thickness; j++)
                        for (double l = minZ; l <= maxZ; l+=intervalL)
                            spawnParticle(mc, minX + j*intervalWH, y + i*intervalWH, l, r, g, b);
                break;
            case 'e':
                for (int i = -thickness; i <= thickness; i++)
                    for (int j = -thickness; j <= thickness; j++)
                        for (double l = minZ; l <= maxZ; l+=intervalL)
                            spawnParticle(mc, maxX + j*intervalWH, y + i*intervalWH, l, r, g, b);
                break;
        }
    }

    private static void spawnParticle(Minecraft mc, double x, double y, double z,
                                      float r, float g, float b) {
        mc.level.addParticle(
                new net.minecraft.core.particles.DustParticleOptions(
                        new org.joml.Vector3f(r, g, b), 1.0f),
                x, y, z,
                0, 0, 0);
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

        HitResult hit = mc.player.pick(100.0, 0, false);
        if (hit.getType() != HitResult.Type.BLOCK) return;

        BlockPos blockPos = ((BlockHitResult) hit).getBlockPos();
        ChunkPos chunkPos = new ChunkPos(blockPos);

       clientSelection.remove(chunkPos);
    }

    public static void applySelection(Player player) {
        if (clientSelection.isEmpty()) {
            player.sendSystemMessage(
                    Component.translatable("item.chessdim.cell_brush.nothing_selected"));
            return;
        }

        ModPackets.sendApplySelection(
                new ArrayList<>(clientSelection),
                clientType);
        player.sendSystemMessage(
                (Component.translatable("item.chessdim.cell_brush.preset",
                                Component.translatable(clientType.getTranslationKey()), clientSelection.size())));


        clientSelection.clear();
    }
}
