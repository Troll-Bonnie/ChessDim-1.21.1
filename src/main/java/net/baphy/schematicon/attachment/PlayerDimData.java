package net.baphy.schematicon.attachment;

import net.baphy.schematicon.registry.ModAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.GameType;

import java.util.Objects;

public class PlayerDimData {

    private CompoundTag overworldInventory = new CompoundTag();
    private double overworldX, overworldY, overworldZ;
    private float overworldYaw, overworldPitch;
    private GameType overworldGameMode = GameType.SURVIVAL;
    private boolean hasOverworldData = false;

    private CompoundTag schemInventory = new CompoundTag();
    private double schemX, schemY, schemZ;
    private float schemYaw, schemPitch;
    private boolean hasSchemPos = false;

    // --- NBT ключи ---
    private static final String KEY_OW_INV       = "ow_inv";
    private static final String KEY_OW_X         = "ow_x";
    private static final String KEY_OW_Y         = "ow_y";
    private static final String KEY_OW_Z         = "ow_z";
    private static final String KEY_OW_YAW       = "ow_yaw";
    private static final String KEY_OW_PITCH     = "ow_pitch";
    private static final String KEY_OW_GAMEMODE  = "ow_gamemode";
    private static final String KEY_HAS_OW       = "has_ow";
    private static final String KEY_schem_INV    = "schem_inv";
    private static final String KEY_schem_X      = "schem_x";
    private static final String KEY_schem_Y      = "schem_y";
    private static final String KEY_schem_Z      = "schem_z";
    private static final String KEY_schem_YAW    = "schem_yaw";
    private static final String KEY_schem_PITCH  = "schem_pitch";
    private static final String KEY_HAS_schem    = "has_schem";

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
        tag.put(KEY_schem_INV, schemInventory);
        tag.putDouble(KEY_schem_X, schemX);
        tag.putDouble(KEY_schem_Y, schemY);
        tag.putDouble(KEY_schem_Z, schemZ);
        tag.putFloat(KEY_schem_YAW, schemYaw);
        tag.putFloat(KEY_schem_PITCH, schemPitch);
        tag.putBoolean(KEY_HAS_schem, hasSchemPos);
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
        data.schemInventory = tag.getCompound(KEY_schem_INV);
        data.schemX             = tag.getDouble(KEY_schem_X);
        data.schemY             = tag.getDouble(KEY_schem_Y);
        data.schemZ             = tag.getDouble(KEY_schem_Z);
        data.schemYaw           = tag.getFloat(KEY_schem_YAW);
        data.schemPitch         = tag.getFloat(KEY_schem_PITCH);
        data.hasSchemPos = tag.getBoolean(KEY_HAS_schem);
        return data;
    }

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

    public static void saveOverworldDataAndTeleport(
            ServerPlayer player,
            ServerLevel schemLevel
    ) {
        PlayerDimData data = ModAttachments.getDimData(player);

        data.overworldInventory = serializeInventory(player.getInventory());
        data.overworldX         = player.getX();
        data.overworldY         = player.getY();
        data.overworldZ         = player.getZ();
        data.overworldYaw       = player.getYRot();
        data.overworldPitch     = player.getXRot();
        data.overworldGameMode  = player.gameMode.getGameModeForPlayer();
        data.hasOverworldData   = true;

        player.getInventory().clearContent();

        if (!data.schemInventory.isEmpty()) {
            deserializeInventory(player.getInventory(), data.schemInventory);
        }

        player.setGameMode(GameType.CREATIVE);

        double tx = data.hasSchemPos ? data.schemX : 0.5;
        double ty = data.hasSchemPos ? data.schemY : 1.0;
        double tz = data.hasSchemPos ? data.schemZ : 0.5;
        float  yaw   = data.hasSchemPos ? data.schemYaw   : 0f;
        float  pitch = data.hasSchemPos ? data.schemPitch  : 0f;

        player.teleportTo(schemLevel, tx, ty, tz, yaw, pitch);

        player.setData(ModAttachments.PLAYER_DIM_DATA, data);
    }


    public static void saveSchemDataAndReturn(ServerPlayer player) {
        PlayerDimData data = ModAttachments.getDimData(player);

        data.schemInventory = serializeInventory(player.getInventory());
        data.schemX         = player.getX();
        data.schemY         = player.getY();
        data.schemZ         = player.getZ();
        data.schemYaw       = player.getYRot();
        data.schemPitch     = player.getXRot();
        data.hasSchemPos = true;

        player.getInventory().clearContent();

        if (data.hasOverworldData) {
            deserializeInventory(player.getInventory(), data.overworldInventory);
        }

        player.setGameMode(data.overworldGameMode);

        ServerLevel overworld = Objects.requireNonNull(player.getServer()).getLevel(net.minecraft.world.level.Level.OVERWORLD);

        if (overworld != null && data.hasOverworldData) {
            player.teleportTo(overworld,
                    data.overworldX,
                    data.overworldY,
                    data.overworldZ,
                    data.overworldYaw,
                    data.overworldPitch);
        }

        player.setData(ModAttachments.PLAYER_DIM_DATA, data);
    }
}
