package net.baphy.chessdim.world;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class ChessCellDataHelper {
    public static ChessCellData get(ServerLevel chessLevel) {
        DimensionDataStorage storage = chessLevel.getDataStorage();
        return storage.computeIfAbsent(
                ChessCellData.factory(),
                ChessCellData.DATA_NAME
        );
    }
    public static CellType getCell(ServerLevel chessLevel, ChunkPos pos) {
        return get(chessLevel).getCell(pos);
    }

    public static void setCell(ServerLevel chessLevel, ChunkPos pos, CellType type) {
        get(chessLevel).setCell(pos, type);
    }

    public static void resetCell(ServerLevel chessLevel, ChunkPos pos) {
        get(chessLevel).resetCell(pos);
    }
}
