package es.luiscuesta.uncraftingdropper.common.api;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import java.util.List;

import es.luiscuesta.uncraftingdropper.common.tileentity.UncraftHelper;

public class UncraftingdropperWrapper implements IRecipeWrapper {
    private final String key;
    private final ItemStack input ;
    
    public UncraftingdropperWrapper(ItemStack stack) {
    	input=stack;
    	key=UncraftHelper.getKey(input);
    }

    public ItemStack getInput() {
    	return input;
	}
    
    public List<ItemStack> getOutputs() {
    	
    	 return UncraftHelper.getComponentsFromCache(key);
    }
    

	@Override
    public void getIngredients(IIngredients ingredients) {
        // Set input and multiple outputs
        ingredients.setInput(VanillaTypes.ITEM, input);
        ingredients.setOutputs(VanillaTypes.ITEM, getOutputs());

    }


    

}