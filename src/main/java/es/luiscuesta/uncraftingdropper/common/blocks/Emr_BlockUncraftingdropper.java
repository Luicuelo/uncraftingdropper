package es.luiscuesta.uncraftingdropper.common.blocks;

import es.luiscuesta.uncraftingdropper.common.libs.LibBlockNames;
import es.luiscuesta.uncraftingdropper.common.tileentity.UncraftHelper;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;


public class Emr_BlockUncraftingdropper extends BlockUncraftingdropper {


	 private static final int  TIER=5;
    public ResourceLocation resourceLocation;
	
	  public Emr_BlockUncraftingdropper() {
        		  
		  super(LibBlockNames.EMR_UNCRAFTINGDROPPER, Material.IRON);
		  setHardness(3.0F);
		  setResistance(8.0f);
    }
	  
	  @Override  
	public Color getColour() {
		//get a color similar to the block, cian for diamond, yellow for gold, grey for stone ....
		//we should return a color similar to diamond
		return new Color(100, 255, 100);
	}
    public  int  getTier() {
    	return TIER;
    }
    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        
        String processing="Processing Time: "+UncraftHelper.getProcessingTicks(TIER)+" ticks";
        String chance=UncraftHelper.getLossChance(TIER)+"% chance of item loss";
        String book=UncraftHelper.getBookProbability(TIER) + "% book recovery chance";
        
        tooltip.add(TextFormatting.GREEN + "Max Uncrafting");
        tooltip.add(TextFormatting.GRAY + processing);
        tooltip.add(TextFormatting.RED + chance);
        tooltip.add(TextFormatting.BLUE + book);
    }
}