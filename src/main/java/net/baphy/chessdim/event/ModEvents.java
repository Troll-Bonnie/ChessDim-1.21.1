package net.baphy.chessdim.event;

import net.baphy.chessdim.ChessDimMod;
import net.baphy.chessdim.client.CellBrushClientHandler;
import net.baphy.chessdim.client.ChunkSelectionRenderer;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ChessDimMod.MOD_ID)
public class ModEvents {


    @SubscribeEvent
    public static void onClientTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof net.minecraft.client.player.LocalPlayer) {
            CellBrushClientHandler.onClientTick(Minecraft.getInstance());
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (CellBrushClientHandler.onMouseScroll(event.getScrollDeltaY())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLeftClick(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack()) {
            CellBrushClientHandler.onLeftClick();
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        ChunkSelectionRenderer.onRenderLevel(event);
    }
}
