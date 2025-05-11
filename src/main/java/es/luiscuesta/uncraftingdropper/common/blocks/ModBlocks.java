package es.luiscuesta.uncraftingdropper.common.blocks;

import es.luiscuesta.uncraftingdropper.common.libs.LibMisc;

import net.minecraftforge.fml.common.registry.GameRegistry;


@GameRegistry.ObjectHolder(LibMisc.MOD_ID)
public class ModBlocks {
	public static final BlockUncraftingdropper uncraftingdropper = new BlockUncraftingdropper();
	public static void init() {//ensures all objects has to be created.
		
	}
}
