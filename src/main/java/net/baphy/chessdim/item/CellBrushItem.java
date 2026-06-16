package net.baphy.chessdim.item;

import net.baphy.chessdim.world.CellType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

import static net.baphy.chessdim.world.ChessChunkGenerator.*;
import static net.baphy.chessdim.world.ChessChunkGenerator.getFloorMinY;

public class CellBrushItem extends Item {

    public CellBrushItem(Properties properties) {super(properties);}

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
            case DEFAULT ->
                fillDefault(level, pos, getFloorMinY(), getFloorMaxY(), mutable);
            case WATER -> {
                fillDefault(level, pos, getFloorMinY(), getFloorMinY(), mutable);
                var water = Blocks.WATER.defaultBlockState();
                for (int x = 0; x < 16; x++)
                    for (int z = 0; z < 16; z++)
                        for (int y = getFloorMinY() + 1; y <= getFloorMaxY(); y++)
                            level.setBlock(mutable.set(minX+x, y, minZ+z), water, 3);
            }
            case VOID ->
                fillDefault(level, pos, getFloorMinY(), getFloorMinY(), mutable);
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
            case GRASS -> {
                var dirt = Blocks.DIRT.defaultBlockState();
                var grass = Blocks.GRASS_BLOCK.defaultBlockState();
                for (int x = 0; x < 16; x++)
                    for (int z = 0; z < 16; z++) {
                        for (int y = getFloorMinY(); y < getFloorMaxY(); y++)
                            level.setBlock(mutable.set(minX + x, y, minZ + z), dirt, 3);
                        level.setBlock(mutable.set(minX + x, getFloorMaxY(), minZ + z), grass, 3);
                    }
            }
        }
    }

    public static void fillDefault(ServerLevel level, ChunkPos pos,
                            int minY, int maxY, BlockPos.MutableBlockPos mutable) {
        int minX = pos.getMinBlockX();
        int minZ = pos.getMinBlockZ();
        boolean isWhite = ((pos.x + pos.z) & 1) == 0;
        var block = isWhite
                ? Blocks.WHITE_CONCRETE.defaultBlockState()
                : Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
        for (int x = 0; x < 16; x++)
            for (int z = 0; z < 16; z++)
                for (int y = minY; y <= maxY; y++)
                    level.setBlock(mutable.set(minX+x, y, minZ+z), block, 3);
    }
}
