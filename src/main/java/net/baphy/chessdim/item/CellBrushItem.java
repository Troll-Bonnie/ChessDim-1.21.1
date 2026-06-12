package net.baphy.chessdim.item;

import net.baphy.chessdim.client.CellBrushClientHandler;
import net.baphy.chessdim.registry.ModDimensions;
import net.baphy.chessdim.world.CellType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.baphy.chessdim.world.ChessChunkGenerator.*;
import static net.baphy.chessdim.world.ChessChunkGenerator.getFloorMinY;

public class CellBrushItem extends Item {

    public CellBrushItem(Properties properties) {super(properties);}

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            Level level, Player player, @NotNull InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        if (!level.dimension().equals(ModDimensions.CHESS_WORLD_KEY))
            return InteractionResultHolder.pass(stack);
        if (player.isShiftKeyDown()) {
            if (level.isClientSide()) {
                CellBrushClientHandler.applySelection(player);
            }
            return InteractionResultHolder.success(stack);
        }

        if (level.isClientSide()) {
            HitResult hit = player.pick(100.0, 0, false);
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = ((BlockHitResult) hit).getBlockPos();
                ChunkPos chunkPos = new ChunkPos(blockPos);
                //player.sendSystemMessage(Component.literal("DEBUG: выделен чанк = (" + chunkPos.x + ", " + chunkPos.z + ")"));
                CellBrushClientHandler.clientSelection.add(chunkPos);
            }
        }

        return InteractionResultHolder.success(stack);
    }

    public static void regenerateChunk(ServerLevel level, ChunkPos pos, CellType type) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int minX = pos.getMinBlockX();
        int minZ = pos.getMinBlockZ();

        for (int x = 0; x < 16; x++)
            for (int z = 0; z < 16; z++)
                for (int y = getFloorMinY(); y <= getFloorMaxY() + getPillarTop(); y++)
                    level.setBlock(mutable.set(minX+x, y, minZ+z),
                            Blocks.AIR.defaultBlockState(), 3);

        fillLive(level, pos, type, mutable);
    }

    private static void fillLive(ServerLevel level, ChunkPos pos,
                                 CellType type, BlockPos.MutableBlockPos mutable) {
        int minX = pos.getMinBlockX();
        int minZ = pos.getMinBlockZ();

        switch (type) {
            case DEFAULT -> {
                boolean isWhite = ((pos.x + pos.z) & 1) == 0;
                var block = isWhite
                        ? Blocks.WHITE_CONCRETE.defaultBlockState()
                        : Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
                for (int x = 0; x < 16; x++)
                    for (int z = 0; z < 16; z++)
                        for (int y = getFloorMinY(); y <= getFloorMaxY(); y++)
                            level.setBlock(mutable.set(minX+x, y, minZ+z), block, 3);
            }
            case WATER -> {
                var water = Blocks.WATER.defaultBlockState();
                for (int x = 0; x < 16; x++)
                    for (int z = 0; z < 16; z++)
                        for (int y = getFloorMinY(); y <= getFloorMaxY(); y++)
                            level.setBlock(mutable.set(minX+x, y, minZ+z), water, 3);
            }
            case VOID -> { }
            case STONE, NETHER -> {
                java.util.Random rand = new java.util.Random(
                        (long) pos.x * 341873128712L + (long) pos.z * 132897987541L);
                var base = type == CellType.NETHER
                        ? Blocks.NETHERRACK.defaultBlockState()
                        : Blocks.STONE.defaultBlockState();
                var ores = type == CellType.NETHER
                        ? List.of(Blocks.NETHER_QUARTZ_ORE.defaultBlockState(),
                        Blocks.NETHER_GOLD_ORE.defaultBlockState(),
                        Blocks.ANCIENT_DEBRIS.defaultBlockState())
                        : List.of(Blocks.COAL_ORE.defaultBlockState(),
                        Blocks.IRON_ORE.defaultBlockState(),
                        Blocks.DIAMOND_ORE.defaultBlockState());
                for (int x = 0; x < 16; x++)
                    for (int z = 0; z < 16; z++)
                        for (int y = getFloorMinY(); y <= getFloorMaxY() + getPillarTop(); y++) {
                            var block = rand.nextFloat() < 0.015f
                                    ? ores.get(rand.nextInt(ores.size())) : base;
                            level.setBlock(mutable.set(minX+x, y, minZ+z), block, 3);
                        }
            }
        }
    }
}
