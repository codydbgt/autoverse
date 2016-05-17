package fi.dy.masa.autoverse.tileentity;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import fi.dy.masa.autoverse.gui.client.GuiAutoverse;
import fi.dy.masa.autoverse.gui.client.GuiFilter;
import fi.dy.masa.autoverse.inventory.ItemHandlerWrapperFilter;
import fi.dy.masa.autoverse.inventory.ItemHandlerWrapperSelective;
import fi.dy.masa.autoverse.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.autoverse.inventory.container.ContainerFilter;
import fi.dy.masa.autoverse.reference.ReferenceNames;
import fi.dy.masa.autoverse.util.EntityUtils;
import fi.dy.masa.autoverse.util.InventoryUtils;

public class TileEntityFilter extends TileEntityAutoverseInventory
{
    protected ItemStackHandlerTileEntity inventoryReset;
    protected ItemStackHandlerTileEntity inventoryFilterItems;

    protected ItemStackHandlerTileEntity inventoryFilterered;
    protected ItemStackHandlerTileEntity inventoryOtherOut;
    protected IItemHandler wrappedInventoryFilterered;
    protected IItemHandler wrappedInventoryOtherOut;
    protected ItemHandlerWrapperFilter inventoryInput;

    protected EnumFacing facingFilteredOut;
    protected EnumFacing facingFilteredOutOpposite;
    protected BlockPos posFilteredOut;
    protected int filterTier;

    public TileEntityFilter()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_FILTER);
    }

    protected void initInventories()
    {
        this.inventoryReset         = new ItemStackHandlerTileEntity(0, this.getNumResetSlots(),   1, false, "ResetItems", this);
        this.inventoryFilterItems   = new ItemStackHandlerTileEntity(1, this.getNumFilterSlots(),  1, false, "FilterItems", this);
        this.inventoryFilterered    = new ItemStackHandlerTileEntity(2,                       31, 64, false, "FilteredItems", this);
        this.inventoryOtherOut      = new ItemStackHandlerTileEntity(3,                       31, 64, false, "OutputItems", this);
        this.itemHandlerBase        = this.inventoryOtherOut;

        // 31 slots = 9 buffer slots + max 22 reset + filter-item slots when doing a reset cycle

        this.wrappedInventoryFilterered    = new ItemHandlerWrapperOutputBuffer(this.inventoryFilterered);
        this.wrappedInventoryOtherOut      = new ItemHandlerWrapperOutputBuffer(this.inventoryOtherOut);
        this.inventoryInput                = new ItemHandlerWrapperFilter(this.inventoryReset, this.inventoryFilterItems,
                                                this.inventoryFilterered, this.inventoryOtherOut, this);
    }

    @Override
    public IItemHandler getWrappedInventoryForContainer()
    {
        return this.getBaseItemHandler();
    }

    public IItemHandler getInputInventory()
    {
        return new ItemHandlerWrapperInputModifiable(this.inventoryInput);
    }

    public IItemHandler getResetInventory()
    {
        return this.inventoryReset;
    }

    public IItemHandler getResetSequenceBuffer()
    {
        return this.inventoryInput.getSequenceBuffer();
    }

    public IItemHandler getFilterItemsInventory()
    {
        return this.inventoryFilterItems;
    }

    public IItemHandler getFilteredItemsInventory()
    {
        return this.inventoryFilterered;
    }

    public IItemHandler getOutputInventory()
    {
        return this.inventoryOtherOut;
    }

    private int getNumResetSlots()
    {
        return 2 + this.getFilterTier();
    }

    private int getNumFilterSlots()
    {
        int tier = this.getFilterTier();
        if (tier == 2)
        {
            return 18;
        }

        return tier == 1 ? 9 : 1;
    }

    public int getFilterTier()
    {
        return this.filterTier;
    }

    public void setFilterTier(int tier)
    {
        this.filterTier = MathHelper.clamp_int(tier, 0, 2);

        this.initInventories();
    }

    @Override
    public void setFacing(EnumFacing facing)
    {
        super.setFacing(facing);

        if (facing.getAxis().isHorizontal() == true)
        {
            this.facingFilteredOut = facing.rotateYCCW();
        }
        else
        {
            // FIXME add 24-way rotation?
            this.facingFilteredOut = EnumFacing.WEST;
        }

        this.facingFilteredOutOpposite = this.facingFilteredOut.getOpposite();
        this.posFilteredOut = this.getPos().offset(this.facingFilteredOut);
    }

    @Override
    protected void onRedstoneChange(boolean state)
    {
        if (state == true)
        {
            this.scheduleBlockTick(1, true);
        }
    }

    @Override
    public void onBlockTick(IBlockState state, Random rand)
    {
        super.onBlockTick(state, rand);

        int slot1 = InventoryUtils.getFirstNonEmptySlot(this.wrappedInventoryOtherOut);
        if (slot1 != -1)
        {
            for (int slot = slot1; slot < this.wrappedInventoryFilterered.getSlots(); slot++)
            {
                if (this.pushItemsToAdjacentInventory(this.wrappedInventoryOtherOut, slot,
                        this.posFront, this.facingOpposite, this.redstoneState) == true)
                {
                    break;
                }
            }
        }

        int slot2 = InventoryUtils.getFirstNonEmptySlot(this.wrappedInventoryFilterered);
        if (slot2 != -1)
        {
            //System.out.printf("block tick - pos: %s\n", this.getPos());
            for (int slot = slot2; slot < this.wrappedInventoryFilterered.getSlots(); slot++)
            {
                if (this.pushItemsToAdjacentInventory(this.wrappedInventoryFilterered, slot,
                        this.posFilteredOut, this.facingFilteredOutOpposite, this.redstoneState) == true)
                {
                    break;
                }
            }
        }

        // Lazy check for if there WERE some items, then schedule a new tick
        if (slot1 != -1 || slot2 != -1)
        {
            this.scheduleBlockTick(4, false);
        }
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound tag)
    {
        super.readFromNBTCustom(tag);

        // Setting the tier and thus initializing the inventories needs to
        // happen before reading the inventories!
        this.setFilterTier(tag.getByte("Tier"));

        this.inventoryReset.deserializeNBT(tag);
        this.inventoryFilterItems.deserializeNBT(tag);
        this.inventoryFilterered.deserializeNBT(tag);
        this.inventoryOtherOut.deserializeNBT(tag);
        this.inventoryInput.deserializeNBT(tag);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);

        tag.setByte("Tier", (byte)this.getFilterTier());
        tag.merge(this.inventoryInput.serializeNBT());
    }

    @Override
    protected void readItemsFromNBT(NBTTagCompound nbt)
    {
    }

    @Override
    public void writeItemsToNBT(NBTTagCompound nbt)
    {
        super.writeItemsToNBT(nbt);

        nbt.merge(this.inventoryReset.serializeNBT());
        nbt.merge(this.inventoryFilterItems.serializeNBT());
        nbt.merge(this.inventoryFilterered.serializeNBT());
        nbt.merge(this.inventoryOtherOut.serializeNBT());
    }

    @Override
    public NBTTagCompound getDescriptionPacketTag(NBTTagCompound nbt)
    {
        nbt = super.getDescriptionPacketTag(nbt);
        nbt.setByte("t", (byte)this.getFilterTier());
        nbt.setByte("m", (byte)this.inventoryInput.getMode().getId());
        return nbt;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
    {
        NBTTagCompound nbt = packet.getNbtCompound();

        this.setFilterTier(nbt.getByte("t"));
        this.inventoryInput.setMode(ItemHandlerWrapperFilter.EnumMode.fromId(nbt.getByte("m")));

        super.onDataPacket(net, packet);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return true;
        }

        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            if (facing == this.facing)
            {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.wrappedInventoryOtherOut);
            }

            if (facing == this.facingFilteredOut)
            {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.wrappedInventoryFilterered);
            }

            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.inventoryInput);
        }

        return super.getCapability(capability, facing);
    }

    public void dropInventories()
    {
        EntityUtils.dropAllItemInWorld(this.getWorld(), this.getPos(), this.inventoryReset, true, true);
        EntityUtils.dropAllItemInWorld(this.getWorld(), this.getPos(), this.inventoryFilterItems, true, true);
        EntityUtils.dropAllItemInWorld(this.getWorld(), this.getPos(), this.inventoryFilterered, true, true);
        EntityUtils.dropAllItemInWorld(this.getWorld(), this.getPos(), this.inventoryOtherOut, true, true);
    }

    private class ItemHandlerWrapperInputModifiable implements IItemHandlerModifiable
    {
        private final IItemHandler parent;

        public ItemHandlerWrapperInputModifiable(IItemHandler parent)
        {
            this.parent = parent;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack)
        {
            // This is a dummy method to satisfy the Container#putStack()
            // Nothing will ever be stored in the virtual input slot itself anyway
        }

        @Override
        public int getSlots()
        {
            return this.parent.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot)
        {
            return this.parent.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            return this.parent.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            return this.parent.extractItem(slot, amount, simulate);
        }
    }

    private class ItemHandlerWrapperOutputBuffer extends ItemHandlerWrapperSelective
    {
        public ItemHandlerWrapperOutputBuffer(IItemHandler baseInventory)
        {
            super(baseInventory);
        }

        @Override
        public int getSlots()
        {
            return super.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot)
        {
            // TODO: cache the index?
            //slot = InventoryUtils.getFirstNonEmptySlot(this.baseHandler);
            return super.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            //System.out.printf("inserting to slot %d (offset: %d) to: %s\n", slot, TileEntityBufferFifo.this.getOffsetSlot(slot), stack);
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            // TODO: cache the index?
            //slot = InventoryUtils.getFirstNonEmptySlot(this.baseHandler);
            return super.extractItem(slot, amount, simulate);
        }
    }

    @Override
    public ContainerFilter getContainer(EntityPlayer player)
    {
        return new ContainerFilter(player, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiAutoverse getGui(EntityPlayer player)
    {
        return new GuiFilter(this.getContainer(player), this);
    }
}
