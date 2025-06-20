package es.luiscuesta.uncraftingdropper.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.Objects;

public  class RegistrationHandler {
    
    private final ArrayList<BlockTileEntity<?>> blocks = new ArrayList<>();
    private final ArrayList<BlockTileEntity<?>> itemBlocks = new ArrayList<>();
    
    
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


        for (BlockTileEntity<?> blockTileEntity : itemBlocks) {
            ItemBlock itemBlock = new ItemBlock(blockTileEntity);
            ResourceLocation rl = new ResourceLocation(blockTileEntity.getItemBlockName());
            itemBlock.setRegistryName(rl);
            itemBlock.setUnlocalizedName(blockTileEntity.getUnlocalizedName());
            registry.register(itemBlock);
        }

        for (BlockTileEntity<?> block : blocks) {
            if (block != null) {
                Class<? extends TileEntity> classTileEntity = block.getClassTileEntity();
                //if not yet register , register TileEntity
                try {
                    GameRegistry.registerTileEntity(classTileEntity, Objects.requireNonNull(block.getRegistryName()).toString());
                } catch
                (IllegalArgumentException e) {
                    //already registered
                }
            }

        }
    }


	public void registerModels(ModelRegistryEvent  event) {		
		for(int i = 0; i < itemBlocks.size(); i++) {
			itemBlocks.get(i).registerModels(event);
		}

		/*
		for(int i = 0; i < items.size(); i++) {
			items.get(i).registerModels();
		}*/
				
	}

}
