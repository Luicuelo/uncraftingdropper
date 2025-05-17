package es.luiscuesta.uncraftingdropper.common.api;

import java.util.ArrayList;
import java.util.List;
import es.luiscuesta.uncraftingdropper.common.blocks.ModBlocks;
import es.luiscuesta.uncraftingdropper.common.tileentity.UncraftHelper;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.JEIPlugin;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;


@JEIPlugin
public class UncraftingdropperJEIPlugin implements IModPlugin {

    private static final ResourceLocation PLUGIN_UID = new ResourceLocation("uncraftingdropper", "jei_plugin");
    public static IRecipeRegistry recipeRegistry;

    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }


    
    @SuppressWarnings("deprecation")
	@Override
    public void register(IModRegistry registry) {
        // Register your recipe category and handler here
        registry.addRecipeCategories(new UncraftingdropperCategory(registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeHandlers(new UncraftingdropperHandler());
        
        // Add a catalyst (e.g., the block/item that opens your recipe category)
        registry.addRecipeCategoryCraftingItem(new ItemStack(ModBlocks.uncraftingdropper), UncraftingdropperCategory.UID);
        
        List<UncraftingdropperWrapper> recipes = new ArrayList<>();        
        List<ItemStack> customRecipeKeys = UncraftHelper.getCustomRecipesKeys();
        
        //add  custom recipes
        for (ItemStack input : customRecipeKeys) {
        	recipes.add(new UncraftingdropperWrapper(input));
        }
		
		// Add the recipes to JEI		
        registry.addRecipes(recipes, UncraftingdropperCategory.UID);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        // Use jeiRuntime to interact with JEI at runtime if needed
        // For example, you can access recipe categories or hide recipes
        System.out.println("JEI Runtime is available.");
    }

}

