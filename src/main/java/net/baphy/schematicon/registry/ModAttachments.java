package net.baphy.schematicon.registry;

import net.baphy.schematicon.SchematiconMod;
import net.baphy.schematicon.attachment.PlayerDimData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, SchematiconMod.MOD_ID);

    public static final Supplier<AttachmentType<PlayerDimData>> PLAYER_DIM_DATA =
            ATTACHMENT_TYPES.register("player_dim_data", () ->
                    AttachmentType.builder(PlayerDimData::new).serialize(
                            new net.neoforged.neoforge.attachment.IAttachmentSerializer<net.minecraft.nbt.CompoundTag,PlayerDimData>() {
                                @Override
                                public @NotNull PlayerDimData read(
                                        @NotNull IAttachmentHolder holder,
                                        @NotNull CompoundTag tag,
                                        HolderLookup.@NotNull Provider provider) {
                                    return PlayerDimData.load(tag);
                                }

                                @Override
                                public CompoundTag write(
                                        @NotNull PlayerDimData attachment,
                                        HolderLookup.@NotNull Provider provider) {
                                    return attachment.save();
                                }
                            }).copyOnDeath().build());


    public static PlayerDimData getDimData(Player player) {
        return player.getData(PLAYER_DIM_DATA);
    }

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
