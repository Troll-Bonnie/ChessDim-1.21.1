package net.baphy.chessdim.network;

import net.baphy.chessdim.ChessDimMod;
import net.baphy.chessdim.world.CellType;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;

@EventBusSubscriber(modid = ChessDimMod.MOD_ID)
public class ModPackets {
    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                ApplySelectionPacket.TYPE,
                ApplySelectionPacket.CODEC,
                ApplySelectionPacket::handle);
    }


    public static void sendApplySelection(List<ChunkPos> chunks, CellType type) {
        PacketDistributor.sendToServer(new ApplySelectionPacket(chunks, type));
    }
}
