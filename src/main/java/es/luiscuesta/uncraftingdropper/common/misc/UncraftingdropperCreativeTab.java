package es.luiscuesta.uncraftingdropper.common.misc;

import es.luiscuesta.uncraftingdropper.common.blocks.ModBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;


public class UncraftingdropperCreativeTab extends CreativeTabs {
    public UncraftingdropperCreativeTab() {
        super("uncraftingdropper");
    }
   
    
    @Nonnull
    public ItemStack createIcon() {
    	return new ItemStack(ModBlocks.uncraftingdropper);
    }

	@Nonnull
    @Override
	public ItemStack getTabIconItem() {
		return new ItemStack(ModBlocks.uncraftingdropper);
	}
		
}

