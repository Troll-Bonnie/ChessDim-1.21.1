package net.baphy.schematicon.registry;

import net.baphy.schematicon.SchematiconMod;
import net.baphy.schematicon.item.ChessKeyItem;
import net.baphy.schematicon.item.CellBrushItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = SchematiconMod.MOD_ID)
public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SchematiconMod.MOD_ID);

    public static final DeferredItem<ChessKeyItem> CHESS_KEY = ITEMS.register("chess_key", () ->
                    new ChessKeyItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<CellBrushItem> CELL_BRUSH = ITEMS.register("cell_brush", () ->
                    new CellBrushItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    @SubscribeEvent
    public static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event){
        if(event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
            event.accept(CHESS_KEY.get());
            event.accept(CELL_BRUSH.get());
        }
    }
}
