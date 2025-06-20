package es.luiscuesta.uncraftingdropper.common.blocks;

import es.luiscuesta.uncraftingdropper.Uncraftingdropper;
import es.luiscuesta.uncraftingdropper.common.libs.LibMisc;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public abstract  class BlockTileEntity<T extends TileEntity> extends Block {
	
    private final String baseName;
    private final ResourceLocation resourceLocation;
    private boolean preserveTileEntity;
    
    
   

	//private static final Map<IRegistryDelegate<Block>, IStateMapper> customStateMappers = ReflectionHelper.getPrivateValue(ModelLoader.class, null, "customStateMappers");
	//private static final DefaultStateMapper fallbackMapper = new DefaultStateMapper();
    
    public ResourceLocation getResourceLocation() {
    	return resourceLocation;
    }
    
    
	@Nonnull
    public   String getUnlocalizedName() {
		return resourceLocation.getResourceDomain() + "." + resourceLocation.getResourcePath();
	}
	
	public   String getItemBlockName() {
		return resourceLocation.getResourceDomain() + ":" + resourceLocation.getResourcePath();
	}	
	
	
    public BlockTileEntity(String name, Material materialIn, final boolean preserveTileEntity) {

        this(name, materialIn);
        this.setTickRandomly(false);
        this.preserveTileEntity = preserveTileEntity;
    }
	
    public BlockTileEntity(String name, Material blockMaterialIn) {
  
        super(blockMaterialIn, blockMaterialIn.getMaterialMapColor());
        baseName = name;
        //setBlockName(this, name);
        setHardness(2);
        if (isInCreativeTab())
            setCreativeTab(Uncraftingdropper.getTab());
        resourceLocation= new ResourceLocation(LibMisc.MOD_ID, name);
    }


    
	@SideOnly(Side.CLIENT)
	public void registerModels(ModelRegistryEvent event) {
		Item item=Item.getItemFromBlock(this);
		if(item != Items.AIR)	{			
			ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(Objects.requireNonNull(this.getRegistryName()), "inventory"));
		}
	}
	
	
    protected boolean isInCreativeTab() {
        return true;
    }



    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        int i = 0;
        String name = "item." + LibMisc.RESOURCE_PREFIX + baseName + "." + i;
        while (I18n.hasKey(name)) {
            tooltip.add(I18n.format(name));
            i++;
            name = "item." + LibMisc.RESOURCE_PREFIX + baseName + "." + i;
        }

    }
	
	//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	


    @Override
    public boolean hasTileEntity(@Nonnull final IBlockState state) {
        return true;
    }
    
    public abstract Class<? extends TileEntity> getClassTileEntity();
    	
    public abstract void onBlockPlaced(World world,BlockPos pos,ItemStack itemStackUsed);

    @Override
    public abstract TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state);


    @SuppressWarnings("unchecked")
    @Nullable
    protected T getTileEntity(final IBlockAccess world, final BlockPos pos) {
        return (T) world.getTileEntity(pos);
    }

    @Override
    public boolean removedByPlayer(@Nonnull final IBlockState state, @Nonnull final World world, @Nonnull final BlockPos pos, @Nonnull final EntityPlayer player, final boolean willHarvest) {
        // If it harvests, delay deletion of the block until after getDrops
        if(preserveTileEntity && willHarvest && !player.capabilities.isCreativeMode)
            return true;
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void harvestBlock(@Nonnull final World world, @Nonnull final EntityPlayer player, @Nonnull final BlockPos pos, @Nonnull final IBlockState state, @Nullable final TileEntity te, @Nonnull final ItemStack stack) {
        super.harvestBlock(world, player, pos, state, te, stack);

        if (preserveTileEntity) {
            this.onBlockHarvested(world,pos,state,player);
            world.setBlockToAir(pos);
            world.removeTileEntity(pos);
        }
    }

    @SuppressWarnings("deprecation")
	@Override
    public boolean eventReceived(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, int id, int param) {
        super.eventReceived(state, worldIn, pos, id, param);
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(id, param);
    }


    public void updateTick(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random random) {
        super.updateTick(world, pos, state, random);
    }

    protected abstract void redstoneUpdate(World world, BlockPos pos, IBlockState state, boolean powered);
    
    protected abstract void onNeighborChange(World world, BlockPos pos, IBlockState state, Block blockIn, BlockPos fromPos);
    
    @SuppressWarnings("deprecation")
	@Override
    public void neighborChanged(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
    	super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        redstoneUpdate(worldIn, pos, state, isPowered(worldIn, pos));
        onNeighborChange(worldIn, pos, state, blockIn, fromPos);
    }

 
    
    protected boolean isPowered(World world,BlockPos pos) {
	    boolean powered = false;
		for(EnumFacing dir : EnumFacing.VALUES) {// EnumFacing.HORIZONTALS
			int redstoneSide = world.getRedstonePower(pos.offset(dir), dir);
			if(redstoneSide >= 14) {
				powered = true;				
				break;
			}
		}
		return powered;
	}

    @Override
    public void onBlockAdded(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);       
    }

    @Override
    public boolean canConnectRedstone(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side) {
        return false;
    }
}
