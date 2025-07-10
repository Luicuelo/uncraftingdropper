package es.luiscuesta.uncraftingdropper.common.tileentity;

import es.luiscuesta.uncraftingdropper.Uncraftingdropper;
import es.luiscuesta.uncraftingdropper.common.config.TTConfig;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.Constants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.Map.Entry;

/**
 * Helper class for uncrafting operations in the mod.
 * Manages recipe caching, component calculation, and custom recipe loading.
 */

public class UncraftHelper {

	/** Cache mapping item keys to their component ingredients */
	private static final Map<String, List<ItemStack>> recipeCache = new HashMap<>();
	
	/** Cache of items that can be decomposed into smaller components */
	private static final List<String> decompCache = new ArrayList<>();
	
	/** List of custom recipe input items */
	private static final List<ItemStack> customRecipesKeys = new ArrayList<>();

	/**
	 * Gets the list of custom recipe input items.
	 * 
	 * @return List of ItemStacks representing custom recipe inputs
	 */
	public static List<ItemStack> getCustomRecipesKeys() {
		return customRecipesKeys;
	}

	/**
	 * Creates a copy of component items for a given recipe key.
	 * 
	 * @param key The recipe key to look up
	 * @return A new list containing copies of the component items
	 */
	public static List<ItemStack> getComponentsCopyFromCache(String key) {
		List<ItemStack> componentsList = recipeCache.get(key);
		List<ItemStack> components = new ArrayList<>();
		if (componentsList == null)
			return components;

		for (ItemStack itemStack : componentsList) {
			if (itemStack == null || itemStack.isEmpty())
				continue;
			components.add(itemStack.copy());
		}
		return components;
	}

	/**
	 * Gets the original component list for a given recipe key.
	 * 
	 * @param key The recipe key to look up
	 * @return The list of component items or null if not found
	 */
	public static List<ItemStack> getComponentsFromCache(String key) {
		return recipeCache.get(key);
	}

	/**
	 * Creates an ItemStack from a recipe cache key string.
	 * 
	 * @param key The NBT string representation of an item
	 * @return The ItemStack created from the NBT data
	 */
	public static ItemStack getStackFromRecipeCacheKey(String key) {
		NBTTagCompound nbtTag = new NBTTagCompound();
		try {
			nbtTag = JsonToNBT.getTagFromJson(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return new ItemStack(nbtTag);
	}

	/**
	 * Sets the quantity of an ItemStack and returns it.
	 * 
	 * @param stack The ItemStack to modify
	 * @param quantity The new quantity to set
	 * @return The modified ItemStack
	 */
	private static ItemStack setQuantity(ItemStack stack, int quantity) {
		stack.setCount(quantity);
		return stack;
	}

	/**
	 * Generates a unique key for an ItemStack, ignoring quantity and damage.
	 * Format example: {id:"minecraft:chainmail_leggings",Count:1b,Damage:0s}
	 * 
	 * @param stack The ItemStack to generate a key for
	 * @return String representation of the ItemStack as a key
	 */
	public static String getKey(ItemStack stack) {
		NBTTagCompound nbtTag = new NBTTagCompound();
		stack.writeToNBT(nbtTag);
		
		// Normalize the NBT data for consistent keys
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

	/**
	 * Checks if an ItemStack can be decomposed into smaller components.
	 * 
	 * @param stack The ItemStack to check
	 * @return true if the item is reversible, false otherwise
	 */
	private static boolean isReversible(ItemStack stack) {
		return (decompCache.contains(getKey(stack)));
	}

	/**
	 * Gets a copy of the smaller component for a decomposable ItemStack.
	 * 
	 * @param stack The ItemStack to get the component for
	 * @return A copy of the smaller component or null if not found
	 */
	public static ItemStack getSmallerComponentCopy(ItemStack stack) {
		String key = getKey(stack);
		if (decompCache.contains(key)) {
			return recipeCache.get(key).get(0).copy();
		}
		// if not exists, return null
		return null;
	}

	/**
	 * Sorts a list of components to prioritize reversible items first.
	 * This helps optimize the uncrafting process by handling decomposable items first.
	 * 
	 * @param components The list of ItemStack components to sort
	 * @return The sorted list with reversible components first
	 */
	private static List<ItemStack> sortComponents(List<ItemStack> components) {
		for (int i = 0; i < components.size(); i++) {
			ItemStack itemStack = components.get(i);
			if (isReversible(itemStack))
				continue;
			// skip all reversible components

			// here is not reversible, so will find the first reversible as swap
			int j;
			for (j = i + 1; j < components.size(); j++) {
				if (isReversible(components.get(j))) {
					// Swap the two components
					ItemStack tempItemStack = components.get(i);
					components.set(i, components.get(j));
					components.set(j, tempItemStack);
					break;
				}
			}
			// if not more reversible, break
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
			if (!output.isEmpty()) {

				int outputCount = output.getCount();
				if (outputCount == 1) {
					String key = getKey(output);
					// only add if not exists
					if (!recipeCache.containsKey(key)) {
						// Uncraftingdropper.logger.info(output+":Added to recipeCache: " + key + " -> "
						// + recipe);
						List<ItemStack> components = computeComponents(recipe);
						if (!components.isEmpty() && !getKey(components.get(0)).equals(key))
							// do not add if ingredient is equal than output
							recipeCache.put(key, components);
					}
				}
			}
		}

		for (IRecipe recipe : CraftingManager.REGISTRY) {
			ItemStack output = recipe.getRecipeOutput();
			if (!output.isEmpty()) {

				int outputCount = output.getCount();
				if (outputCount > 1 && recipe.getIngredients().size() == 1) {
					Ingredient ingredient = recipe.getIngredients().get(0);
					ItemStack[] ingredientStacks = ingredient.getMatchingStacks();
					if (ingredientStacks.length == 0) {
						continue; // Skip if no matching stacks
					}
					ItemStack bigger = ingredientStacks[0];
					String key = getKey(bigger);

					// check if bigger decomposition in recipeCachematch outputcount
					if (recipeCache.containsKey(key) && recipeCache.get(key).get(0).getCount() == outputCount
							&& !decompCache.contains(key)) {

						decompCache.add(key);
						// Uncraftingdropper.logger.info(output+":Added to descompCache: " + key + " ->
						// " + key);
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
			// if (key.equals("{id:\"minecraft:chainmail_leggings\",Count:1b,Damage:0s}")) {
			List<ItemStack> sortedComponents = sortComponents(components);
			// Update the cache with the sorted components
			recipeCache.put(key, sortedComponents);
		}
	}

	/**
	 * Loads custom uncrafting recipes from the configuration file.
	 * Custom recipes allow users to define their own uncrafting rules for items
	 * that don't have standard crafting recipes or need special handling.

	 * The recipes are stored in NBT format in the config/uncraftingdropper/recipes.txt file.
	 */
	public static void loadCustomRecipes() {
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
	 * 
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
	 * Calculates XP value from an enchanted book based on enchantment levels.
	 * 
	 * @param enchantedBook The enchanted book ItemStack
	 * @return The XP value to return
	 */
	public static int calculateXPFromEnchantedBook(ItemStack enchantedBook) {
		if (enchantedBook == null || enchantedBook.isEmpty() || enchantedBook.getItem() != Items.ENCHANTED_BOOK) {
			return 0;
		}

		Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(enchantedBook);
		int totalXP = 0;

		for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
			Enchantment enchantment = entry.getKey();
			int level = entry.getValue();
			
			// Base XP calculation: rarity affects XP value
			int baseXP = 1; // Base XP per level
			
			// Adjust based on enchantment rarity
			switch (enchantment.getRarity()) {
				case COMMON:
					baseXP = 1;
					break;
				case UNCOMMON:
					baseXP = 3;
					break;
				case RARE:
					baseXP = 5;
					break;
				case VERY_RARE:
					baseXP = 8;
					break;
			}
			
			// XP increases linearly with level
			totalXP += baseXP * level * 6;
		}

		return Math.max(1, totalXP); // Minimum 1 XP
	}

	/**
	 * Converts XP amount into XP bottles.
	 * 
	 * @param xpAmount The total XP to convert
	 * @return List of XP bottle ItemStacks
	 */
	public static void createXPBottles(List<ItemStack> xpBottles, int xpAmount) {

		// Each XP bottle gives 3-11 XP (average 7), we'll use 7 for calculation
		int bottleXP = 7;
		int bottleCount = Math.max(1, (xpAmount + bottleXP - 1) / bottleXP); // Round up
		
		// Create XP bottles
		while (bottleCount > 0) {
			int stackSize = Math.min(bottleCount, 64); // Max stack size for XP bottles
			ItemStack xpBottle = new ItemStack(Items.EXPERIENCE_BOTTLE, stackSize);
			xpBottles.add(xpBottle);
			bottleCount -= stackSize;
		}		
	}

	/**
	 * Computes components for uncrafting considering item damage and probability.
	 * Items with damage > 25% have reduced probability of returning components.
	 * Special handling for enchanted books - returns XP bottles instead.
	 *
	 * @param stack The ItemStack to compute components for
	 * @return List of ItemStacks representing the components with adjusted damage
	 *         and quantities
	 */
	public static List<ItemStack> computeComponentsWithDamageAndProbability(ItemStack stack, int tier) {

		List<ItemStack> adjustedComponents = new ArrayList<>();
		// Check if the stack is valid
		if (stack == null || stack.isEmpty()) {
			return adjustedComponents;
		}
		
		// Special handling for enchanted books - return XP bottles
		if (TTConfig.enableXPBottles && stack.getItem() == Items.ENCHANTED_BOOK) {
			int xpAmount = calculateXPFromEnchantedBook(stack);
			// Apply tier-based bonus and config multiplier to XP amount
			float tierMultiplier = 1.0f + (tier - 1) * 0.25f; // 1.0x, 1.25x, 1.5x, 1.75x for tiers 1-4
			xpAmount = Math.round(xpAmount * tierMultiplier * (float)TTConfig.xpMultiplier);
			
			createXPBottles(adjustedComponents,xpAmount);
			return adjustedComponents;
		}

		if (isReversible(stack)) {// is reversible, no reduction calculated
			adjustedComponents.add(getSmallerComponentCopy(stack));
			return adjustedComponents;
		}

		// Get the recipe cache key for the stack
		String keyStack = getKey(stack);

		// Get the percentage of reduction from the damage to the stack
		int damage = stack.getItemDamage();
		int maxDamage = stack.getMaxDamage();

		// Calculate the percentage of damage
		float damagePercentage = (1.0f - ((maxDamage > 0) ? (float) damage / (float) maxDamage : 0.0f));

		// Apply damage percentage to the fixed reduction, and then apply the
		// probability reduction
		float fixedReduction = ((1.0f - ((float) getLossChance(tier) / 100.0f)) * damagePercentage);
		// get probabilityReduction with math random
		// Ensure the probability reduction is between 0 and 1

		float probabilityReduction = (1.0f - (float) (((float) TTConfig.probabilityReduction) * Math.random() / 100.0f))
				* fixedReduction;
		// Compute the components using the recipe cache key
		List<ItemStack> components = getComponentsCopyFromCache(keyStack);
		boolean isEmpty = true;

		// Adjust each component's quantity based on the reductions
		for (int i = 0; i < components.size(); i++) {

			ItemStack itemStack = components.get(i);
			// if empty continue
			if (itemStack.isEmpty())
				continue;
			int quantity = itemStack.getCount();

			if (isReversible(itemStack)) {

				ItemStack smallerItemStack = getSmallerComponentCopy(itemStack);
				if (smallerItemStack == null) continue;
                int smallerQuantity = smallerItemStack.getCount();

                quantity = quantity * smallerQuantity;

				// search into the i+1 to end of the list for smaller components, if found add
				// the quantiy and set the quantity to 0
				for (int j = i + 1; j < components.size(); j++) {

					ItemStack nextItemStack = components.get(j);
					if (nextItemStack.isItemEqual(smallerItemStack)) {
						quantity += nextItemStack.getCount();
						// set i component quantity to 0
						components.get(j).setCount(0);
					}
				}

				// Calculate the final quantity after applying reductions
				int finalQuantity = (int) (quantity * probabilityReduction);
				int integerPart = finalQuantity / smallerQuantity;
				int decimalPart = finalQuantity % smallerQuantity;

				ItemStack recipeComponent = setQuantity(itemStack, integerPart);

				if (integerPart > 0) {
					isEmpty = false;
					adjustedComponents.add(recipeComponent);
				} else if (TTConfig.consumeItem) { // add even is 0
					adjustedComponents.add(recipeComponent);
				}

				ItemStack decimalComponent = setQuantity(smallerItemStack, decimalPart);
				if (decimalPart > 0) {
					isEmpty = false;
					adjustedComponents.add(decimalComponent);
				} else if (TTConfig.consumeItem) { // add even is 0
					adjustedComponents.add(decimalComponent);
				}

			} else {
				int finalQuantity = (int) (quantity * probabilityReduction);
				ItemStack recipeComponent = setQuantity(itemStack, finalQuantity);
				if (finalQuantity > 0) {
					isEmpty = false;
					adjustedComponents.add(recipeComponent);
				} else if (TTConfig.consumeItem) { // add even is 0
					adjustedComponents.add(recipeComponent);
				}
			}
		}

		// if the stsck is enchanted, we need to extract the books gets a list of
		// enchanted books
		List<ItemStack> enchantedBooks = extractEnchantedBooks(stack);
		int bookProbability = getBookProbability(tier);

		if (isEmpty) {
			// If the adjusted components are empty, return an empty list
			// if not enchantedBook return empty list
			// Only continue if ConsumeItem activated and try to get a book, but if not ,
			// the item will be lost
			if (!TTConfig.consumeItem || enchantedBooks.isEmpty())
				adjustedComponents.clear();
			return adjustedComponents;
		}

		if (!enchantedBooks.isEmpty()) {

			// depending on the enchant mode , gets the first, random or all
			if (TTConfig.enchantMode == 1) {
				// Get the first enchanted book
				ItemStack enchantedBook = enchantedBooks.get(0);
				// Add the enchanted book to the adjusted components
				{
					int randomValue = (int) (Math.random() * 100);
					if (randomValue < bookProbability)
						adjustedComponents.add(setQuantity(enchantedBook, 1));
				}
			} else if (TTConfig.enchantMode == 2) {
				// Get a random enchanted book
				int randomIndex = (int) (Math.random() * enchantedBooks.size());
				ItemStack enchantedBook = enchantedBooks.get(randomIndex);
				// Add the enchanted book to the adjusted components
				{
					int randomValue = (int) (Math.random() * 100);
					if (randomValue < bookProbability)
						adjustedComponents.add(setQuantity(enchantedBook, 1));
				}
			} else if (TTConfig.enchantMode == 3) {
				// Add all enchanted books to the adjusted components
				for (ItemStack enchantedBook : enchantedBooks) {
					int randomValue = (int) (Math.random() * 100);
					if (randomValue < bookProbability)
						adjustedComponents.add(setQuantity(enchantedBook, 1));
				}
			}
		}

		return adjustedComponents;
	}

	/**
	 * Computes the list of components required for a given recipe. Handles
	 * ingredient deduplication and quantity aggregation.
	 * 
	 * @param recipe The recipe to analyze
	 * @return List of ItemStacks representing the required components
	 */
	public static List<ItemStack> computeComponents(IRecipe recipe) {

		List<ItemStack> components = new ArrayList<>();
		if (recipe == null || recipe.getIngredients().isEmpty()) {
			return components; // Return an empty list if no recipe is found
		}

		Map<String, ItemStack> ingredientCounts = new HashMap<>();

		for (Ingredient ingredient : recipe.getIngredients()) {
			ItemStack[] matchingStacks = ingredient.getMatchingStacks();

			if (matchingStacks.length > 0) {
				ItemStack ingredientStack = matchingStacks[0].copy();
				// if key don't exist, create it, else , sum count
				String key = getKey(ingredientStack);
				int stackCount = ingredientStack.getCount();

				if (ingredientCounts.containsKey(key)) {
					ItemStack previousStack = ingredientCounts.get(key);
					previousStack.setCount(previousStack.getCount() + stackCount);

				} else {
					ingredientCounts.put(key, ingredientStack);
				}
			}
		}
		// return components from ingredientCounts gettin the values of ingredientCounts
		// as a list
		components.addAll(ingredientCounts.values());
		return components;
	}

	/**
	 * Sends a debug message to the player showing all ingredients for an item.
	 * 
	 * @param player The player to send the message to
	 * @param stack The ItemStack being analyzed
	 * @param components The list of component ItemStacks
	 */
	public static void debugPrintIngredients(EntityPlayer player, ItemStack stack, List<ItemStack> components) {
		player.sendMessage(new TextComponentString("Ingredients for: " + stack.getDisplayName()));
		for (ItemStack component : components) {
			String itemName = component.getDisplayName();
			int quantity = component.getCount();
			player.sendMessage(new TextComponentString("- " + itemName + "; Quantity: " + quantity));
		}
	}

	/**
	 * Extracts the default recipes file from mod resources to the config directory.
	 * 
	 * @param configDir The configuration directory path
	 */
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
				try (InputStream inputStream = UncraftHelper.class
						.getResourceAsStream("/assets/uncraftingdropper/recipes.txt")) {
					if (inputStream != null) {
						Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
						System.out.println("Default 'recipes.txt' extracted to 'uncraftingdropper' folder.");
					} else {
						System.err.println("Default 'recipes.txt' not found in mod resources.");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes the recipes file structure in the config directory.
	 * Creates the necessary folders and extracts the default recipes file if needed.
	 * 
	 * @param configDir The configuration directory path
	 */
	public static void initializeRecipesFile(Path configDir) {
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
				extractDefaultRecipesFile(configDir);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the processing time in ticks for a specific tier of uncrafting dropper.
	 * Higher tiers process items faster.
	 * 
	 * @param tier The tier level (1-4) of the uncrafting dropper
	 * @return The number of ticks between processing operations
	 */
	public static int getProcessingTicks(int tier) {
		switch (tier) {
		case 1: // Stone
			return 50;
		case 2: // Iron
			return 25;
		case 3: // Gold
			return 10;
		case 4: // Diamond
			return 5;
		default:
			return 0;
		}
	}

	/**
	 * Calculates the probability of extracting enchantment books based on tier.
	 * Higher tiers have better chances of preserving enchantments.
	 * 
	 * @param tier The tier level (1-4) of the uncrafting dropper
	 * @return The percentage chance (0-100) of extracting an enchantment book
	 */
	public static int getBookProbability(int tier) {
		float remainingProbability = (100f - TTConfig.bookProbability) / 4.0f;

		switch (tier) {
		case 1: // Stone
			return TTConfig.bookProbability;
		case 2: // Iron
			return TTConfig.bookProbability + Math.round(remainingProbability);
		case 3: // Gold
			return TTConfig.bookProbability + Math.round(remainingProbability * 2);
		case 4: // Diamond
			return TTConfig.bookProbability + Math.round(remainingProbability * 3.5f);
		default:
			return 0;
		}
	}

	/**
	 * Calculates the chance of losing components during uncrafting based on tier.
	 * Higher tiers have lower loss chances.
	 * 
	 * @param tier The tier level (1-4) of the uncrafting dropper
	 * @return The percentage (0-100) chance of component loss
	 */
	public static int getLossChance(int tier) {
		switch (tier) {
		case 1: // Stone
			return TTConfig.fixedReduction;
		case 2: // Iron
			return TTConfig.fixedReduction / 2;
		case 3: // Gold
			return TTConfig.fixedReduction / 4;
		case 4: // Diamond
			return TTConfig.fixedReduction / 10;
		default:
			return 0;
		}
	}
}
