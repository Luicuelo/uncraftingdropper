package es.luiscuesta.uncraftingdropper.common.blocks;

import es.luiscuesta.uncraftingdropper.common.libs.LibMisc;
import net.minecraftforge.fml.common.registry.GameRegistry;


@GameRegistry.ObjectHolder(LibMisc.MOD_ID)
public class ModBlocks {
	public static final BlockUncraftingdropper uncraftingdropper =  new Stn_BlockUncraftingdropper();
	public static final Irn_BlockUncraftingdropper irn_uncraftingdropper =  new Irn_BlockUncraftingdropper();
	public static final Gld_BlockUncraftingdropper gld_uncraftingdropper =  new Gld_BlockUncraftingdropper();
	public static final Dmd_BlockUncraftingdropper dmd_uncraftingdropper =  new Dmd_BlockUncraftingdropper();
	public static final Emr_BlockUncraftingdropper emr_uncraftingdropper =  new Emr_BlockUncraftingdropper();	
	
	public static void init() {//ensures all objects has to be created.
		
	}
}
