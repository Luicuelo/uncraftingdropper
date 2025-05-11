package es.luiscuesta.uncraftingdropper.common.tileentity;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Map;
import java.util.Map.Entry;

import es.luiscuesta.uncraftingdropper.Uncraftingdropper;
import es.luiscuesta.uncraftingdropper.common.config.TTConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class UncraftHelper {


    public static class RecipeComponent {
        private final ItemStack itemStack;
        private int quantity;

        
        public RecipeComponent() {
        
        	this.itemStack =ItemStack.EMPTY;
        	this.quantity=0;
        }
        public RecipeComponent(ItemStack itemStack, int quantity) {
            this.itemStack = itemStack;
            this.quantity = quantity;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public int getQuantity() {
            return quantity;
        }
        
        public void setQuantity(int quantity) {
			this.quantity = quantity;
		}
    }
	
    //recipes for output
	private static final Map<String,  List<IRecipe>>recipeCache = new HashMap<>();
	// A cache for simple decompiling recipes
	private static final Map<String,  Map.Entry<ItemStack,Integer>>descompCache = new HashMap<>();
	private static final Map<String, ItemStack> descompToOriginalCache=new HashMap<>();
	
	
	
	private static String getRecipeCacheKey(ItemStack stack) {
	    return stack.getItem().isDamageable() 
	        ? stack.getItem().getRegistryName().toString() 
	        : stack.getItem().getRegistryName() + "@" + stack.getMetadata();
	}
	
	private static ItemStack getStackFromRecipeCacheKey(String key) {
	    // Split the key into the registry name and metadata
	    String[] parts = key.split("@");
	    if (parts.length != 2) {
	        throw new IllegalArgumentException("Invalid key format: " + key);
	    }

	    String registryName = parts[0];
	    int metadata;
	    try {
	        metadata = Integer.parseInt(parts[1]);
	    } catch (NumberFormatException e) {
	        throw new IllegalArgumentException("Invalid metadata in key: " + key, e);
	    }

	    // Get the item from the registry name
	    Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(registryName));
	    if (item == null) {
	        throw new IllegalArgumentException("Item not found for registry name: " + registryName);
	    }

	    // Create and return the ItemStack
	    return new ItemStack(item, 1, metadata);
	}
	//get stack from key
	
	
	
    // Initialize the cache
    public static void initializeCache() {
    	Uncraftingdropper.logger.info("------------------initializeCache------------------");
        for (IRecipe recipe : CraftingManager.REGISTRY) {        
            ItemStack output = recipe.getRecipeOutput();
            if (output != null && !output.isEmpty()) {
            	if(output.getCount()==1) {
            		String key = getRecipeCacheKey(output);
            		recipeCache.computeIfAbsent(key, k -> new ArrayList<>()).add(recipe);
            	}
            	
            	if(output.getCount()>1 && recipe.getIngredients().size()==1 ) {
            		Ingredient ingredient = recipe.getIngredients().get(0);
            		ItemStack ingredientStacks[] = ingredient.getMatchingStacks();
            		if (ingredientStacks.length == 0) {
						continue; // Skip if no matching stacks
					}
            		
            		ItemStack ingredientStack =ingredientStacks[0];
					String key = getRecipeCacheKey(ingredientStack);           
					int count = output.getCount();
					output.setCount(1); // Set the output count to 1 for the cache    
					if (!descompCache.containsKey(key)) {
            			descompCache.put(key, new AbstractMap.SimpleEntry<>(output, count));
            			descompToOriginalCache.put(getRecipeCacheKey(output), ingredientStack);
            			//Uncraftingdropper.logger.info(output+":Added to descompCache: " + key + " -> " + descompCache.get(key));
            		}
            	}
            }            		            
        }
        
        
        Iterator<Map.Entry<String, Map.Entry<ItemStack, Integer>>> iterator = descompCache.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Map.Entry<ItemStack, Integer>> descompCacheEntry = iterator.next();
            String stackKey = descompCacheEntry.getKey();
            List<IRecipe> recipeCacheList = recipeCache.get(stackKey);
            if (recipeCacheList == null || recipeCacheList.isEmpty() || recipeCacheList.size() != 1) {
                //Uncraftingdropper.logger.info("Removing from descompCache: " + stackKey + " -> " + descompCacheEntry.getValue().getKey());
                iterator.remove(); // Safely remove the entry
            } else {
            	
            	List<RecipeComponent> originalComponents = computeComponents(stackKey);
            	          	
                int componentQuantity = originalComponents.get(0).quantity;

                if (componentQuantity != descompCacheEntry.getValue().getValue()) {
                   // Uncraftingdropper.logger.info("Removing from descompCache: " + stackKey + " -> " + descompCacheEntry.getValue().getKey());
                    iterator.remove(); // Safely remove the entry
                    descompToOriginalCache.remove(getRecipeCacheKey(originalComponents.get(0).getItemStack()));
                }
            }
        }         
    }
    
    public static RecipeComponent getSmallerComponent(ItemStack stack) {
    	Map.Entry<ItemStack, Integer> mapEntry  = descompCache.get(getRecipeCacheKey(stack));
    	if(mapEntry!=null) {    		
    		return new RecipeComponent(mapEntry.getKey(),mapEntry.getValue());    		
    	}
    	return null;
    	
    }
    
    public static RecipeComponent getBiggerStack(ItemStack stack) {
       	
       	String biggerKey=getRecipeCacheKey(descompToOriginalCache.get(getRecipeCacheKey(stack)));
       	if (biggerKey!=null && !biggerKey.isEmpty()) {
       		Map.Entry<ItemStack, Integer> mapEntry  = descompCache.get(biggerKey);
       		return new RecipeComponent(getStackFromRecipeCacheKey(biggerKey),mapEntry.getValue());
       	}
       	return null;
    }
    
    /* 
     Recorrer la lista descompCache y borrar los que no sean reversibles
     Crear otra lista para buscar los componentes
	 
     
     List<RecipeComponent> originalComponents = computeComponents(ingredientStack);
				    if (originalComponents.isEmpty()|| originalComponents.size() != 1) {
				           continue;
				    }

				    RecipeComponent component=originalComponents.get(0);
				    ItemStack componentStack = component.getItemStack();
				    int componentQuantity = component.getQuantity(); 
					
				    if (output.getUnlocalizedName()!=componentStack.getUnlocalizedName()||componentQuantity!=count) continue;
		
     
      */
	
    /**
     * Retrieves all recipes that produce the given ItemStack as output.
     * @param output The desired output ItemStack.
     * @return A list of recipes that produce the given output.
     */

    // Retrieve recipes for a specific output
    public static List<IRecipe> getRecipesForOutput(ItemStack output) {
        return getRecipesForOutput(getRecipeCacheKey(output));
        
    }
    
    public static List<IRecipe> getRecipesForOutput(String output) {
        return recipeCache.getOrDefault(output, new ArrayList<>());
        
    }
    
    /**
     * Extracts enchanted books from an enchanted item.
     * @param stack The input ItemStack (must be enchanted).
     * @return A list of ItemStack enchanted books.
     */
    public static List<ItemStack> extractEnchantedBooks(ItemStack stack) {
        List<ItemStack> enchantedBooks = new ArrayList<>();

        if (stack == null || stack.isEmpty() || !stack.isItemEnchanted()) {
            return enchantedBooks;
        }

        // Get the enchantments from the item
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);

        // Create an enchanted book for each enchantment
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();

            ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantmentHelper.setEnchantments(Collections.singletonMap(enchantment, level), enchantedBook);
            enchantedBooks.add(enchantedBook);
        }

        return enchantedBooks;
    }
    
    
    public static List<RecipeComponent> computeComponentsWithDamageAndProbability(ItemStack stack) {
    	
    	List<RecipeComponent> adjustedComponents=new ArrayList<>();
        // Check if the stack is valid
        if (stack == null || stack.isEmpty()) {
        	return adjustedComponents;
        }

        // Get the recipe cache key for the stack
        String keyStack = getRecipeCacheKey(stack);

        //if the stsck is enchanted, we need to extract the books gets a list of enchanted books
        List<ItemStack> enchantedBooks = extractEnchantedBooks(stack);
        
        //for each enchanted book, we need to add it to the adjusted components depending of the Book Probability
        // caclulate the probability of getting an enchanted book
        
        int bookProbability = TTConfig.bookProbability;
     
        if (!enchantedBooks.isEmpty()) {
        	
        	//depending of the enchant mode , gets the first, random or all
        	if (TTConfig.enchantMode==1) {
				// Get the first enchanted book
				ItemStack enchantedBook = enchantedBooks.get(0);
				// Add the enchanted book to the adjusted components
				{ int randomValue = (int) (Math.random() * 100);
				  if (randomValue < bookProbability) adjustedComponents.add(new RecipeComponent(enchantedBook, 1));
				}
			} else if (TTConfig.enchantMode==2) {
				// Get a random enchanted book
				int randomIndex = (int) (Math.random() * enchantedBooks.size());
				ItemStack enchantedBook = enchantedBooks.get(randomIndex);
				// Add the enchanted book to the adjusted components
				{
				int randomValue = (int) (Math.random() * 100);
				if (randomValue < bookProbability) adjustedComponents.add(new RecipeComponent(enchantedBook, 1));
				}
			} else if (TTConfig.enchantMode==3) {
				// Add all enchanted books to the adjusted components
				for (ItemStack enchantedBook : enchantedBooks) {
					 int randomValue = (int) (Math.random() * 100);
					 if (randomValue < bookProbability) adjustedComponents.add(new RecipeComponent(enchantedBook, 1));
				}
			}        	
		}
        
        
        // Get the percentage of reduction from the damage of the stack
        int damage = stack.getItemDamage();
        int maxDamage = stack.getMaxDamage();

        // Calculate the percentage of damage
        float damagePercentage = (1.0f -((maxDamage > 0) ? (float) damage / (float) maxDamage : 0.0f));

        // Apply damage percentage to the fixed reduction, and then apply the probability reduction
        float fixedReduction =  ((1.0f -((float) TTConfig.fixedReduction / 100.0f)) * damagePercentage);
        //get probabilityReduction with math ramdom
        		// Ensure the probability reduction is between 0 and 1
       
        float probabilityReduction = (1.0f -(float) (((float) TTConfig.probabilityReduction)* Math.random() / 100.0f))*fixedReduction; 
               // Compute the components using the recipe cache key
        List<RecipeComponent> components = computeComponents(keyStack);

        // Adjust each component's quantity based on the reductions
        for (int i = 0; i < components.size(); i++) {
        	
            RecipeComponent component = components.get(i);
            ItemStack itemStack = component.getItemStack();            
            RecipeComponent smaller=getSmallerComponent(itemStack);
            boolean hasSmaller=(smaller!=null);
            int quantity = component.getQuantity();
            if (hasSmaller) { 
            	int smallerQuantity=smaller.getQuantity();
            	quantity=quantity*smallerQuantity;
            	// 	Calculate the final quantity after applying reductions
            	int finalQuantity = (int) (quantity * probabilityReduction);

            	int integerPart = finalQuantity / smallerQuantity;
            	int decimalPart = finalQuantity % smallerQuantity;
            	// 	Update the component's quantity
                // Add the decimal part as a separate component if necessary
            	
            	RecipeComponent recipeComponent=new RecipeComponent(itemStack,integerPart);            	
            	adjustedComponents.add (recipeComponent);
                if (decimalPart > 0) {
                    RecipeComponent decimalComponent = new RecipeComponent(smaller.getItemStack(), decimalPart);                	    	
                	adjustedComponents.add (decimalComponent);
                }
            }else
            {
            	int finalQuantity = (int) (quantity * probabilityReduction);
            	RecipeComponent recipeComponent=new RecipeComponent(itemStack,finalQuantity);            	
            	adjustedComponents.add (recipeComponent);
            }
                        
        }

        return adjustedComponents;
    }

    
    public static List<RecipeComponent> computeComponents(ItemStack stack) {
       
        // Check if the stack is valid
        if (stack == null || stack.isEmpty()) {
        	List<RecipeComponent> components = new ArrayList<>();
            return components;
        }
        return computeComponents (getRecipeCacheKey(stack));
    }
    
    
    public static List<RecipeComponent> computeComponents(String keyStack) {
       
    	List<RecipeComponent> components = new ArrayList<>();
        List<IRecipe> recipes = getRecipesForOutput(keyStack);

        for (IRecipe recipe : recipes) {
            if (recipe == null || recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
                continue;
            }
            
            //if we get more than one result, we need to divide the quantity of each component by the result count
            int resultCount=recipe.getRecipeOutput().getCount();
           
            // Check if the recipe is a shaped recipe
            // Create a temporary list to store components
 
            if (recipe instanceof ShapedRecipes) {
                ShapedRecipes shapedRecipe = (ShapedRecipes) recipe;
                Map<String,Map.Entry<ItemStack, Integer>> ingredientCounts = new HashMap<>();

                for (Ingredient ingredient : shapedRecipe.getIngredients()) {
                    ItemStack[] matchingStacks = ingredient.getMatchingStacks();
                    if (matchingStacks != null && matchingStacks.length > 0) {
                        ItemStack ingredientStack = matchingStacks[0].copy();
                        //if key dont exists, create it, else , sum count
                        String key = getRecipeCacheKey(ingredientStack);                        
                        if (ingredientCounts.containsKey(key)) {
							ingredientCounts.get(key).setValue(ingredientCounts.get(key).getValue() + ingredientStack.getCount());
						} else {
							ingredientCounts.put(getRecipeCacheKey(ingredientStack), new AbstractMap.SimpleEntry<>(ingredientStack, ingredientStack.getCount()));
						}
                                              
                    }
                }

                for (Map.Entry<String,Map.Entry<ItemStack,Integer>> entry : ingredientCounts.entrySet()) {           
                	components.add(new RecipeComponent(entry.getValue().getKey(), entry.getValue().getValue()/resultCount));                	
                }
            } else {
                for (Ingredient ingredient : recipe.getIngredients()) {
                    ItemStack[] matchingStacks = ingredient.getMatchingStacks();
                    if (matchingStacks != null && matchingStacks.length > 0) {
                    	                    	
                        ItemStack ingredientStack = matchingStacks[0].copy();
                        int quantity = ingredientStack.getCount(); // Use the actual quantity
                        ingredientStack.setCount(1); // Set to 1 for the component
                        components.add(new RecipeComponent(ingredientStack, quantity/resultCount));                        
                    }
                }
            }

            break; // Stop once a recipe is found
            
        }

        return components;
    }


    
   
    public static boolean isReversible(ItemStack originalStack) {
        if (originalStack == null || originalStack.isEmpty() ) {
            return false;
        }

        List<RecipeComponent> originalComponents = computeComponents(originalStack);
        if (originalComponents.isEmpty()|| originalComponents.size() != 1) {
            return false;
        }

        RecipeComponent component=originalComponents.get(0);
        
        // Check if each component can be used to recreate the original stack
        ItemStack componentStack = component.getItemStack();
        int componentQuantity = component.getQuantity();

       if (componentStack == null || componentStack.isEmpty()) {
			return false;
		}

        // Check if the component can be used to recreate the original stack
        String Key= getRecipeCacheKey(originalStack);
        Entry<ItemStack, Integer> dcomponent=descompCache.get(Key);
        String originalKey= getRecipeCacheKey(componentStack);
        return (dcomponent != null && dcomponent.getKey() != null && getRecipeCacheKey(dcomponent.getKey()).equals(originalKey) && dcomponent.getValue() == componentQuantity);
    }
    
    public static void debugPrintIngredients(EntityPlayer player,ItemStack stack,  List<UncraftHelper.RecipeComponent> components) {
   
        player.sendMessage(new TextComponentString("Ingredients for: " + stack.getDisplayName()));
        for (UncraftHelper.RecipeComponent component : components) {
            String itemName = component.getItemStack().getDisplayName();
            int quantity = component.getQuantity();
            player.sendMessage(new TextComponentString("- " + itemName + "; Quantity: " + quantity));
        }
    }
    
}

