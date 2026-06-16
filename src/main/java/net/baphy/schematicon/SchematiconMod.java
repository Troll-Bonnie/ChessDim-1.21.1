package net.baphy.schematicon;

import net.baphy.schematicon.registry.ModAttachments;
import net.baphy.schematicon.registry.ModDimensions;
import net.baphy.schematicon.registry.ModItems;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(SchematiconMod.MOD_ID)
public class SchematiconMod {
    public static final String MOD_ID = "schematicon";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SchematiconMod(IEventBus modEventBus) {
        ModDimensions.register(modEventBus);
        ModItems.register(modEventBus);
        ModAttachments.register(modEventBus);
        LOGGER.info("Schematicon loading...");
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
