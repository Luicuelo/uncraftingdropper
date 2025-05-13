package es.luiscuesta.uncraftingdropper.common.tileentity;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentString;
import java.util.Map;
import java.util.Map.Entry;

import es.luiscuesta.uncraftingdropper.Uncraftingdropper;
import es.luiscuesta.uncraftingdropper.common.config.TTConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import net.minecraftforge.common.util.Constants;


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
	private static final Map<String,  List<RecipeComponent>>recipeCache = new HashMap<>();
	// A cache for simple decompiling recipes
	private static final Map<String,  Map.Entry<ItemStack,Integer>>descompCache = new HashMap<>();
	//private static final Map<String, ItemStack> descompToOriginalCache=new HashMap<>();
	
	
	
	private static String getRecipeCacheKey(ItemStack stack) {
	    return stack.getItem().isDamageable() 
	        ? stack.getItem().getRegistryName().toString() 
	        : stack.getItem().getRegistryName() + "@" + stack.getMetadata();
	}
	
	
	/*
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
	}*/
	

	
	//gets a RecipeCacheKey and true if the stack can be used to recreate the original stack
	private static boolean isReversible(ItemStack stack) {
		
		String key = getRecipeCacheKey(stack);
		Entry<ItemStack, Integer> dcomponent = descompCache.get(key);
		return (dcomponent != null);
	}
	
	
	//Sort a recipe cache entry, reversible first
	private static List<RecipeComponent> sortComponents(List<RecipeComponent> components) {
		
		for (int i = 0; i < components.size(); i++) {
			RecipeComponent component = components.get(i);
			ItemStack itemStack = component.getItemStack();
			if (isReversible(itemStack)) continue;
			//skip all reversible components
			
			//here is not reversible, so will find the first reversible as swap
			int j;
			for (j = i + 1; j < components.size(); j++) {
				RecipeComponent nextComponent = components.get(j);
				ItemStack nextItemStack = nextComponent.getItemStack();
				if (isReversible(nextItemStack)) {
					// Swap the two components					
					components.set(i, nextComponent);
					components.set(j, component);				
					break;
				}
			}				
			//if not more reversible, break
			if (j == components.size()) {
				break;
			}
		}
		
		return components;
	}
	
	
    // Initialize the cache
    public static void initializeCache() {
    	Uncraftingdropper.logger.info("------------------initializeCache------------------");
        for (IRecipe recipe : CraftingManager.REGISTRY) {        
            ItemStack output = recipe.getRecipeOutput();
            if (output != null && !output.isEmpty()) {
            	
            	if(output.getCount()==1) {
            		String key = getRecipeCacheKey(output);
            		//only add if not exists
            		if (!recipeCache.containsKey(key)) {
						//Uncraftingdropper.logger.info(output+":Added to recipeCache: " + key + " -> " + recipe);
            			List<RecipeComponent> components = computeComponents(recipe);
						recipeCache.put(key, components);
					}            		
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
            			//descompToOriginalCache.put(getRecipeCacheKey(output), ingredientStack);
            			//Uncraftingdropper.logger.info(output+":Added to descompCache: " + key + " -> " + descompCache.get(key));
            		}
            	}
            }     
        }
        
        loadCustomRecipes();
        
        Iterator<Map.Entry<String, Map.Entry<ItemStack, Integer>>> iterator = descompCache.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Map.Entry<ItemStack, Integer>> descompCacheEntry = iterator.next();
            String stackKey = descompCacheEntry.getKey();
            List<RecipeComponent> recipeCacheList = recipeCache.get(stackKey);
            if (recipeCacheList == null ||  recipeCacheList.isEmpty()) {
                //Uncraftingdropper.logger.info("Removing from descompCache: " + stackKey + " -> " + descompCacheEntry.getValue().getKey());
                iterator.remove(); // Safely remove the entry
            } else {
            	
            	List<RecipeComponent> originalComponents = getComponentsFromCache(stackKey);
            	          	
                int componentQuantity = originalComponents.get(0).quantity;

                if (componentQuantity != descompCacheEntry.getValue().getValue()) {
                   // Uncraftingdropper.logger.info("Removing from descompCache: " + stackKey + " -> " + descompCacheEntry.getValue().getKey());
                    iterator.remove(); // Safely remove the entry
                    //descompToOriginalCache.remove(getRecipeCacheKey(originalComponents.get(0).getItemStack()));
                }
            }
        }         
        
        // sort all getRecipeCacheKeys using sortComponents            
        for (Entry<String, List<RecipeComponent>> entry : recipeCache.entrySet()) {
			String key = entry.getKey();
			List<RecipeComponent> components = entry.getValue();
			// Sort the components
			List<RecipeComponent> sortedComponents = sortComponents(components);
			// Update the cache with the sorted components
			recipeCache.put(key, sortedComponents);
		}
    }
    
    
    public static void loadCustomRecipes() {
    	
        //get ConfigDir
        Path configDir = Minecraft.getMinecraft().mcDataDir.toPath().resolve("config");               
     
        Path filePath = configDir.resolve("uncraftingdropper/recipes.txt");

        if (!Files.exists(filePath)) {
            System.out.println("File 'recipes.txt' does not exist. Skipping custom recipes loading.");
            return;
        }

        try {
        	
        	String content = new String(Files.readAllBytes(filePath));
            NBTTagCompound nbtData = JsonToNBT.getTagFromJson(content);

            NBTTagList recipesList = nbtData.getTagList("Recipes", Constants.NBT.TAG_COMPOUND);

            for (NBTBase recipeBase : recipesList) {
                if (recipeBase instanceof NBTTagCompound) {
                    NBTTagCompound recipeTag = (NBTTagCompound) recipeBase;

                    // Parse the input item
                    NBTTagCompound inputTag = recipeTag.getCompoundTag("inputItem");
                    ItemStack inputStack = new ItemStack(inputTag);
                    String key = UncraftHelper.getRecipeCacheKey(inputStack);
                    
                    // if exists, skip
                    if (recipeCache.containsKey(key)) {
						System.out.println("Recipe already exists for: " + key);
						continue;
					}

                    // Parse the output items
                    NBTTagList outputList = recipeTag.getTagList("outputItems", Constants.NBT.TAG_COMPOUND);
                    List<UncraftHelper.RecipeComponent> components = new ArrayList<>();

                    for (NBTBase outputBase : outputList) {
                        if (outputBase instanceof NBTTagCompound) {
                            NBTTagCompound outputTag = (NBTTagCompound) outputBase;
                            ItemStack outputStack = new ItemStack(outputTag);
                            int count = outputStack.getCount();
                            outputStack.setCount(1); // Set the output count to 1 for the cache
                            components.add(new UncraftHelper.RecipeComponent(outputStack, count));
                        }
                    }

                    // Add the recipe to the cache
                  
                    UncraftHelper.recipeCache.put(key, components);
                    System.out.println("Added custom recipe: " + key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Failed to parse custom recipes from 'recipes.txt': " + e.getMessage());
        }
    }
    
    
    
    public static RecipeComponent getSmallerComponent(ItemStack stack) {
    	Map.Entry<ItemStack, Integer> mapEntry  = descompCache.get(getRecipeCacheKey(stack));
    	if(mapEntry!=null) {    		
    		return new RecipeComponent(mapEntry.getKey(),mapEntry.getValue());    		
    	}
    	return null;
    	
    }
    
    /*
    public static RecipeComponent getBiggerStack(ItemStack stack) {
       	
       	String biggerKey=getRecipeCacheKey(descompToOriginalCache.get(getRecipeCacheKey(stack)));
       	if (biggerKey!=null && !biggerKey.isEmpty()) {
       		Map.Entry<ItemStack, Integer> mapEntry  = descompCache.get(biggerKey);
       		return new RecipeComponent(getStackFromRecipeCacheKey(biggerKey),mapEntry.getValue());
       	}
       	return null;
    }*/
    
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
        List<RecipeComponent> components = getComponentsFromCache(keyStack);

        boolean notEmpty = false;
        // Adjust each component's quantity based on the reductions
        for (int i = 0; i < components.size(); i++) {
        	
            RecipeComponent component = components.get(i);
            ItemStack itemStack = component.getItemStack();            
            RecipeComponent smaller=getSmallerComponent(itemStack);
            boolean hasSmaller=(smaller!=null);
            int quantity = component.getQuantity();
            
            if (hasSmaller) { 
            	ItemStack smallerItemStack = smaller.getItemStack();
            	int smallerQuantity=smaller.getQuantity();
            	quantity=quantity*smallerQuantity;
            	
            	//search into the i+1 to end of the list for smaller components, if found add the quantiy and set the quantity to 0
            	for (int j = i + 1; j < components.size(); j++) {
					RecipeComponent nextComponent = components.get(j);
					ItemStack nextItemStack = nextComponent.getItemStack();
					if (nextItemStack.isItemEqual(smallerItemStack)) {
						quantity +=  nextComponent.getQuantity();
						// set i component quantity to 0
						components.get(j).setQuantity(0);
					}
				}
            	
            	
            	// 	Calculate the final quantity after applying reductions
            	int finalQuantity = (int) (quantity * probabilityReduction);

            	int integerPart = finalQuantity / smallerQuantity;
            	int decimalPart = finalQuantity % smallerQuantity;
            	// 	Update the component's quantity
                // Add the decimal part as a separate component if necessary
            	
            	//(TTConfig.comsumeItem)
            	
            	RecipeComponent recipeComponent=new RecipeComponent(itemStack,integerPart);         
            	
            	if(integerPart>0) {
					notEmpty=true;
					adjustedComponents.add (recipeComponent);
            	}else if(TTConfig.comsumeItem) { // add even is 0					   	
					adjustedComponents.add (recipeComponent);
            	}
            	
            	RecipeComponent decimalComponent = new RecipeComponent(smaller.getItemStack(), decimalPart);   
                if (decimalPart > 0) {
                	notEmpty=true;
                	adjustedComponents.add (decimalComponent);
            	}else if(TTConfig.comsumeItem) { // add even is 0					   	
					adjustedComponents.add (decimalComponent);
            	}
                
            }else
            {
            	int finalQuantity = (int) (quantity * probabilityReduction);
            	RecipeComponent recipeComponent=new RecipeComponent(itemStack,finalQuantity);   
            	 if (finalQuantity > 0) {
            		 notEmpty=true;                	
            		 adjustedComponents.add (recipeComponent);
            	 }else if(TTConfig.comsumeItem) { // add even is 0					   	
 					adjustedComponents.add (recipeComponent);
             	}
            }
                        
        }
        
        if (!notEmpty&&!TTConfig.comsumeItem) {
			// If the adjusted components are empty, return an empty list
			return adjustedComponents;
		}
        
        //if the stsck is enchanted, we need to extract the books gets a list of enchanted books
        List<ItemStack> enchantedBooks = extractEnchantedBooks(stack);
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
        
        return adjustedComponents;
    }

    

    
    public static List<RecipeComponent> computeComponents(IRecipe recipe) {
       
    	List<RecipeComponent> components = new ArrayList<>();
 

    
        if (recipe == null || recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            return components; // Return an empty list if no recipe is found
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


        return components;
    }


    
    private static List<RecipeComponent>  getComponentsFromCache(ItemStack stack) {
		String key = getRecipeCacheKey(stack);
		return recipeCache.get(key);
	}
    
    private static List<RecipeComponent>  getComponentsFromCache(String key) {
		return recipeCache.get(key);
	}
   

    
    public static void debugPrintIngredients(EntityPlayer player,ItemStack stack,  List<UncraftHelper.RecipeComponent> components) {
   
        player.sendMessage(new TextComponentString("Ingredients for: " + stack.getDisplayName()));
        for (UncraftHelper.RecipeComponent component : components) {
            String itemName = component.getItemStack().getDisplayName();
            int quantity = component.getQuantity();
            player.sendMessage(new TextComponentString("- " + itemName + "; Quantity: " + quantity));
        }
    }
    
    
    public static void extractDefaultRecipesFile(Path configDir) {
        Path folderPath = configDir.resolve("uncraftingdropper");
        Path filePath = folderPath.resolve("recipes.txt");

        try {
            // Create the folder if it doesn't exist
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            // Copy the default file if it doesn't exist
            if (!Files.exists(filePath)) {
                try (InputStream inputStream = UncraftHelper.class.getResourceAsStream("/assets/uncraftingdropper/recipes.txt")) {
                    if (inputStream != null) {
                        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Default 'recipes.txt' extracted to 'uncraftingdropper' folder.");
                    } else {
                        System.err.println("Default 'recipes.txt' not found in mod resources.");
                    }
                }
            } else {
                //System.out.println("File 'recipes.txt' already exists.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void initializeRecipesFile(Path configDir) {
        // Define the folder and file paths inside the config directory
        Path folderPath = configDir.resolve("uncraftingdropper");
        Path filePath = folderPath.resolve("recipes.txt");

        try {
            // Create the folder if it doesn't exist
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
                System.out.println("Folder 'uncraftingdropper' created inside config directory.");
            }

            // Create the file if it doesn't exist
            if (!Files.exists(filePath)) {
                //Files.createFile(filePath);
            	extractDefaultRecipesFile(configDir);            	
                //System.out.println("File 'recipes.txt' created inside 'uncraftingdropper' folder.");
            } else {
                //System.out.println("File 'recipes.txt' already exists.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

