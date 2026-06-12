package net.baphy.chessdim.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChessChunkGenerator extends ChunkGenerator {
    public static final MapCodec<ChessChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Biome.CODEC.fieldOf("biome").forGetter(gen -> gen.biomeHolder)
    ).apply(instance, ChessChunkGenerator::new));

    private final Holder<Biome> biomeHolder;
    private ServerLevel serverLevel;

    // Белый и светло-серый бетон
    private static final BlockState WHITE = Blocks.WHITE_CONCRETE.defaultBlockState();
    private static final BlockState GRAY  = Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();

    private static final int FLOOR_MIN_Y = -64;
    private static final int FLOOR_MAX_Y = 0;
    private static final int PILLAR_TOP  = 50;

    public static int getFloorMinY(){
        return FLOOR_MIN_Y;
    }

    public static int getFloorMaxY(){
        return FLOOR_MAX_Y;
    }

    public static int getPillarTop(){
        return PILLAR_TOP;
    }

    public ChessChunkGenerator(Holder<Biome> biome) {
        super(new FixedBiomeSource(biome));
        this.biomeHolder = biome;
    }

    public void setServerLevel(ServerLevel level){
        this.serverLevel = level;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {

    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk) {

    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {

    }

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        CellType cellType = CellType.DEFAULT;
        if (serverLevel != null) {
            cellType = ChessCellDataHelper.getCell(serverLevel, chunkPos);
        }

        switch (cellType) {
            case DEFAULT -> fillDefault(chunk, chunkPos, mutable);
            case WATER   -> fillWater(chunk, chunkPos, mutable, true);
            case VOID    -> fillWater(chunk, chunkPos, mutable, false);
            case STONE   -> fillStone(chunk, chunkPos, mutable, randomState, false);
            case NETHER  -> fillStone(chunk, chunkPos, mutable, randomState, true);
        }

        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return FLOOR_MIN_Y;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        return FLOOR_MAX_Y + PILLAR_TOP + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        boolean isWhite = (Math.floorDiv(x, 16) + Math.floorDiv(z, 16) & 1) == 0;
        BlockState block = isWhite ? WHITE : GRAY;

        int height = FLOOR_MAX_Y - FLOOR_MIN_Y + 1; // 65 блоков
        BlockState[] states = new BlockState[height];
        Arrays.fill(states, block);

        return new NoiseColumn(FLOOR_MIN_Y, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        ChunkPos cp = new ChunkPos(pos);
        boolean isWhite = ((cp.x + cp.z) & 1) == 0;
        info.add("ChessDim cell: " + (isWhite ? "WHITE" : "GRAY")
                + " | chunk: " + cp.x + ", " + cp.z);
    }

    private void fillDefault(ChunkAccess chunk, ChunkPos pos,
                             BlockPos.MutableBlockPos mutable) {
        boolean isWhite = ((pos.x + pos.z) & 1) == 0;
        BlockState block = isWhite ? WHITE : GRAY;

        for (int x = 0; x < 16; x++)
            for (int z = 0; z < 16; z++)
                for (int y = FLOOR_MIN_Y; y <= FLOOR_MAX_Y; y++)
                    chunk.setBlockState(
                            mutable.set(pos.getMinBlockX()+x, y, pos.getMinBlockZ()+z),
                            block, false);
    }

    private void fillWater(ChunkAccess chunk, ChunkPos pos,
                           BlockPos.MutableBlockPos mutable, boolean isWater) {
        BlockState water = isWater
                ? Blocks.WATER.defaultBlockState()
                : Blocks.AIR.defaultBlockState();

        for (int x = 0; x < 16; x++)
            for (int z = 0; z < 16; z++)
                for (int y = FLOOR_MIN_Y; y <= FLOOR_MAX_Y; y++)
                    chunk.setBlockState(
                            mutable.set(pos.getMinBlockX()+x, y, pos.getMinBlockZ()+z),
                            water, false);
    }

    private void fillStone(ChunkAccess chunk, ChunkPos pos,
                           BlockPos.MutableBlockPos mutable,
                           RandomState randomState, boolean isNether) {

        // Базовый блок
        BlockState base = isNether
                ? Blocks.NETHERRACK.defaultBlockState()
                : Blocks.STONE.defaultBlockState();

        // Руды для оверворлда
        List<BlockState> ores = getOres(isNether);

        // Используем позицию чанка как seed для рандома
        java.util.Random rand = new java.util.Random(
                (long) pos.x * 341873128712L + (long) pos.z * 132897987541L
        );

        int topY = FLOOR_MAX_Y + PILLAR_TOP; // Y=50

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = FLOOR_MIN_Y; y <= topY; y++) {

                    BlockState block = base;

                    // Шанс руды: 1.5% для каждого блока
                    if (rand.nextFloat() < 0.015f) {
                        block = ores.get(rand.nextInt(ores.size()));
                    }

                    chunk.setBlockState(
                            mutable.set(pos.getMinBlockX()+x, y, pos.getMinBlockZ()+z),
                            block, false);
                }
            }
        }
    }

    private static @NotNull List<BlockState> getOres(boolean isNether) {
        List<BlockState> overworldOres = List.of(
                Blocks.COAL_ORE.defaultBlockState(),
                Blocks.IRON_ORE.defaultBlockState(),
                Blocks.COPPER_ORE.defaultBlockState(),
                Blocks.GOLD_ORE.defaultBlockState(),
                Blocks.REDSTONE_ORE.defaultBlockState(),
                Blocks.LAPIS_ORE.defaultBlockState(),
                Blocks.DIAMOND_ORE.defaultBlockState(),
                Blocks.EMERALD_ORE.defaultBlockState()
        );

        // Руды для незера
        List<BlockState> netherOres = List.of(
                Blocks.NETHER_QUARTZ_ORE.defaultBlockState(),
                Blocks.NETHER_GOLD_ORE.defaultBlockState(),
                Blocks.ANCIENT_DEBRIS.defaultBlockState()
        );

        return isNether ? netherOres : overworldOres;
    }


}
