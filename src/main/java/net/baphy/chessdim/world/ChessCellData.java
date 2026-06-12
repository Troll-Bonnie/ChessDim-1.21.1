package net.baphy.chessdim.world;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class ChessCellData extends SavedData {

    public static final String DATA_NAME = "chessdim_cells";
    private final Map<ChunkPos, CellType> cellMap = new HashMap<>();

    public static ChessCellData create() {
        return new ChessCellData();
    }

    public static ChessCellData load(CompoundTag tag, HolderLookup.Provider provider) {
        ChessCellData data = new ChessCellData();
        data.readFromNBT(tag);
        return data;
    }

    public static Factory<ChessCellData> factory() {
        return new Factory<>(
                ChessCellData::create,
                ChessCellData::load,
                null
        );
    }

    // --- Чтение / запись NBT ---

    private void readFromNBT(CompoundTag tag) {
        ListTag list = tag.getList("cells", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int x = entry.getInt("x");
            int z = entry.getInt("z");
            String typeName = entry.getString("type");

            try {
                CellType type = CellType.valueOf(typeName);
                cellMap.put(new ChunkPos(x, z), type);
            } catch (IllegalArgumentException e) {
                // Неизвестный тип — пропускаем, будет дефолт
            }
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();

        for (Map.Entry<ChunkPos, CellType> entry : cellMap.entrySet()) {
            CompoundTag cellTag = new CompoundTag();
            cellTag.putInt("x", entry.getKey().x);
            cellTag.putInt("z", entry.getKey().z);
            cellTag.putString("type", entry.getValue().name());
            list.add(cellTag);
        }

        tag.put("cells", list);
        return tag;
    }

    // --- API для работы с клетками ---

    /**
     * Получить тип клетки. Если не задан — вернуть дефолт по шахматному паттерну.
     */
    public CellType getCell(ChunkPos pos) {
        return cellMap.getOrDefault(pos, CellType.DEFAULT);
    }

    /**
     * Установить тип клетки и пометить данные как изменённые.
     */
    public void setCell(ChunkPos pos, CellType type) {
        // Если тип совпадает с дефолтным — удаляем запись (экономим место)
        if (type == CellType.DEFAULT) {
            cellMap.remove(pos);
        } else {
            cellMap.put(pos, type);
        }
        setDirty(); // говорим Minecraft: сохрани это на диск
    }

    /**
     * Сбросить клетку к дефолтному шахматному виду.
     */
    public void resetCell(ChunkPos pos) {
        cellMap.remove(pos);
        setDirty();
    }

    /**
     * Проверить, задан ли кастомный тип для клетки.
     */
    public boolean hasCustomCell(ChunkPos pos) {
        return cellMap.containsKey(pos);
    }
}