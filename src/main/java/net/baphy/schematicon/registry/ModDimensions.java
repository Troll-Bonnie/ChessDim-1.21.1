package net.baphy.schematicon.registry;

import com.mojang.serialization.MapCodec;
import net.baphy.schematicon.SchematiconMod;
import net.baphy.schematicon.world.SchematiconChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModDimensions {
    public static final ResourceKey<Level> SCHEMATICON_KEY = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(SchematiconMod.MOD_ID, "schematicon_world"));
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS = DeferredRegister.create(Registries.CHUNK_GENERATOR, SchematiconMod.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>, MapCodec<SchematiconChunkGenerator>> SCHEMATICON_GENERATOR = CHUNK_GENERATORS.register("schematicon_generator", () -> SchematiconChunkGenerator.CODEC);

    public static void register(IEventBus modEventBus) {
        CHUNK_GENERATORS.register(modEventBus);
    }
}
