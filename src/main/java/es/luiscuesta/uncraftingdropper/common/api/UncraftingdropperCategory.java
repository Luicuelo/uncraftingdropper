package es.luiscuesta.uncraftingdropper.common.api;

import es.luiscuesta.uncraftingdropper.common.blocks.ModBlocks;
import es.luiscuesta.uncraftingdropper.common.libs.LibMisc;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class UncraftingdropperCategory implements IRecipeCategory<UncraftingdropperWrapper> {
    private static final int SLOT_SIZE = 18;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated arrow;
    public static final String UID = "uncraftingdropper:uncrafting";

    
    public UncraftingdropperCategory(IGuiHelper guiHelper) {

    	ResourceLocation location = new ResourceLocation("uncraftingdropper", "textures/gui/face.png");
        this.background = guiHelper.createDrawable(location, 0, 0, 144, 54);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.uncraftingdropper));
        
        IDrawableStatic arrowStatic  = guiHelper.createDrawable(location, 144, 0, 24, 17);
        this.arrow = guiHelper.createAnimatedDrawable(
                arrowStatic,
                200, // Total animation time in ticks
                IDrawableAnimated.StartDirection.LEFT,
                false // Do not show progress bar, show animation instead
            );
  
    }

    @Override
    public void setRecipe(IRecipeLayout layout, UncraftingdropperWrapper wrapper, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = layout.getItemStacks();
        
        // Input slot
        //itemStacks.init(0, true, PADDING, (background.getHeight() - SLOT_SIZE) / 2);
        itemStacks.init(0, true, 16, 2);
        
        // Output slots in a 3x3 grid
        int startX = 64;
        int startY = -1;
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                int index = 1 + (x * 3) + y;
                itemStacks.init(index, false, startX + (x * SLOT_SIZE), startY + (y * SLOT_SIZE));
            }
        }
        
        itemStacks.set(ingredients);
    }

    @Override
    public String getUid() {
        return UID;
    }
    
    @Override
    public IDrawable getIcon() {
		return icon;
    	
    }


	@Override
	public String getTitle() {
		 return I18n.format("Uncrafting Dropper");
	}

	@Override
	public String getModName() {
		return LibMisc.MOD_NAME;
	}

	@Override
	public IDrawable getBackground() {
		  return background;
	}


    public void drawExtras(Minecraft minecraft) {
        // Draw the arrow in the middle of the GUI
        arrow.draw(minecraft, 36, 18); // Adjust these coordinates as needed
    }

    public void draw(Minecraft minecraft, int recipeWidth, int recipeHeight, String probability) {
    	Minecraft.getMinecraft().fontRenderer.drawString(
                probability,
                44,
                42,
                0xFF808080,
                false
            );
        
    }

}