/*
 * Copyright (c) 2020. Katrina Knight, Luis Cuesta 2024
 */

package es.luiscuesta.uncraftingdropper.client.rendering;

import javax.annotation.Nullable;

import es.luiscuesta.uncraftingdropper.common.tileentity.TileEntityUncraftingdropper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;



@SideOnly(Side.CLIENT)
public class TileEntityUncraftingdropperRenderer extends TileEntitySpecialRenderer<TileEntityUncraftingdropper> {
	//private static FloatBuffer colorBuffer = GLAllocation.createDirectFloatBuffer(4);
	private static float rotation = 0.0F; // Static variable to share rotation across all instances


	@Override
	public void render(@Nullable TileEntityUncraftingdropper te, double x, double y, double z, float pticks, int digProgress, float unused) {

		
		if(te==null)return;		
		if(te.isStackEmpty()) return;
        ItemStack wrk=te.getStackCopy();
	    
	  	try {
	  		
	        // Increment the static rotation angle
	        rotation += 0.5F; // Adjust the increment value for desired speed
	        if (rotation >= 360.0F) {
	            rotation -= 360.0F; // Keep the rotation within 0-360 degrees
	        }
	  		drawItem(x, y+0.5, z,wrk);
						
			} catch (Exception e) {}
					///e.printStackTrace();					
	}
	
	private void drawItem(double x, double y, double z, ItemStack wrk ) {
			GlStateManager.pushMatrix();
		    GlStateManager.color(1.0F, 1.0F, 1.0F, 1F);
			GlStateManager.translate(x+ 0.5F, y+ 0.7F, z+ 0.5F);
			GlStateManager.scale(0.25F, 0.25F, 0.25F); // Scale down to half size
		    GlStateManager.rotate(rotation, 0.0F, 1.0F, 0.0F); // Rotate 45º around the Y-axis
			RenderItem rendemItem=Minecraft.getMinecraft().getRenderItem();
			if (rendemItem!=null) rendemItem.renderItem(wrk,  ItemCameraTransforms.TransformType.NONE);
			GlStateManager.popMatrix();	
	}
	

	

	
	
}
