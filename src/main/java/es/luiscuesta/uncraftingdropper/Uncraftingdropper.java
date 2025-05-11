//Copyright (c) Luis Cuesta 2025
package es.luiscuesta.uncraftingdropper;
import org.apache.logging.log4j.Logger;

import es.luiscuesta.uncraftingdropper.common.blocks.RegistrationHandler;
import es.luiscuesta.uncraftingdropper.common.libs.LibMisc;
import es.luiscuesta.uncraftingdropper.common.misc.UncraftingdropperCreativeTab;
import es.luiscuesta.uncraftingdropper.common.packets.PacketHandler;
import es.luiscuesta.uncraftingdropper.common.tileentity.UncraftHelper;
import es.luiscuesta.uncraftingdropper.proxy.ICommonProxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = LibMisc.MOD_ID, name = LibMisc.MOD_NAME,
     version = LibMisc.MOD_VERSION, dependencies = LibMisc.MOD_DEPENDENCIES)
public class Uncraftingdropper {
	
@Mod.Instance(LibMisc.MOD_ID) public static Uncraftingdropper instance;	

// MinecraftForge.EVENT_BUS.register(ModEventSubscriber.class);

@SidedProxy(clientSide="es.luiscuesta.uncraftingdropper.proxy.ClientProxy", serverSide="es.luiscuesta.uncraftingdropper.proxy.ServerProxy")
  public static ICommonProxy commonProxy=null;

 
  public static Logger logger;
  public static RegistrationHandler modRegistry= new RegistrationHandler();
  private static CreativeTabs tab;
  
  public static CreativeTabs getTab() { return tab; }

  public static void setTab(CreativeTabs tab) {Uncraftingdropper.tab = tab; }

  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    tab = new UncraftingdropperCreativeTab();
    logger = event.getModLog();
 	commonProxy.preInit(event);
    PacketHandler.registerMessages(LibMisc.MOD_ID);
  }  
  
	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
			if (commonProxy!=null) commonProxy.init(event);

	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if (commonProxy!=null) commonProxy.postInit(event);

		UncraftHelper.initializeCache();
		MinecraftForge.EVENT_BUS.register(this);
	}	
	
    @Mod.EventHandler
    public void init(FMLServerStartingEvent event)
    {
      logger.info("initalise FMLServerStartingEvent :" + LibMisc.MOD_NAME);
      //event.registerServerCommand(new HandCommand());
    }
    

  @Mod.EventHandler
  public void serverLoad(FMLServerStartingEvent event) {
   
  }

}

/* TODO

Cuando se activa el redstone, si esta trabajando, debe seguir en estado de trabajo.
 Podria hacer otro bloque (un cofre) que combinase automaticamente objetos con daño.
 
 
*/

