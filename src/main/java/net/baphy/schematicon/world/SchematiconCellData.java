package net.baphy.schematicon.world;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SchematiconCellData extends SavedData {

    public static final String DATA_NAME = "schematicon_cells";
    private final Map<ChunkPos, CellType> cellMap = new HashMap<>();

    public static SchematiconCellData create() {
        return new SchematiconCellData();
    }

    public static SchematiconCellData load(CompoundTag tag, HolderLookup.Provider provider) {
        SchematiconCellData data = new SchematiconCellData();
        data.readFromNBT(tag);
        return data;
    }

    public static Factory<SchematiconCellData> factory() {
        return new Factory<>(
                SchematiconCellData::create,
                SchematiconCellData::load,
                null
        );
    }


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
            } catch (IllegalArgumentException ignored) {

            }
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
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


    public void setCell(ChunkPos pos, CellType type) {
        if (type == CellType.DEFAULT) {
            cellMap.remove(pos);
        } else {
            cellMap.put(pos, type);
        }
        setDirty();
    }


}