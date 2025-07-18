package es.luiscuesta.uncraftingdropper.common.tileentity;

import es.luiscuesta.uncraftingdropper.common.blocks.BlockUncraftingdropper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TileEntityUncraftingdropper extends TileEntityBase implements ITickable {

	private List<ItemStack> currentComponents = new java.util.ArrayList<>(); // empty list
	private final MyItemStackHandler inventory = new MyItemStackHandler();
	public BlockPos inventoryPos = null;
	private int lastComparatorSignal = 0;
	private int ticksUpdate = 20;
	private int ticksElapsed = 0;
	private BlockUncraftingdropper block;

	public TileEntityUncraftingdropper() {
		super();
	}
		
	public BlockUncraftingdropper getBlock() {
		return block;
	}
	
	public boolean isStackEmpty() {
		return inventory.isStackEmpty();
	}

	public ItemStack getStackCopy() {
		return inventory.getStackCopy();
	}

	public void insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		 inventory.insertItem(slot, stack, simulate);
	}

	public boolean currentComponentsIfEmpty() {
		 return currentComponents == null || currentComponents.isEmpty();
	}

	public void sendUpdates() {
		if (world == null || world.isRemote) return; // Check if the world is not null and not remote
		IBlockState state = world.getBlockState(pos);
		if (!(state.getBlock() instanceof BlockUncraftingdropper)) return;
		BlockUncraftingdropper block = (BlockUncraftingdropper) state.getBlock();

		boolean working = !this.isStackEmpty();
		block.setNewState(pos, world, working);
		/*
			boolean change = !block.setNewState(pos, world, working); // Notify the block to update its state
		 	System.out.println("Current stack"+inventory.getStackCopy().getDisplayName()
		*/
		markDirty();

		world.notifyBlockUpdate(pos, state, state, 2);
		world.markBlockRangeForRenderUpdate(pos, pos);

	}

	// function to serialize NBT currentComponents
	public NBTTagCompound serializeNBTCurrentComponents() {
		NBTTagCompound nbt = new NBTTagCompound();
		if (currentComponents != null) {
			for (int i = 0; i < currentComponents.size(); i++) {
				NBTTagCompound componentTag = new NBTTagCompound();
				ItemStack component = currentComponents.get(i);
				componentTag.setTag("item", component.serializeNBT());
				componentTag.setInteger("quantity", component.getCount());
				nbt.setTag("component" + i, componentTag);
			}
		}
		return nbt;
	}

	// function to deserialize NBT currentComponents
	public void deserializeNBTCurrentComponents(NBTTagCompound nbt) {
		if (currentComponents != null)
			currentComponents.clear();
		else
			currentComponents = new java.util.ArrayList<>(); // empty list
		
		
		for (int i = 0; nbt.hasKey("component" + i); i++) {
			NBTTagCompound componentTag = nbt.getCompoundTag("component" + i);
			ItemStack itemStack = new ItemStack(componentTag.getCompoundTag("item"));
			currentComponents.add(itemStack);
		}
	}

	// decorations for only server side

	public void writeExtraNBT(NBTTagCompound nbttagcompound) {

		nbttagcompound.setTag("inventory", inventory.serializeNBT());
		nbttagcompound.setTag("currentComponents", serializeNBTCurrentComponents());
		// inventory.logMessage("World Server: "+!getWorld().isRemote + " Item stored in
		// writeFromNBT:"+inventory.getStackName());
	}

	public void readExtraNBT(NBTTagCompound nbttagcompound) {

		inventory.deserializeNBT(nbttagcompound.getCompoundTag("inventory"));
		deserializeNBTCurrentComponents(nbttagcompound.getCompoundTag("currentComponents"));
	}

	private class MyItemStackHandler implements IItemHandler, IItemHandlerModifiable, INBTSerializable<NBTTagCompound> {

		private ItemStack internalStack = ItemStack.EMPTY;

		@Override
		public int getSlotLimit(int slot) {
			return 1;
		}

		public MyItemStackHandler() {

		}

		private boolean isStackEmpty() {
			return (internalStack == null || internalStack.isEmpty());
		}

		@SuppressWarnings("unused")
		private int getStackCount() {
			return internalStack.getCount();
		}

		@SuppressWarnings("unused")
		private String getStackName() {
			return internalStack.getDisplayName();
		}

		@Override
		public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
			// logMessage("setStackInSlot:"+stack.getDisplayName());
			internalStack = stack.copy();
			onContentsChanged();
		}

		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
			if (getWorld().isRemote)
				return stack; // Check if the world is not null and not remote
			ItemStack inserted;
			if (!isItemValidForSlot(slot, stack))
				return stack;
			// if(!simulate)logMessage("--------------------------------------------------------");
			// if(!simulate)logMessage("insertItem:"+stack.getDisplayName());
			if (!isStackEmpty())
				return stack;

			ItemStack stackCopy = stack.copy();
			int quantity = stack.getCount();
			if (simulate) {
				stackCopy.setCount(quantity - 1);
				inserted = stackCopy;
				if (stack.getCount() <= 0)
					inserted = ItemStack.EMPTY;
			} else {
				stackCopy.setCount(1);
				setStackInSlot(slot, stackCopy); // Store the item in the slot
				stack.shrink(1);
				if (stack.getCount() <= 0)
					stack = ItemStack.EMPTY; // Set the inserted stack to empty if count is 0
				inserted = stack;
				// logMessage("->Stored:"+getStackName());
				// logMessage(currentComponentsIfEmpty()+"-> stack count "+getStackCount()+"
				// list size:"+currentComponents.size());
			}

			return inserted;
		}

		@SuppressWarnings("unused")
		private void logMessage(String msg) {
			// EntityPlayer player = world.getClosestPlayer(pos.getX(), pos.getY(),
			// pos.getZ(), 10, false);
			// if (player != null) player.sendMessage(new TextComponentString(msg));
			System.out.println(msg);
		}

		protected void onContentsChanged() {

			if (getWorld().isRemote)
				return; 
			
			// logMessage("onContentsChanged------------------:"+getStackName());			
			if(block!=null) {
				currentComponents = UncraftHelper.computeComponentsWithDamageAndProbability(getStackCopy(), block.getTier());
				sendUpdates();
			}
		}

		public boolean isItemValidForSlot(int index, ItemStack stack) {
			return TileEntityUncraftingdropper.this.isItemValidForSlot(index, stack);
		}

		@Override
		public NBTTagCompound serializeNBT() {
			// Serialize internalStack
			NBTTagCompound nbt = new NBTTagCompound();
			if (internalStack == null)
				internalStack = ItemStack.EMPTY;
			nbt.setTag("internalStack", internalStack.writeToNBT(new NBTTagCompound()));
			// System.out.println("Server:" + !getWorld().isRemote + " SerializeNBT:" +
			// internalStack.toString());
			return nbt; // Return the NBTTagCompound
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			// Check if the "internalStack" tag exists
			if (nbt.hasKey("internalStack")) {
				NBTTagCompound internalStackNBT = nbt.getCompoundTag("internalStack");
				internalStack = new ItemStack(internalStackNBT); // Deserialize the stack
				// System.out.println(getWorld().isRemote + " DeserializeNBT: " +
				// internalStack.toString());
			} else {
				internalStack = ItemStack.EMPTY; // Default to empty if no tag is found
			}
		}

		@Override
		public int getSlots() {
			return 1;
		}

		@Nonnull
		@Override
		public ItemStack getStackInSlot(int slot) {
			if (slot == 0) {
				return internalStack.copy();
			}
			return ItemStack.EMPTY;
		}

		public void empty() {
			internalStack = ItemStack.EMPTY;
			onContentsChanged();
		}

		public ItemStack getStackCopy() {
			return internalStack.copy();
		}

		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return ItemStack.EMPTY;
		}

	}



	public boolean isTitleTick() {

		return ((ticksElapsed % ticksUpdate == 1));
	}

	public int comparatorSignal() {

		boolean signalHasChanged;
		

		// if inventory not empty, signal =15
    	int signal = !inventory.isStackEmpty() ? 15 : 0;

		signalHasChanged = (lastComparatorSignal != signal);
		lastComparatorSignal = signal;

	    if (signalHasChanged) {
	        sendUpdates();
	    }
		return signal;
	}
	
	private boolean getRedstonePowered() {
		if (world == null) return false;
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block instanceof BlockUncraftingdropper) {
			return state.getValue(BlockUncraftingdropper.POWER);
		}
		return false;
	}

	public boolean isItemValidForSlot(int index, ItemStack stack) {

		return index == 0 && !stack.isEmpty() && stack.getCount() > 0 && !getRedstonePowered();

	}

	public int getSpeed() {
		return ticksUpdate;
	}

	public void setSpeed(int speed) {
		this.ticksUpdate = speed;
	}

	@SuppressWarnings("unused")
	private MyItemStackHandler getInventory() {
		return inventory;
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
		if (facing != EnumFacing.DOWN)
			return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
					|| super.hasCapability(capability, facing);
		else
			return super.hasCapability(capability, facing);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		if (facing != EnumFacing.DOWN && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return (T) inventory;
		} else {
			return super.getCapability(capability, facing);
		}
	}

	@Override
	public void onLoad() {
		super.onLoad();

		BlockPos pos = getPos();
		IBlockState state = world.getBlockState(pos);
		Block tblock = state.getBlock();
		if (tblock instanceof BlockUncraftingdropper) {		
			block=(BlockUncraftingdropper)  tblock;
			block.updateInventoryPosInFrontPosition(world, pos, state);
			ticksUpdate = UncraftHelper.getProcessingTicks(block.getTier());
			// System.out.println("onLoad: "+tier);
		}

	}

	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {

		this.inventoryPos = null;
		if (this.currentComponentsIfEmpty()) {

			if (inventory.isStackEmpty())
				return;
			EntityItem item = new EntityItem(worldIn, pos.getX(), pos.getY(), pos.getZ(), inventory.getStackCopy());
			worldIn.spawnEntity(item);
			inventory.setStackInSlot(0, ItemStack.EMPTY);
		}

		else {

			if (currentComponents != null && !currentComponents.isEmpty()) {
				// for each currentComponents must be spawned as entity in the world before
				// break the block
				for (ItemStack component : currentComponents) {

					int quantity = component.getCount();
					if (quantity == 0)
						continue;
					ItemStack itemStackCopy = component.copy();
					itemStackCopy.setCount(quantity);
					worldIn.spawnEntity(new EntityItem(worldIn, pos.getX(), pos.getY(), pos.getZ(), itemStackCopy));
				}
				currentComponents.clear();
				inventory.setStackInSlot(0, ItemStack.EMPTY);

			} else {
				// if the list is empty, set the item in the inventory to empty
				inventory.setStackInSlot(0, ItemStack.EMPTY);
			}

		}

	}

	public void update() {

		if (world == null) return;
		if (world.isRemote)  return;
		
		
		ticksElapsed++;
		if (!isTitleTick())
			return;

		if (getRedstonePowered())
			return;
		if (inventory.isStackEmpty())
			return;

		if (currentComponentsIfEmpty()) {// cant uncraft
			// inventory.logMessage("We are going to dispense the stack " + inventory.getStackName());
			ItemStack itemStackCopy = inventory.getStackCopy();
			if (dispense(this, itemStackCopy)) {
				this.playDispenseSound(this);
				// inventory.logMessage("Item dispensed");
				if (itemStackCopy.getCount() == 0)
					itemStackCopy = ItemStack.EMPTY; // Set the stack to empty
				inventory.setStackInSlot(0, itemStackCopy); // Set the stack to the new value
			}

		} else {

			ItemStack itemStack = currentComponents.get(0);
			if (itemStack == null || itemStack.isEmpty())
				return;
			int quantity = itemStack.getCount();
			if (quantity == 0)
				return;

			// inventory.logMessage("We are going to dispense an
			// item:"+itemStackCopy.getDisplayName());
			if (dispense(this, itemStack)) {
				this.playDispenseSound(this);
				// if the item is dispensed, remove one from the list
				// inventory.logMessage("Item dispensed:"+itemStackCopy.getDisplayName());
				if (itemStack.getCount() == 0) {
					currentComponents.remove(0);
				}

				if (currentComponents.isEmpty()) {
					inventory.empty(); // remove the item is already uncrafted
				}
			}
		}
	}

	public void sendUpdate() {
		world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
		markDirty();
	}

	/*
	 * protected void playDispenseSound(TileEntityUncraftingdropper source) {
	 * source.getWorld().playEvent(1000, source.getPos(), 0); }
	 */

	protected void playDispenseSound(TileEntityUncraftingdropper source) {
		// Use this:
		World world = source.getWorld();
		BlockPos pos = source.getPos();
		world.playSound(null, // Player - null for all players
				pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_DISPENSER_DISPENSE, // Or your custom sound
				SoundCategory.BLOCKS, (float) 0.2, // Volume (default is 1.0F, lower = quieter)
				0.8F // Pitch (default is 1.0F, lower = deeper sound)
		);
	}

	public final boolean dispense(TileEntityUncraftingdropper tileEntity, ItemStack stack) {

		if (tileEntity == null)
			return false;
		World tileWorld = tileEntity.getWorld(); // Get the world of the TileEntity
		IBlockState actualState = tileWorld.getBlockState(pos); // Get the actual state at the TileEntity's position

		boolean dispenseFullStack = (block != null && block.getTier() == 5);																		// from the block state

		if (inventoryPos == null) {
			EnumFacing enumfacing = actualState.getValue(BlockUncraftingdropper.FACING); // Get the facing direction
			dispenseStack(tileEntity, stack, enumfacing,dispenseFullStack);
			this.spawnDispenseParticles(tileEntity, enumfacing);
			return true;
		} else // insertInInventory
		{
			// Check if the block at posCheck has a tile entity with an inventory
			TileEntity inventoryToInsert = getWorld().getTileEntity(inventoryPos);
			if (inventoryToInsert instanceof IInventory) {
				// System.out.println("DBG: Insert in inventory");
                return (insertInInventory(stack, (IInventory) inventoryToInsert,dispenseFullStack));
			}
			return false;
		}

	}

	protected void dispenseStack(TileEntityUncraftingdropper source, ItemStack stack, EnumFacing enumfacing,boolean fullStack) {
		if (fullStack) {
			int maxStack = stack.getMaxStackSize();
			int toDispense = Math.min(stack.getCount(), maxStack);
						
			ItemStack itemstack = stack.splitStack(toDispense);
			doDispense(source.getWorld(), itemstack, 6, enumfacing);
		} else {
			ItemStack itemstack = stack.splitStack(1);
			doDispense(source.getWorld(), itemstack, 6, enumfacing);
		}
	}

	public void doDispense(World worldIn, ItemStack stack, int speed, EnumFacing facing) {

		// Calculate the middle point of the Y-axis
		double middleY = pos.getY() + 0.2;

		// Calculate the position based on the facing direction
		double d0 = pos.getX() + 0.5 + facing.getFrontOffsetX() * 0.8;
		double d1 = middleY; // + facing.getFrontOffsetY() * 0.5;
		double d2 = pos.getZ() + 0.5 + facing.getFrontOffsetZ() * 0.8;

		if (facing.getAxis() == EnumFacing.Axis.Y) {
			d1 = d1 - 0.125D;
		} else {
			d1 = d1 - 0.15625D;
		}

		EntityItem entityitem = new EntityItem(worldIn, d0, d1, d2, stack);
		double d3 = worldIn.rand.nextDouble() * 0.1D + 0.2D;
		entityitem.motionX = (double) facing.getFrontOffsetX() * d3;
		entityitem.motionY = 0.20000000298023224D;
		entityitem.motionZ = (double) facing.getFrontOffsetZ() * d3;
		entityitem.motionX += worldIn.rand.nextGaussian() * 0.007499999832361937D * (double) speed;
		entityitem.motionY += worldIn.rand.nextGaussian() * 0.007499999832361937D * (double) speed;
		entityitem.motionZ += worldIn.rand.nextGaussian() * 0.007499999832361937D * (double) speed;
		worldIn.spawnEntity(entityitem);
	}

	protected void spawnDispenseParticles(TileEntityUncraftingdropper source, EnumFacing facingIn) {
		source.getWorld().playEvent(2000, source.getPos(), this.getWorldEventDataFrom(facingIn));
	}

	private int getWorldEventDataFrom(EnumFacing facingIn) {
		return facingIn.getFrontOffsetX() + 1 + (facingIn.getFrontOffsetZ() + 1) * 3;
	}

	private static boolean compareItemStacks(ItemStack itemStackIn, ItemStack itemStack) {
		if (itemStackIn.getMetadata() != itemStack.getMetadata())
			return false;
		// Check if the NBT tags are equal
		return ItemStack.areItemStackTagsEqual(itemStackIn, itemStack);
	}

	private boolean insertInInventory(ItemStack itemStackIn, IInventory inventoryToInsert,boolean fullStack) {

		int initialCount = itemStackIn.getCount();
		if (itemStackIn.isEmpty())
			return false;

		// System.out.println("DBG: Try to insert into
		// inventoryToInsert:"+itemStackInMetadata);

		// First try to merge with existing stacks
		for (int i = 0; i < inventoryToInsert.getSizeInventory(); ++i) {
			ItemStack existingStack = inventoryToInsert.getStackInSlot(i);

			// Skip empty slots in first pass
			if (existingStack.isEmpty())
				continue;

			// Check if items match first (fail fast if they don't)
			if (!existingStack.getItem().equals(itemStackIn.getItem())
					|| !compareItemStacks(itemStackIn, existingStack)) {
				continue;
			}

			// Now check if we can add to this stack
			int quantity = existingStack.getCount();
			int maxQuantity = existingStack.getMaxStackSize();

			if (quantity < inventoryToInsert.getInventoryStackLimit() && quantity < maxQuantity) {
				
				int amountToAdd=1;
				if(fullStack) {
					amountToAdd = Math.min(itemStackIn.getCount(), maxQuantity - quantity);
				}
				
				// We can merge with this stack
				existingStack.grow(amountToAdd);
				
				itemStackIn.shrink(amountToAdd);
				if (itemStackIn.getCount() <= 0) {
					itemStackIn.setCount(0);
				}
				
				inventoryToInsert.setInventorySlotContents(i, existingStack);
				
				if (itemStackIn.getCount() == 0) return true; //all has been inserted
			}
		}

		for (int i = 0; i < inventoryToInsert.getSizeInventory(); ++i) {
			ItemStack itemStack;
			itemStack = inventoryToInsert.getStackInSlot(i);
			if (itemStack.isEmpty()) {
				// System.out.println("DBG: Slot empty:"+i);

				if (!inventoryToInsert.isItemValidForSlot(i, itemStackIn))
					continue;
				itemStack = itemStackIn.copy();
				int amountToAdd=1;
				
				if(fullStack) {
					amountToAdd = Math.min(itemStackIn.getCount(), itemStackIn.getMaxStackSize());
				}		
				
				itemStack.setCount(amountToAdd);
				inventoryToInsert.setInventorySlotContents(i, itemStack);
				itemStackIn.shrink(amountToAdd);
				if (itemStackIn.getCount() <= 0) {
					itemStackIn.setCount(0);
				}
				if (itemStackIn.getCount() == 0) return true; //all has been inserted
			}
		}

		return !(initialCount==itemStackIn.getCount());//items have been inserted
	}
}
