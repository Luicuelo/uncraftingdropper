package es.luiscuesta.uncraftingdropper.common.blocks;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public  class RegistrationHandler {
    
    private ArrayList<BlockTileEntity<?>> blocks = new ArrayList<>();
    private ArrayList<BlockTileEntity<?>> itemBlocks = new ArrayList<>();
    
    
	public void addBlockForRegistry(BlockTileEntity<?> block) {
		blocks.add(block);
	}
	public void addBlockItemForRegistry(BlockTileEntity<?> block) {
		itemBlocks.add(block);
	}
    
    public  void registerBlocks(final RegistryEvent.Register<Block> event) {
        final IForgeRegistry<Block> registry = event.getRegistry();
		for(BlockTileEntity<?> b:blocks) {		
			b.setRegistryName(b.getResourceLocation());											
			registry.register(b);
		}
    }

    @SuppressWarnings("deprecation")
	public  void registerItems(final RegistryEvent.Register<Item> event) {

        IForgeRegistry<Item> registry = event.getRegistry();


		for(int i = 0; i < itemBlocks.size(); i++) {			
			ItemBlock itemBlock=new ItemBlock ((itemBlocks.get(i)));
			ResourceLocation rl=new ResourceLocation (itemBlocks.get(i).getItemBlockName()) ;
			itemBlock.setRegistryName(rl);
			itemBlock.setUnlocalizedName(itemBlocks.get(i).getUnlocalizedName());					
			registry.register(itemBlock);
		}

		for(int i = 0; i < blocks.size(); i++) {
			Block block =(blocks.get(i));
			if (block instanceof BlockTileEntity) {
				Class<? extends TileEntity> classTileEntity=((BlockTileEntity<?>)block).getClassTileEntity();
				
				//if not yet register , register TileEntity
				try {
					GameRegistry.registerTileEntity(classTileEntity, block.getRegistryName().toString());
				}catch
				(IllegalArgumentException e) {
					//already registered
				}	
			}
				
		} 
    }


	public void registerModels(ModelRegistryEvent  event) {		
		for(int i = 0; i < itemBlocks.size(); i++) {
			blocks.get(i).registerModels(event);
		}

		/*
		for(int i = 0; i < items.size(); i++) {
			items.get(i).registerModels();
		}*/
				
	}

}
