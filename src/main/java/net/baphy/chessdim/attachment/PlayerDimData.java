package net.baphy.chessdim.attachment;

import net.baphy.chessdim.registry.ModAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.GameType;

public class PlayerDimData {

    // --- Данные оверворлда ---
    private CompoundTag overworldInventory = new CompoundTag();
    private double overworldX, overworldY, overworldZ;
    private float overworldYaw, overworldPitch;
    private GameType overworldGameMode = GameType.SURVIVAL;
    private boolean hasOverworldData = false;

    // --- Данные шахматного измерения ---
    private CompoundTag chessInventory = new CompoundTag();
    private double chessX, chessY, chessZ;
    private float chessYaw, chessPitch;
    private boolean hasChessPos = false;

    // --- NBT ключи ---
    private static final String KEY_OW_INV       = "ow_inv";
    private static final String KEY_OW_X         = "ow_x";
    private static final String KEY_OW_Y         = "ow_y";
    private static final String KEY_OW_Z         = "ow_z";
    private static final String KEY_OW_YAW       = "ow_yaw";
    private static final String KEY_OW_PITCH     = "ow_pitch";
    private static final String KEY_OW_GAMEMODE  = "ow_gamemode";
    private static final String KEY_HAS_OW       = "has_ow";
    private static final String KEY_CHESS_INV    = "chess_inv";
    private static final String KEY_CHESS_X      = "chess_x";
    private static final String KEY_CHESS_Y      = "chess_y";
    private static final String KEY_CHESS_Z      = "chess_z";
    private static final String KEY_CHESS_YAW    = "chess_yaw";
    private static final String KEY_CHESS_PITCH  = "chess_pitch";
    private static final String KEY_HAS_CHESS    = "has_chess";

    // --- Сериализация ---

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put(KEY_OW_INV, overworldInventory);
        tag.putDouble(KEY_OW_X, overworldX);
        tag.putDouble(KEY_OW_Y, overworldY);
        tag.putDouble(KEY_OW_Z, overworldZ);
        tag.putFloat(KEY_OW_YAW, overworldYaw);
        tag.putFloat(KEY_OW_PITCH, overworldPitch);
        tag.putInt(KEY_OW_GAMEMODE, overworldGameMode.getId());
        tag.putBoolean(KEY_HAS_OW, hasOverworldData);
        tag.put(KEY_CHESS_INV, chessInventory);
        tag.putDouble(KEY_CHESS_X, chessX);
        tag.putDouble(KEY_CHESS_Y, chessY);
        tag.putDouble(KEY_CHESS_Z, chessZ);
        tag.putFloat(KEY_CHESS_YAW, chessYaw);
        tag.putFloat(KEY_CHESS_PITCH, chessPitch);
        tag.putBoolean(KEY_HAS_CHESS, hasChessPos);
        return tag;
    }

    public static PlayerDimData load(CompoundTag tag) {
        PlayerDimData data = new PlayerDimData();
        data.overworldInventory = tag.getCompound(KEY_OW_INV);
        data.overworldX         = tag.getDouble(KEY_OW_X);
        data.overworldY         = tag.getDouble(KEY_OW_Y);
        data.overworldZ         = tag.getDouble(KEY_OW_Z);
        data.overworldYaw       = tag.getFloat(KEY_OW_YAW);
        data.overworldPitch     = tag.getFloat(KEY_OW_PITCH);
        data.overworldGameMode  = GameType.byId(tag.getInt(KEY_OW_GAMEMODE));
        data.hasOverworldData   = tag.getBoolean(KEY_HAS_OW);
        data.chessInventory     = tag.getCompound(KEY_CHESS_INV);
        data.chessX             = tag.getDouble(KEY_CHESS_X);
        data.chessY             = tag.getDouble(KEY_CHESS_Y);
        data.chessZ             = tag.getDouble(KEY_CHESS_Z);
        data.chessYaw           = tag.getFloat(KEY_CHESS_YAW);
        data.chessPitch         = tag.getFloat(KEY_CHESS_PITCH);
        data.hasChessPos        = tag.getBoolean(KEY_HAS_CHESS);
        return data;
    }

    // --- Работа с инвентарём ---

    private static CompoundTag serializeInventory(Inventory inventory) {
        CompoundTag tag = new CompoundTag();
        ListTag list = inventory.save(new ListTag());
        tag.put("items", list);
        tag.putInt("selected", inventory.selected);
        return tag;
    }

    private static void deserializeInventory(Inventory inventory, CompoundTag tag) {
        inventory.clearContent();
        if (tag.contains("items")) {
            inventory.load(tag.getList("items", Tag.TAG_COMPOUND));
        }
        if (tag.contains("selected")) {
            inventory.selected = tag.getInt("selected");
        }
    }

    // --- Телепортация: оверворлд → шахматный мир ---

    public static void saveOverworldDataAndTeleport(
            ServerPlayer player,
            ServerLevel chessLevel
    ) {
        PlayerDimData data = ModAttachments.getDimData(player);

        // 1. Сохраняем инвентарь оверворлда
        data.overworldInventory = serializeInventory(player.getInventory());
        data.overworldX         = player.getX();
        data.overworldY         = player.getY();
        data.overworldZ         = player.getZ();
        data.overworldYaw       = player.getYRot();
        data.overworldPitch     = player.getXRot();
        data.overworldGameMode  = player.gameMode.getGameModeForPlayer();
        data.hasOverworldData   = true;

        // 2. Очищаем инвентарь
        player.getInventory().clearContent();

        // 3. Загружаем шахматный инвентарь (если есть)
        if (!data.chessInventory.isEmpty()) {
            deserializeInventory(player.getInventory(), data.chessInventory);
        }

        // 4. Переключаем в Creative
        player.setGameMode(GameType.CREATIVE);

        // 5. Телепортируем
        double tx = data.hasChessPos ? data.chessX : 0.5;
        double ty = data.hasChessPos ? data.chessY : 1.0;
        double tz = data.hasChessPos ? data.chessZ : 0.5;
        float  yaw   = data.hasChessPos ? data.chessYaw   : 0f;
        float  pitch = data.hasChessPos ? data.chessPitch  : 0f;

        player.teleportTo(chessLevel, tx, ty, tz, yaw, pitch);

        // 6. Сохраняем attachment
        player.setData(ModAttachments.PLAYER_DIM_DATA, data);
    }

    // --- Телепортация: шахматный мир → оверворлд ---

    public static void saveChessDataAndReturn(ServerPlayer player) {
        PlayerDimData data = ModAttachments.getDimData(player);

        // 1. Сохраняем шахматный инвентарь
        data.chessInventory = serializeInventory(player.getInventory());
        data.chessX         = player.getX();
        data.chessY         = player.getY();
        data.chessZ         = player.getZ();
        data.chessYaw       = player.getYRot();
        data.chessPitch     = player.getXRot();
        data.hasChessPos    = true;

        // 2. Очищаем инвентарь
        player.getInventory().clearContent();

        // 3. Восстанавливаем оверворлд инвентарь
        if (data.hasOverworldData) {
            deserializeInventory(player.getInventory(), data.overworldInventory);
        }

        // 4. Восстанавливаем gamemode
        player.setGameMode(data.overworldGameMode);

        // 5. Телепортируем обратно
        ServerLevel overworld = player.getServer()
                .getLevel(net.minecraft.world.level.Level.OVERWORLD);

        if (overworld != null && data.hasOverworldData) {
            player.teleportTo(overworld,
                    data.overworldX,
                    data.overworldY,
                    data.overworldZ,
                    data.overworldYaw,
                    data.overworldPitch);
        }

        // 6. Сохраняем attachment
        player.setData(ModAttachments.PLAYER_DIM_DATA, data);
    }
}
