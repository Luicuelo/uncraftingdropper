package es.luiscuesta.uncraftingdropper.common.tileentity;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import net.minecraftforge.common.util.Constants;


public class UncraftHelper {



	
    //recipes for output
	private static final Map<String,  List<ItemStack>>recipeCache = new HashMap<>();
	// A cache for simple decompiling recipes
	private static final List<String>descompCache = new ArrayList<>();
	//private static final Map<String, ItemStack> descompToOriginalCache=new HashMap<>();
	
	//list of custom recipes keys
	private static final List<ItemStack> customRecipesKeys = new ArrayList<>();
	
	public static final List<ItemStack> getCustomRecipesKeys(){
		return customRecipesKeys;
	}
	
	public static List<ItemStack>  getComponentsCopyFromCache(String key) {    	
		//create a copy of the list from the recipeCache
		List<ItemStack> componentsList =recipeCache.get(key);
		List<ItemStack> components = new ArrayList<>();
		if (componentsList==null) return components;
		
		for (ItemStack itemStack : componentsList) {
			if (itemStack==null||itemStack.isEmpty()) continue;
			components.add(itemStack.copy());
		}
		return components;			
	}
   
    public static List<ItemStack>  getComponentsFromCache(String key) {    	
    		return recipeCache.get(key);
    }
    
	public static ItemStack getStackFromRecipeCacheKey(String key) {
		//get 	NTB TagCompound from String representation
		NBTTagCompound nbtTag = new NBTTagCompound();
		try {
			nbtTag = JsonToNBT.getTagFromJson(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Create a new ItemStack from the NBT data
		ItemStack itemStack = new ItemStack(nbtTag);
		return itemStack;
	}
	
    private static ItemStack setQuantity(ItemStack stack, int quantity) {
    	stack.setCount(quantity);
        return stack;
    }
	
	public static String getKey(ItemStack stack) {
		//{id:"minecraft:chainmail_leggings",Count:1b,Damage:0s}
		
		NBTTagCompound nbtTag = new NBTTagCompound();
		stack.writeToNBT(nbtTag);
		// set count to 1 and damage to 0
		//remove tag if exists
		if (nbtTag.hasKey("tag")) {
			nbtTag.removeTag("tag");
		}
		if (nbtTag.hasKey("Count")) {
			nbtTag.setInteger("Count", 1);
		}
		if (nbtTag.hasKey("Damage")) {
			nbtTag.setInteger("Damage", 0);
		}
		return nbtTag.toString();
	}
	

	

	
	//gets a RecipeCacheKey and true if the stack can be used to recreate the original stack
	private static boolean isReversible(ItemStack stack) {		
		return (descompCache.contains(getKey(stack)));
	}
	
   public static ItemStack getSmallerComponentCopy (ItemStack stack) {
    	String key=getKey(stack);
    	if  (descompCache.contains(key)){
    		return recipeCache.get(key).get(0).copy();
    	}
    	//if not exists, return null
    	return null;
    }
	
	
	//Sort a recipe cache entry, reversible first
	private static List<ItemStack> sortComponents(List<ItemStack> components) {
		
		for (int i = 0; i < components.size(); i++) {
		
			ItemStack itemStack = components.get(i);
			if (isReversible(itemStack)) continue;
			//skip all reversible components
			
			//here is not reversible, so will find the first reversible as swap
			int j;
			for (j = i + 1; j < components.size(); j++) {

				if (isReversible(components.get(j))) {
					// Swap the two components
					ItemStack tempItemStack=components.get(i);
					components.set(i, components.get(j));
					components.set(j, tempItemStack);				
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
            	
            	int outputCount=output.getCount();
            	if(outputCount==1) {
            		String key = getKey(output);
            		//only add if not exists
            		if (!recipeCache.containsKey(key)) {
						//Uncraftingdropper.logger.info(output+":Added to recipeCache: " + key + " -> " + recipe);
            			List<ItemStack> components = computeComponents(recipe);            			
            			if (components.size()>0&&!getKey(components.get(0)).equals(key))
            			//do not add if ingredient is equal than output
            				recipeCache.put(key, components);
					}            		
            	}            	
            }     
        }
        
        for (IRecipe recipe : CraftingManager.REGISTRY) {        
            ItemStack output = recipe.getRecipeOutput();
            if (output != null && !output.isEmpty()) {
            	
            	int outputCount=output.getCount();            	      
		    	if(outputCount>1 && recipe.getIngredients().size()==1 ) {
		    		Ingredient ingredient = recipe.getIngredients().get(0);
		    		ItemStack ingredientStacks[] = ingredient.getMatchingStacks();
		    		if (ingredientStacks.length == 0) {
						continue; // Skip if no matching stacks
					}		    		
		    		ItemStack bigger =ingredientStacks[0];		    				    		
					String key = getKey(bigger);          
					
					//check if bigger decomposition in  recipeCachematch outputcount	
					if (recipeCache.containsKey(key) && recipeCache.get(key).get(0).getCount() == outputCount
							&& !descompCache.contains(key)) {

		    			descompCache.add(key);
		    			//Uncraftingdropper.logger.info(output+":Added to descompCache: " + key + " -> " + key);
		    		}
		    	}
            }     
        } 
        
        loadCustomRecipes();
        
        // sort all getRecipeCacheKeys using sortComponents            
        for (Entry<String, List<ItemStack>> entry : recipeCache.entrySet()) {
			String key = entry.getKey();
			List<ItemStack> components = entry.getValue();
			// Sort the components
			//if (key.equals("{id:\"minecraft:chainmail_leggings\",Count:1b,Damage:0s}")) {
			List<ItemStack> sortedComponents = sortComponents(components);
			// Update the cache with the sorted components
			recipeCache.put(key, sortedComponents);			
		}
    }
    
    
    public static void loadCustomRecipes() {
    	
        //get ConfigDir
    	
        //Path configDir = Minecraft.getMinecraft().mcDataDir.toPath().resolve("config");                    
        //Path filePath = configDir.resolve("uncraftingdropper/recipes.txt");
        // Use FMLPaths for side-independent config directory access

    	Path configDir = new File("config").toPath();
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
                    String key = UncraftHelper.getKey(inputStack);
                    
                    // if exists, skip
                    if (recipeCache.containsKey(key)) {
						System.out.println("Recipe already exists for: " + key);
						continue;
					}

                    // Parse the output items
                    NBTTagList outputList = recipeTag.getTagList("outputItems", Constants.NBT.TAG_COMPOUND);
                    List<ItemStack> components = new ArrayList<>();

                    for (NBTBase outputBase : outputList) {
                        if (outputBase instanceof NBTTagCompound) {
                            NBTTagCompound outputTag = (NBTTagCompound) outputBase;
                            ItemStack outputStack = new ItemStack(outputTag);
                                         
                            components.add(outputStack);
                        }
                    }

                    // Add the recipe to the cache
                    customRecipesKeys.add(inputStack);
                    recipeCache.put(key, components);
                    System.out.println("Added custom recipe: " + key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Failed to parse custom recipes from 'recipes.txt': " + e.getMessage());
        }
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
    
    
    /**
     * Computes components for uncrafting considering item damage and probability.
     * Items with damage > 25% have reduced probability of returning components.
     *
     * @param stack The ItemStack to compute components for
     * @return List of ItemStacks representing the components with adjusted damage and quantities
     */
    public static List<ItemStack> computeComponentsWithDamageAndProbability(ItemStack stack, int tier) {
    	
    	List<ItemStack> adjustedComponents=new ArrayList<>();
        // Check if the stack is valid
        if (stack == null || stack.isEmpty()) {
        	return adjustedComponents;
        }
                        
        if (isReversible(stack)) {//is reversible, no reduction calulated
        	adjustedComponents.add(getSmallerComponentCopy(stack));
        	return adjustedComponents;        	
        }

        // Get the recipe cache key for the stack
        String keyStack = getKey(stack);
        
        // Get the percentage of reduction from the damage of the stack
        int damage = stack.getItemDamage();
        int maxDamage = stack.getMaxDamage();

        // Calculate the percentage of damage
        float damagePercentage = (1.0f -((maxDamage > 0) ? (float) damage / (float) maxDamage : 0.0f));

        // Apply damage percentage to the fixed reduction, and then apply the probability reduction
        float fixedReduction =  ((1.0f -((float) getLossChance(tier) / 100.0f)) * damagePercentage);
        //get probabilityReduction with math ramdom
        		// Ensure the probability reduction is between 0 and 1
       
        float probabilityReduction = (1.0f -(float) (((float) TTConfig.probabilityReduction)* Math.random() / 100.0f))*fixedReduction; 
               // Compute the components using the recipe cache key
        List<ItemStack> components = getComponentsCopyFromCache(keyStack);
        boolean isEmpty=true;
       
        // Adjust each component's quantity based on the reductions
        for (int i = 0; i < components.size(); i++) {
        	
            
            ItemStack itemStack = components.get(i);    
            // if empty continue
            if (itemStack.isEmpty()) continue;          
            int quantity = itemStack.getCount();
            
            if (isReversible(itemStack)) { 
          
            	ItemStack smallerItemStack  =getSmallerComponentCopy(itemStack);;
            	int smallerQuantity=smallerItemStack.getCount();
            	quantity=quantity*smallerQuantity;
            	
            	//search into the i+1 to end of the list for smaller components, if found add the quantiy and set the quantity to 0
            	for (int j = i + 1; j < components.size(); j++) {
            
					ItemStack nextItemStack = components.get(j);
					if (nextItemStack.isItemEqual(smallerItemStack)) {
						quantity +=  nextItemStack.getCount();
						// set i component quantity to 0
						components.get(j).setCount(0);
					}
				}
           	            	
            	// 	Calculate the final quantity after applying reductions
            	int finalQuantity = (int) (quantity * probabilityReduction);
            	int integerPart = finalQuantity / smallerQuantity;
            	int decimalPart = finalQuantity % smallerQuantity;

            	ItemStack recipeComponent= setQuantity(itemStack,integerPart);         
            	
            	if(integerPart>0) {				
            		isEmpty=false;
					adjustedComponents.add (recipeComponent);
            	}else if(TTConfig.comsumeItem) { // add even is 0					   	
					adjustedComponents.add (recipeComponent);
            	}
            	
            	ItemStack decimalComponent = setQuantity(smallerItemStack, decimalPart);   
                if (decimalPart > 0) {
                	isEmpty=false;
                	adjustedComponents.add (decimalComponent);
            	}else if(TTConfig.comsumeItem) { // add even is 0					   	
					adjustedComponents.add (decimalComponent);
            	}
                
            }else
            {
            	int finalQuantity = (int) (quantity * probabilityReduction);
            	ItemStack recipeComponent=setQuantity(itemStack,finalQuantity);   
            	 if (finalQuantity > 0) {   
            		 isEmpty=false;
            		 adjustedComponents.add (recipeComponent);
            	 }else if(TTConfig.comsumeItem) { // add even is 0					   	
 					adjustedComponents.add (recipeComponent);
             	}
            }                        
        }
        
        
        

        
        //if the stsck is enchanted, we need to extract the books gets a list of enchanted books
        List<ItemStack> enchantedBooks = extractEnchantedBooks(stack);
        int bookProbability =  getBookProbability(tier);

        
        if (isEmpty) {
			// If the adjusted components are empty, return an empty list
        	//if not enchantedBook return empty list
        	//Only continue if ConsumeItem activated and try to get a book, but if not , the item will be lost
			if (!TTConfig.comsumeItem||enchantedBooks.isEmpty()) adjustedComponents.clear();			
			return adjustedComponents;		
		}
        
        
        if (!enchantedBooks.isEmpty()) {
        	
        	//depending of the enchant mode , gets the first, random or all
        	if (TTConfig.enchantMode==1) {
				// Get the first enchanted book
				ItemStack enchantedBook = enchantedBooks.get(0);
				// Add the enchanted book to the adjusted components
				{ int randomValue = (int) (Math.random() * 100);
				  if (randomValue < bookProbability) adjustedComponents.add( setQuantity(enchantedBook, 1));
				}
			} else if (TTConfig.enchantMode==2) {
				// Get a random enchanted book
				int randomIndex = (int) (Math.random() * enchantedBooks.size());
				ItemStack enchantedBook = enchantedBooks.get(randomIndex);
				// Add the enchanted book to the adjusted components
				{
				int randomValue = (int) (Math.random() * 100);
				if (randomValue < bookProbability) adjustedComponents.add( setQuantity(enchantedBook, 1));
				}
			} else if (TTConfig.enchantMode==3) {
				// Add all enchanted books to the adjusted components
				for (ItemStack enchantedBook : enchantedBooks) {
					 int randomValue = (int) (Math.random() * 100);
					 if (randomValue < bookProbability) adjustedComponents.add( setQuantity(enchantedBook, 1));
				}
			}        	
		}
        
        return adjustedComponents;
    }

    

    /**
     * Computes the list of components required for a given recipe.
     * Handles ingredient deduplication and quantity aggregation.
     * 
     * @param recipe The recipe to analyze
     * @return List of ItemStacks representing the required components
     */
    public static List<ItemStack> computeComponents(IRecipe recipe) {
       
    	List<ItemStack> components = new ArrayList<>();
        if (recipe == null || recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            return components; // Return an empty list if no recipe is found
        }
        
        Map<String,ItemStack> ingredientCounts = new HashMap<>();
        
        for (Ingredient ingredient : recipe.getIngredients()) {
            ItemStack[] matchingStacks = ingredient.getMatchingStacks();
            
            
            if (matchingStacks != null && matchingStacks.length > 0) {
                ItemStack ingredientStack = matchingStacks[0].copy();
                //if key dont exists, create it, else , sum count
                String key = getKey(ingredientStack);                        
                int stackCount=ingredientStack.getCount();
            	
                if (ingredientCounts.containsKey(key)) {                
                	ItemStack previousStack= ingredientCounts.get(key);
                	previousStack.setCount(previousStack.getCount() + stackCount);                    	
					
				} else {					
					ingredientCounts.put(key, ingredientStack);						
				}                                          
            }
        }
        //return components from ingredientCounts gettin the values of ingredientCounts as a list
        components.addAll(ingredientCounts.values());             
        return components;
    }


    


    
    public static void debugPrintIngredients(EntityPlayer player,ItemStack stack,  List<ItemStack> components) {
   
        player.sendMessage(new TextComponentString("Ingredients for: " + stack.getDisplayName()));
        for (ItemStack component : components) {
            String itemName = component.getDisplayName();
            int quantity = component.getCount();
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
    
    public static int getProcessingTicks(int tier) {
    	//base value is 5
        switch (tier) {
        
        	case 1: // Stone
        		
        		return 50;
        
            case 2: // Iron

                return 25;


            case 3: // Gold
                return 10;


            case 4: // Diamond
                return 5;

        }
        return 0;
    }

    public static int getBookProbability(int tier) {
    	
    	float remainingProbability= (100f -TTConfig.bookProbability)/4.0f;
    	
        switch (tier) { 
    	case 1: // Stone

    		return TTConfig.bookProbability;
    		
        case 2: // Iron

            return TTConfig.bookProbability + Math.round(remainingProbability) ;

        case 3: // Gold
            return TTConfig.bookProbability + Math.round(remainingProbability*2);

        case 4: // Diamond
            return TTConfig.bookProbability + Math.round(remainingProbability*3.5f);

        }
		return 0;
    }
    
     public static int getLossChance(int tier) {
        switch (tier) { 
        	case 1: // Stone

        		return TTConfig.fixedReduction;
        
            case 2: // Iron

                return TTConfig.fixedReduction/2;


            case 3: // Gold
                return TTConfig.fixedReduction/4;


            case 4: // Diamond
                return TTConfig.fixedReduction/10;

        }
        return 0;
    }
}

