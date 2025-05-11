/*
 * Copyright (c) 2025. Luis Cuesta
 */

package es.luiscuesta.uncraftingdropper.proxy;

import es.luiscuesta.uncraftingdropper.Uncraftingdropper;
import es.luiscuesta.uncraftingdropper.client.rendering.TileEntityUncraftingdropperRenderer;
import es.luiscuesta.uncraftingdropper.common.tileentity.TileEntityUncraftingdropper;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Client side proxy
 */

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends ICommonProxy {

	public void registerRenderers() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityUncraftingdropper.class, new TileEntityUncraftingdropperRenderer());       
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
    	super.preInit(event);
    	registerRenderers();
    }
    
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent event) {
		Uncraftingdropper.modRegistry.registerModels(event);
	}
	
	/*
	@SubscribeEvent
	public static void onTextureStitch(TextureStitchEvent.Pre event) {

		 	TextureStitchEvent.Pre textures =(TextureStitchEvent.Pre) event ;
		 	ResourceLocation location;
		 	
		 	//location=new ResourceLocation(LibMisc.MOD_ID,"blocks/uncraftingdropper/wrk_side");
            //textures.getMap().registerSprite(location);
      
	 }*/
	
    
    @Override
    public String localize(String translationKey, Object... args) {
        return I18n.format(translationKey, args);
    }

	@Override
	public void init(FMLInitializationEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postInit(FMLPostInitializationEvent e) {
		// TODO Auto-generated method stub
		
	}
    

}
