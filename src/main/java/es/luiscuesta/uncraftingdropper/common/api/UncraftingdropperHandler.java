package es.luiscuesta.uncraftingdropper.common.api;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

@SuppressWarnings("deprecation")
public class UncraftingdropperHandler implements IRecipeHandler<UncraftingdropperWrapper> {

	 @Override
	    public Class<UncraftingdropperWrapper> getRecipeClass() {
	        return UncraftingdropperWrapper.class;
	    }

	    @Override
	    public String getRecipeCategoryUid(UncraftingdropperWrapper recipe) {
	        return UncraftingdropperCategory.UID;
	    }

	    @Override
	    public IRecipeWrapper getRecipeWrapper(UncraftingdropperWrapper recipe) {
	        return recipe;
	    }

	    @Override
	    public boolean isRecipeValid(UncraftingdropperWrapper recipe) {
	        return true; // Validate recipes here if needed
	    }

}


