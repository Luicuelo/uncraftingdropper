/*
 * Luis Cuesta 2024
 */

package es.luiscuesta.uncraftingdropper.client.rendering;

import es.luiscuesta.uncraftingdropper.common.blocks.BlockUncraftingdropper;
import es.luiscuesta.uncraftingdropper.common.tileentity.TileEntityUncraftingdropper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;



@SideOnly(Side.CLIENT)
public class TileEntityUncraftingdropperRenderer extends TileEntitySpecialRenderer<TileEntityUncraftingdropper> {
    @SideOnly(Side.CLIENT)
    
    public static class ParticleEnchantmentTable extends Particle
    {

        private final double cordX;
        private final double cordY;
        private final double cordZ;

        protected ParticleEnchantmentTable(World worldIn, double xCordIn, double yCordIn, double zCordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn,Color color)
        {
            super(worldIn, xCordIn, yCordIn, zCordIn, xSpeedIn, ySpeedIn, zSpeedIn);
            this.motionX = xSpeedIn;
            this.motionY = ySpeedIn;
            this.motionZ = zSpeedIn;
            this.cordX = xCordIn;
            this.cordY = yCordIn;
            this.cordZ = zCordIn;
            this.prevPosX = xCordIn + xSpeedIn;
            this.prevPosY = yCordIn + ySpeedIn;
            this.prevPosZ = zCordIn + zSpeedIn;
            this.posX = this.prevPosX;
            this.posY = this.prevPosY;
            this.posZ = this.prevPosZ;
            float f = this.rand.nextFloat() * 0.6F + 0.4F;
            this.particleScale = this.rand.nextFloat() * 0.2F + 0.1F;
            
            
            float f_red= color.getRed()/255.0F;
            float f_green =color.getGreen()/255.0F;
            float f_blue =color.getBlue()/255.0F;
                        
            this.particleRed =  f_red * f;
            this.particleGreen = f_green * f;
            this.particleBlue = f_blue * f;
                   		
            this.particleMaxAge = (int)(Math.random() * 10.0D) + 20;
            this.setParticleTextureIndex((int)(Math.random() * 8.0D + 1.0D + 144.0D));
            
            
        }

        public void move(double x, double y, double z)
        {
            this.setBoundingBox(this.getBoundingBox().offset(x, y, z));
            this.resetPositionToBB();
        }

        public void onUpdate()
        {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            float f = ((float)this.particleAge / (float)this.particleMaxAge)/3.0F;
            f = 1.0F - f;
            float f1 = 1.0F - f;
            f1 = f1 * f1;
            f1 = f1 * f1;
            this.posX = this.cordX + this.motionX * (double)f;
            this.posY = this.cordY + this.motionY * (double)f - (double)(f1 * 1.2F);
            this.posZ = this.cordZ + this.motionZ * (double)f;
            this.particleAlpha = 0.5F - ((float)this.particleAge / (float)this.particleMaxAge)/2.0F;
            
            if (this.particleAge++ >= this.particleMaxAge)
            {
                this.setExpired();
            }
        }
    }
    

	//private static FloatBuffer colorBuffer = GLAllocation.createDirectFloatBuffer(4);
	private static float rotation = 0.0F; // Static variable to share rotation across all instances


	@Override
	public void render(@Nullable TileEntityUncraftingdropper te, double x, double y, double z, float ticks, int digProgress, float unused) {
		if(te==null)return;		
		if(te.isStackEmpty()) return;
        ItemStack wrk=te.getStackCopy();
	    
	  	try {
	        // Increment the static rotation angle
	        rotation += 0.5F; // Adjust the increment value for desired speed
	        if (rotation >= 360.0F) {
	            rotation -= 360.0F; // Keep the rotation within 0-360 degrees
	        }

			// saves actual el lightmap
			int lastBrightnessX = GL11.glGetInteger(GL11.GL_LIGHT0);
			int lastBrightnessY = GL11.glGetInteger(GL11.GL_LIGHT1);
			// forces max lightmap
			net.minecraft.client.renderer.OpenGlHelper.setLightmapTextureCoords(
					//net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit, 240f, 240f
					net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit, 200f, 200f
			);

			GlStateManager.enableLighting();
			net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();

			drawWithTransparency(x, y+0.5, z,wrk);

			net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
			GlStateManager.disableLighting();

			// restores actual
			net.minecraft.client.renderer.OpenGlHelper.setLightmapTextureCoords(
					net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);

	        World world = te.getWorld();
	        if(world.isRemote)
	        {
				BlockPos pos = te.getPos();
	        	BlockUncraftingdropper block=te.getBlock();
	        	if (block!=null) renderParticles(world,pos.getX(),pos.getY(),pos.getZ(),block.getColour());
	        }
		} catch (Exception ignored) {}
	}
	
	private void  renderParticles(World world,double px, double py, double pz, Color color) {
		
	    // Spawn particles only on client side
	    //if (isTitleTick()) {
	    if (world.getTotalWorldTime() % 10 == 0) {
	        double x = px+ 0.5;
	        double y = py + 1.3;
	        double z = pz + 0.5;
	        
	        // Spawn particles around the item
	        for (int i = 0; i < 2; i++) {
	            double offsetX = (world.rand.nextDouble() - 0.5) * 0.3;
	            double offsetY = (world.rand.nextDouble() - 0.5) * 0.3;
	            double offsetZ = (world.rand.nextDouble() - 0.5) * 0.3;
	           
	            /*
	            world.spawnParticle(
	            		EnumParticleTypes.SPELL_WITCH,
	                x + offsetX, y + offsetY, z + offsetZ,
	                0, 0.05, 0
	            );*/

	            ParticleEnchantmentTable particle = new ParticleEnchantmentTable(world, x + offsetX, y + offsetY, z + offsetZ, 0, 0.01, 0, color);
		        Minecraft.getMinecraft().effectRenderer.addEffect(particle);
	        }
	    }
	}

	@SuppressWarnings("unused")
	private void drawItem(double x, double y, double z, ItemStack wrk ) {
		GlStateManager.pushMatrix();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1F);
			GlStateManager.translate(x+ 0.5F, y+ 0.7F, z+ 0.5F);
			GlStateManager.scale(0.25F, 0.25F, 0.25F); // Scale down to half size
			GlStateManager.rotate(rotation, 0.0F, 1.0F, 0.0F); // Rotate around the Y-axis

			Minecraft.getMinecraft().getRenderItem().renderItem(wrk,  ItemCameraTransforms.TransformType.NONE);
		GlStateManager.popMatrix();
	}

	private void drawWithTransparency(double x, double y, double z, ItemStack stack) {
	    Minecraft mc = Minecraft.getMinecraft();
	    IBakedModel model = mc.getRenderItem().getItemModelWithOverrides(stack, null, null);

	    GlStateManager.pushMatrix();

	    float scale = 0.25F;
	    float centerOffset = 0.5F * scale;

	    // Base position
	    float floatY = (float) (Math.sin(rotation / 20F) * 0.05);
	    GlStateManager.translate(x + 0.5F - centerOffset, y + 0.7F + floatY, z + 0.5F - centerOffset);

	    // Rotate
	    GlStateManager.translate(centerOffset, 0F, centerOffset);
	    GlStateManager.rotate(rotation, 0F, 1F, 0F);
	    GlStateManager.translate(-centerOffset, 0F, -centerOffset);
	    GlStateManager.scale(scale, scale, scale);

	    mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
	    Tessellator tessellator = Tessellator.getInstance();
	    BufferBuilder buffer = tessellator.getBuffer();

	    GlStateManager.enableBlend();

	    // Increase base alpha to make item more solid
	    float alphaFloat = (float)((Math.sin(rotation / 10F) * 0.1F) + 0.9F);
	    int alphaInt = (int)(alphaFloat * 170.0F);
	    
	    int overlayColor = (alphaInt << 24) | 0x00FFFFFF;  
	    
	    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
	   
	    for (EnumFacing side : EnumFacing.values()) {
	        for (BakedQuad quad : model.getQuads(null, side, 0)) {
	        	LightUtil.renderQuadColor(buffer, quad, overlayColor);	        	
	        }
	    }
	    for (BakedQuad quad : model.getQuads(null, null, 0)) {
	    	LightUtil.renderQuadColor(buffer, quad, overlayColor);
	    }
	    tessellator.draw();


	    GlStateManager.disableBlend();
	    GlStateManager.popMatrix();
	    

	}


	
}




/*    public static class ParticleAura extends net.minecraft.client.particle.Particle {
        public ParticleAura(World world, double x, double y, double z, double vx, double vy, double vz) {
            super(world, x, y, z, vx, vy, vz);
            
            this.particleMaxAge = 40 + world.rand.nextInt(20);
            this.setSize(0.1F, 0.1F);
            this.particleScale = 1.0F; // Make it larger
            
            this.motionX = vx;
            this.motionY = vy;
            this.motionZ = vz;
                       
            this.particleGravity = 0;
            
            // Bright blue color
            this.particleRed = 0.3F;
            this.particleGreen = 0.7F;
            this.particleBlue = 1.0F;
            
            // Use a texture that definitely exists in Minecraft 1.12
            this.setParticleTexture(Minecraft.getMinecraft().getTextureMapBlocks()           	
              .getAtlasSprite("minecraft:particle/particles"));
            
            
        }

        @Override
        public int getFXLayer() {
            return 1; // or 0, try both
        }
        
        @Override
        public void onUpdate() {
            super.onUpdate();
            if (this.particleAge++ >= this.particleMaxAge) {
                this.setExpired();
            }
            this.particleAlpha = 1.0F - ((float)this.particleAge / (float)this.particleMaxAge);
            this.motionX *= 0.95;
            this.motionY *= 0.95;
            this.motionZ *= 0.95;
        }
    }

*/
