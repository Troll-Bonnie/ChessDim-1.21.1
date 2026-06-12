package net.baphy.chessdim.network;

import net.baphy.chessdim.client.CellBrushClientHandler;
import net.baphy.chessdim.item.CellBrushItem;
import net.baphy.chessdim.world.CellType;
import net.baphy.chessdim.world.ChessCellDataHelper;
import net.baphy.chessdim.world.ChessChunkGenerator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record ApplySelectionPacket(List<ChunkPos> chunks, CellType cellType) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ApplySelectionPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath("chessdim", "apply_selection"));

    public static final StreamCodec<FriendlyByteBuf, ApplySelectionPacket> CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeEnum(pkt.cellType);
                        buf.writeInt(pkt.chunks.size());
                        for (ChunkPos pos : pkt.chunks) {
                            buf.writeLong(pos.toLong());
                        }
                    },
                    buf -> {
                        CellType type = buf.readEnum(CellType.class);
                        int size = buf.readInt();
                        List<ChunkPos> chunks = new ArrayList<>(size);
                        for (int i = 0; i < size; i++) {
                            chunks.add(new ChunkPos(buf.readLong()));
                        }
                        return new ApplySelectionPacket(chunks, type);
                    });

    @Override
    public CustomPacketPayload.Type<ApplySelectionPacket> type() {
        return TYPE;
    }

    public static void handle(ApplySelectionPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            ServerLevel level = player.serverLevel();

            for (ChunkPos pos : pkt.chunks) {
                ChessCellDataHelper.setCell(level, pos, pkt.cellType);
                CellBrushItem.regenerateChunk(level, pos, pkt.cellType);
            }

            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "item.chessdim.cell_brush.applied",
                            pkt.chunks.size(),
                            net.minecraft.network.chat.Component.translatable(
                                    pkt.cellType.getTranslationKey())));
        });
    }
}
