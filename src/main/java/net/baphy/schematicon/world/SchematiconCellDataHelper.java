package net.baphy.schematicon.world;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class SchematiconCellDataHelper {
    public static SchematiconCellData get(ServerLevel chessLevel) {
        DimensionDataStorage storage = chessLevel.getDataStorage();
        return storage.computeIfAbsent(
                SchematiconCellData.factory(),
                SchematiconCellData.DATA_NAME
        );
    }

    public static void setCell(ServerLevel chessLevel, ChunkPos pos, CellType type) {
        get(chessLevel).setCell(pos, type);
    }

}
