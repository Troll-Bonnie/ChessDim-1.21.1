package net.baphy.schematicon.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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

public class SchematiconChunkGenerator extends ChunkGenerator {
    public static final MapCodec<SchematiconChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Biome.CODEC.fieldOf("biome").forGetter(gen -> gen.biomeHolder)
    ).apply(instance, SchematiconChunkGenerator::new));

    private final Holder<Biome> biomeHolder;

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

    public SchematiconChunkGenerator(Holder<Biome> biome) {
        super(new FixedBiomeSource(biome));
        this.biomeHolder = biome;
    }

    @Override
    protected @NotNull MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(@NotNull WorldGenRegion level, long seed, @NotNull RandomState random, @NotNull BiomeManager biomeManager, @NotNull StructureManager structureManager, @NotNull ChunkAccess chunk, GenerationStep.@NotNull Carving step) {

    }

    @Override
    public void buildSurface(@NotNull WorldGenRegion level, @NotNull StructureManager structureManager, @NotNull RandomState random, @NotNull ChunkAccess chunk) {

    }

    @Override
    public void spawnOriginalMobs(@NotNull WorldGenRegion level) {

    }

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public @NotNull CompletableFuture<ChunkAccess> fillFromNoise(@NotNull Blender blender, @NotNull RandomState randomState, @NotNull StructureManager structureManager, ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        fillDefault(chunk, chunkPos, mutable);
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
    public int getBaseHeight(int x, int z, Heightmap.@NotNull Types type, @NotNull LevelHeightAccessor level, @NotNull RandomState random) {
        return FLOOR_MAX_Y + PILLAR_TOP + 1;
    }

    @Override
    public @NotNull NoiseColumn getBaseColumn(int x, int z, @NotNull LevelHeightAccessor level, @NotNull RandomState random) {
        boolean isWhite = (Math.floorDiv(x, 16) + Math.floorDiv(z, 16) & 1) == 0;
        BlockState block = isWhite ? WHITE : GRAY;

        int height = FLOOR_MAX_Y - FLOOR_MIN_Y + 1; // 65 блоков
        BlockState[] states = new BlockState[height];
        Arrays.fill(states, block);

        return new NoiseColumn(FLOOR_MIN_Y, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, @NotNull RandomState random, @NotNull BlockPos pos) {
        ChunkPos cp = new ChunkPos(pos);
        boolean isWhite = ((cp.x + cp.z) & 1) == 0;
        info.add("Schematicon cell: " + (isWhite ? "WHITE" : "GRAY")
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


}
