package es.luiscuesta.uncraftingdropper.common.blocks;

import es.luiscuesta.uncraftingdropper.Uncraftingdropper;
import es.luiscuesta.uncraftingdropper.common.libs.LibMisc;
import es.luiscuesta.uncraftingdropper.common.tileentity.TileEntityUncraftingdropper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Objects;


public abstract class BlockUncraftingdropper extends BlockTileEntity<TileEntityUncraftingdropper> {

    public static final PropertyBool WRK = PropertyBool.create("wrk");
    public static final PropertyBool POWER = PropertyBool.create("power");
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    
    public ResourceLocation resourceLocation;

    public  BlockUncraftingdropper(String Name, Material material) {
        super(Name, material, true);
        setHardness(3.0F);
        setResistance(8.0f);
        setDefaultState(this.getBlockState().getBaseState().withProperty(WRK, false).withProperty(POWER, false).withProperty(FACING, EnumFacing.NORTH));
        setTickRandomly(true);
       
        //this.setCreativeTab(uncraftingdropper.getTab());
		resourceLocation= new ResourceLocation(LibMisc.MOD_ID, Name);
		this.setUnlocalizedName( Name);
		Uncraftingdropper.modRegistry.addBlockForRegistry(this);
		Uncraftingdropper.modRegistry.addBlockItemForRegistry(this);
    }
    
  
    public  int  getTier() {
    	return 1;
    }
    
    public abstract Color getColour();
    
    @Nonnull
    @Override
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.SOLID;
    }
    


    @Override
    public boolean isTranslucent(@Nonnull IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(@Nonnull IBlockState state) {
        return true;
    }

    @Override
    public boolean isFullCube(@Nonnull IBlockState state) {
        return true;
    }
    
	
	@Override
	public boolean canProvidePower(@Nonnull IBlockState state) {
		return false;
	}
	
	

	@Override 
	public int getWeakPower(@Nonnull IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
				
		//if (blockState.getValue(POWER)) return 15;	
		return 0;
	
	}
	

	@Override
	public int getStrongPower(@Nonnull IBlockState blockState, @Nonnull IBlockAccess blockAccess, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
		return this.getWeakPower(blockState, blockAccess, pos, side);		
	}
	
    
	@Override
	public boolean hasComparatorInputOverride(@Nonnull IBlockState state) {
		return true;
	}
	
	

	
	@Override
	public int getComparatorInputOverride(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos){
		
		TileEntityUncraftingdropper uncraftingdropper = (TileEntityUncraftingdropper) world.getTileEntity(pos);		
		return Objects.requireNonNull(uncraftingdropper).comparatorSignal();

	}
	
	
	@Nonnull
    @Override
    public Block setCreativeTab(@Nonnull CreativeTabs tab)
    {
		super.setCreativeTab(tab);
		return this;
	}
        
    @Nonnull
    @Override
    public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
        return EnumBlockRenderType.MODEL;
    	//return EnumBlockRenderType.INVISIBLE;
    }
 

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, WRK,POWER, FACING);
    }

    public void setNewState(BlockPos pos, World worldIn, boolean working) {
    	
    	IBlockState actualState = worldIn.getBlockState(pos);    	
    	IBlockState newState = actualState.withProperty(POWER, isPowered(worldIn, pos)).withProperty(WRK, working);    	
    	worldIn.setBlockState(pos, newState, 2);

        if (actualState.getValue(POWER) == newState.getValue(POWER)) {
            actualState.getValue(WRK);
            newState.getValue(WRK);
        }

    }
    
   
    @Nonnull
    @Override
    public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public int getMetaFromState(@Nonnull IBlockState state) {
       int isWrk=state.getValue(WRK)?1:0;
       int hasPower=state.getValue(POWER)?1:0;       
       int facing=state.getValue(FACING).getHorizontalIndex();
       
       return (4*facing+2*hasPower+isWrk);
       
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        boolean wrk = (meta & 1) != 0;   
        boolean power = (meta & 2) != 0;
        int indexFacing= (meta >>2)&3 ;			
        return this.blockState.getBaseState().withProperty(WRK, wrk).withProperty(POWER, power).withProperty(FACING, EnumFacing.getHorizontal(indexFacing));
    }

    @Override
    public boolean canPlaceBlockAt(@Nonnull World worldIn, @Nonnull BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos);
        //&& worldIn.getBlockState(pos.down()).getBlock() == Blocks.HOPPER;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityUncraftingdropper();
    }
    
    
    public void updateInventoryPosInFrontPosition(World worldIn, BlockPos pos,IBlockState state) {
    	TileEntity tileEntityUncrafting = worldIn.getTileEntity(pos);
        if (!(tileEntityUncrafting instanceof TileEntityUncraftingdropper)) return;        
        TileEntityUncraftingdropper myTileEntityUncraftingdropper = (TileEntityUncraftingdropper) tileEntityUncrafting;

        BlockPos posCheck = pos.offset(state.getValue(FACING));        
        TileEntity tileEntityToCheck = worldIn.getTileEntity(posCheck);
        myTileEntityUncraftingdropper.inventoryPos = (tileEntityToCheck instanceof IInventory) ? posCheck : null;
    }
    
    public void updateInventoryPosInFrontPosition(World worldIn, BlockPos pos) {	
        IBlockState blockStateUncrafting = worldIn.getBlockState(pos);        
        updateInventoryPosInFrontPosition(worldIn, pos, blockStateUncrafting);

    }
    
	@Override
	protected void onNeighborChange(World world, BlockPos pos, IBlockState state, Block blockIn, BlockPos fromPos) {
		updateInventoryPosInFrontPosition(world, pos, state);
		
	}

    public void setPowerProperty(World world, BlockPos pos, boolean powered) {
    	//call changePower
    	IBlockState state = world.getBlockState(pos);
    	if(state.getValue(POWER) == powered) return;
    	IBlockState newState = state.withProperty(POWER, powered);
    	world.setBlockState(pos, newState, 2);		
    	//if (!world.isRemote) world.notifyBlockUpdate(pos, state, state, 2);
	}
    
    @Override
    protected  void redstoneUpdate(World world, BlockPos pos, IBlockState state, boolean powered) {
		setPowerProperty(world, pos, powered);
    }
	
    @Override
    public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
    	if (worldIn.isRemote) return true; //return false;
		if (hand == EnumHand.OFF_HAND) 	return true;
		
		TileEntity te = worldIn.getTileEntity(pos);
		if (!(te instanceof TileEntityUncraftingdropper)) 	return true;
			
		TileEntityUncraftingdropper uncraftingdropper = (TileEntityUncraftingdropper) te;
		ItemStack playerStack = playerIn.getHeldItem(hand);
		
		if (!uncraftingdropper.isStackEmpty()) return true;
		if (playerIn.getHeldItemMainhand().isEmpty()||!uncraftingdropper.isItemValidForSlot(0, playerStack)) return true;
		
		//System.out.println("-----Uncraftingdropper: Insert Stack");
		uncraftingdropper.insertItem(0, playerStack.copy(), false);
		playerStack.setCount(playerStack.getCount() - 1);
		if (playerStack.isEmpty()) {
			playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, ItemStack.EMPTY);
		}		
		return true;
    }
    
    

    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        TileEntityUncraftingdropper uncraftingdropper = (TileEntityUncraftingdropper) worldIn.getTileEntity(pos);
        if (uncraftingdropper == null) return;
        uncraftingdropper.breakBlock(worldIn, pos, state);
        super.breakBlock(worldIn, pos, state);
    }

	@Override
	public Class<? extends TileEntity> getClassTileEntity() {
		return TileEntityUncraftingdropper.class;
	}




    
	@Override
	public void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
		/*
		if (placer instanceof EntityPlayer){
			placerUUID=EntityPlayer.getUUID(((EntityPlayer)placer).getGameProfile());
		*/			
		world.setBlockState(pos, state.withProperty(WRK, false).withProperty(POWER, isPowered(world, pos)).withProperty(FACING,  placer.getHorizontalFacing().getOpposite()));
		
	}
	
	@Override
	public void onBlockPlaced(World world, BlockPos pos, ItemStack itemStackUsed) {		
		updateInventoryPosInFrontPosition(world, pos);			
	}




    
}

