package net.baphy.chessdim.registry;

import net.baphy.chessdim.ChessDimMod;
import net.baphy.chessdim.world.ChessChunkGenerator;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModDimensions {
    private static Object ChessDim;
    public static final ResourceKey<Level> CHESS_WORLD_KEY = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(ChessDimMod.MOD_ID, "chess_world"));
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS = DeferredRegister.create(Registries.CHUNK_GENERATOR, ChessDimMod.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<ChessChunkGenerator>> CHESS_GENERATOR = CHUNK_GENERATORS.register("chess_generator", () -> ChessChunkGenerator.CODEC);

    public static void register(IEventBus modEventBus) {
        CHUNK_GENERATORS.register(modEventBus);
    }
}
