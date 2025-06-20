package es.luiscuesta.uncraftingdropper.common.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TileEntityBase extends TileEntity {

    @Override // do persistent
    public boolean shouldRefresh(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState) {
        return oldState.getBlock() != newState.getBlock(); // Only refresh if the block type changes
    }

    public void sendUpdates() {
        world.markBlockRangeForRenderUpdate(pos, pos);
        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        markDirty();
    }

    public TileEntityBase() {
    	super();
    }
  
    //-------------------------------------------------------------
    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        writeExtraNBT(compound);
        return compound;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        readExtraNBT(compound);
    }

    public abstract void writeExtraNBT(NBTTagCompound nbttagcompound) ;
    public abstract void readExtraNBT(NBTTagCompound nbttagcompound) ;
    //-------------------------------------------------------------
    
    
    //send
    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 5, this.getUpdateTag());
    }

    //server send state to client
    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound cmp = super.getUpdateTag();
        writeExtraNBT(cmp);
        return cmp;
    }

    // client receive state from server
    @Override
    public void handleUpdateTag(@Nonnull NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        readExtraNBT(tag);
    }

    //client  handleUpdateTag and update changes
    @Override
    public void onDataPacket(@Nonnull NetworkManager net, @Nonnull SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        handleUpdateTag(pkt.getNbtCompound());
        sendUpdates();
    }


    public boolean canRedstoneConnect() {
        return false;
    }
}
