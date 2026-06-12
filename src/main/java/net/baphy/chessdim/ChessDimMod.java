package net.baphy.chessdim;

import net.baphy.chessdim.registry.ModAttachments;
import net.baphy.chessdim.registry.ModDimensions;
import net.baphy.chessdim.registry.ModItems;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ChessDimMod.MOD_ID)
public class ChessDimMod {
    public static final String MOD_ID = "chessdim";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ChessDimMod(IEventBus modEventBus) {
        ModDimensions.register(modEventBus);
        ModItems.register(modEventBus);
        ModAttachments.register(modEventBus);
        LOGGER.info("ChessDimMod loading...");
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
