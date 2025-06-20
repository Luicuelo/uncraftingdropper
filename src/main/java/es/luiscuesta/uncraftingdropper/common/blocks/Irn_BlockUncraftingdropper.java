package es.luiscuesta.uncraftingdropper.common.blocks;


import es.luiscuesta.uncraftingdropper.common.libs.LibBlockNames;
import es.luiscuesta.uncraftingdropper.common.tileentity.UncraftHelper;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

public class Irn_BlockUncraftingdropper extends BlockUncraftingdropper {

	 private static final int  TIER=2;
	 public Irn_BlockUncraftingdropper() {
        		  
		  super(LibBlockNames.IRN_UNCRAFTINGDROPPER, Material.IRON);
		  setHardness(3.0F);
		  setResistance(8.0f);
    }
	 
    public  int  getTier() {
    	return TIER;
    }
    
    @Override
    public Color getColour() {
        // Light gray/silver color for iron
        return new Color(192, 192, 192);
    }
    
    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        
        String processing="Processing Time: "+UncraftHelper.getProcessingTicks(TIER)+" ticks";
        String chance=UncraftHelper.getLossChance(TIER)+"% chance of item loss";
        String book=UncraftHelper.getBookProbability(TIER) + "% book recovery chance";
        
        tooltip.add(TextFormatting.WHITE + "Better Uncrafting");
        tooltip.add(TextFormatting.GRAY + processing);
        tooltip.add(TextFormatting.RED + chance);
        tooltip.add(TextFormatting.BLUE + book);
    }

}