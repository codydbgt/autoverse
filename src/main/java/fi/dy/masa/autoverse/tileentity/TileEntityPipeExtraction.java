package fi.dy.masa.autoverse.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.autoverse.reference.ReferenceNames;
import fi.dy.masa.autoverse.util.InventoryUtils;
import fi.dy.masa.autoverse.util.InventoryUtils.InvResult;

public class TileEntityPipeExtraction extends TileEntityPipe
{
    private int disabledExtractionSides;

    public TileEntityPipeExtraction()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_PIPE_EXTRACTION);
    }

    @Override
    protected boolean canInputOnSide(EnumFacing side)
    {
        return super.canInputOnSide(side) &&
               (this.getWorld().getTileEntity(this.getPos().offset(side)) instanceof TileEntityPipe) == false;
    }

    @Override
    public boolean onRightClickBlock(World world, BlockPos pos, IBlockState state, EnumFacing side,
            EntityPlayer player, EnumHand hand, float hitX, float hitY, float hitZ)
    {
        if (player.isSneaking() &&
            player.getHeldItemMainhand().isEmpty() &&
            player.getHeldItemOffhand().isEmpty() == false)
        {
            if (world.isRemote == false)
            {
                this.toggleExtractionOnSide(side);
            }

            return true;
        }

        return super.onRightClickBlock(world, pos, state, side, player, hand, hitX, hitY, hitZ);
    }

    private void toggleExtractionOnSide(EnumFacing side)
    {
        this.disabledExtractionSides ^= (1 << side.getIndex());
        this.markDirty();
        this.notifyBlockUpdate(this.getPos());
    }

    @Override
    protected boolean reScheduleStuckItems()
    {
        boolean addedDelays = false;

        for (int slot = 0; slot < 6; slot++)
        {
            // Always schedule an update on neighbor tile change, since we may want to push or pull items
            if (this.delays[slot] <= 0)
            {
                this.delays[slot] = this.delay;
                addedDelays = true;
            }
        }

        return addedDelays;
    }

    protected int tryMoveItemsForSide(World world, BlockPos pos, int slot)
    {
        // Only operate without a redstone signal
        if (this.redstoneState == false)
        {
            if (this.itemHandlerBase.getStackInSlot(slot).isEmpty())
            {
                if (this.sideInventories[slot] != null)
                {
                    System.out.printf("EXTRACTION tryMoveItemsForSide(): pos: %s, slot: %d - trying to pull\n", pos, slot);
                    return this.tryPullInItemsFromSide(world, pos, slot);
                }
                else
                {
                    System.out.printf("EXTRACTION tryMoveItemsForSide(): pos: %s, slot: %d - NOT VALID INV\n", pos, slot);
                }
            }
            else
            {
                System.out.printf("EXTRACTION tryMoveItemsForSide(): pos: %s, slot: %d - SUPER\n", pos, slot);
                return super.tryMoveItemsForSide(world, pos, slot);
            }
        }

        return -1;
    }

    private int tryPullInItemsFromSide(World world, BlockPos posSelf, int slot)
    {
        EnumFacing side = EnumFacing.getFront(slot);
        TileEntity te = world.getTileEntity(posSelf.offset(side));

        if (te != null &&
            (te instanceof TileEntityPipe) == false &&
            te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite()))
        {
            IItemHandler inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite());

            if (inv != null)
            {
                System.out.printf("EXTRACTION tryPullInItemsFromSide(): pos: %s, slot: %d trying to pull...\n", posSelf, slot, side);
                this.disableUpdateScheduling = true;
                boolean movedSome = InventoryUtils.tryMoveAllItems(inv, this.sideInventories[slot]) != InvResult.MOVED_NOTHING;
                this.disableUpdateScheduling = false;

                if (movedSome)
                {
                    System.out.printf("EXTRACTION tryPullInItemsFromSide(): pos: %s, slot: %d PULLED\n", posSelf, slot, side);
                    return this.delay;
                }
            }
        }

        System.out.printf("EXTRACTION tryPullInItemsFromSide(): pos: %s, slot: %d - FAILED PULL\n", posSelf, slot);
        return -1;
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.disabledExtractionSides = nbt.getByte("DisabledExtSides");
    }

    @Override
    protected NBTTagCompound writeToNBTCustom(NBTTagCompound nbt)
    {
        nbt = super.writeToNBTCustom(nbt);

        nbt.setByte("DisabledExtSides", (byte) this.disabledExtractionSides);

        return nbt;
    }
}