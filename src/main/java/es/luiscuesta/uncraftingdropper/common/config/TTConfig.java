package es.luiscuesta.uncraftingdropper.common.config;


import es.luiscuesta.uncraftingdropper.common.libs.LibMisc;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = LibMisc.MOD_ID)
@Config.LangKey("uncraftingdropper.config.title")
public class TTConfig {
	
	
    @Config.Comment( "Consumes damaged item when nothing is returned, but can get a book if enchanted")
    @Config.Name("Consume Damaged Item")
    @Config.LangKey("uncraftingdropper.config.consumeDamagedItem")
    // if true, the item will be consumed when nothing is returned cos is very damaged	
    public static boolean consumeItem = false;
    
    
    @Config.Comment("Enchant mode, 0 = none, 1 = first, 2 = random, 3 = all")
    @Config.Name("Enchant Mode")
    @Config.LangKey("uncraftingdropper.config.enchantMode")
    @Config.RangeInt(min = 0, max = 3)
    public static int enchantMode = 1;   
    
    @Config.Comment("Probability of getting an enchantment.")
    @Config.Name("Book Probability")
    @Config.LangKey("uncraftingdropper.config.enchantPercent")
    @Config.RangeInt(min = 1, max = 100)
    public static int bookProbability = 20;   
    
    
    @Config.Comment("Fixed percent reduction.")
    @Config.Name("Fixed Reduction")
    @Config.LangKey("uncraftingdropper.config.reduction")
    @Config.RangeInt(min = 1, max = 100)
    public static int fixedReduction = 50;
    
    @Config.Comment("Probability percent reduction applied after the fixed percent reduction.")
    @Config.Name("Probability Reduction")
    @Config.LangKey("uncraftingdropper.config.probability")
    @Config.RangeInt(min = 1, max = 100)
    public static int probabilityReduction = 0;
    
    @Config.Comment("Enable XP bottles when uncrafting enchanted books")
    @Config.Name("Enable XP Bottles from Books")
    @Config.LangKey("uncraftingdropper.config.enableXPBottles")
    public static boolean enableXPBottles = true;
    
    @Config.Comment("XP multiplier for enchanted books (higher values = more XP bottles)")
    @Config.Name("XP Bottle Multiplier")
    @Config.LangKey("uncraftingdropper.config.xpMultiplier")
    @Config.RangeDouble(min = 0.1, max = 5.0)
    public static double xpMultiplier = 1.0;
    
    
    @Mod.EventBusSubscriber
    private static class EventHandler {

        /**
         * Inject the new values and save to the config file when the config has been changed from the GUI.
         *
         * @param event The event
         */
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(LibMisc.MOD_ID)) {
                ConfigManager.sync(LibMisc.MOD_ID, net.minecraftforge.common.config.Config.Type.INSTANCE);
            }
        }
    }
}

