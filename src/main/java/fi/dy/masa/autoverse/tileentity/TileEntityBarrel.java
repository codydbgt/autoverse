package fi.dy.masa.autoverse.tileentity;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.autoverse.gui.client.GuiAutoverse;
import fi.dy.masa.autoverse.gui.client.GuiBarrel;
import fi.dy.masa.autoverse.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.autoverse.inventory.container.ContainerAutoverse;
import fi.dy.masa.autoverse.inventory.container.ContainerBarrel;
import fi.dy.masa.autoverse.reference.ReferenceNames;
import fi.dy.masa.autoverse.util.EntityUtils;
import fi.dy.masa.autoverse.util.InventoryUtils;

public class TileEntityBarrel extends TileEntityAutoverseInventory
{
    protected boolean isPulsed;
    protected int tier;
    protected final Map<UUID, Long> clickTimes;
    private BlockPos posBottom;

    public TileEntityBarrel()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_BARREL);

        this.clickTimes = new HashMap<UUID, Long>();
        this.initInventories();
    }

    @Override
    protected void initInventories()
    {
        this.itemHandlerBase = new ItemStackHandlerTileEntity(0, 1, this.getMaxStackSize(), true, "Items", this);
        this.itemHandlerExternal = new ItemHandlerWrapperExternal(this.itemHandlerBase, this);
    }

    public void setTier(int tier)
    {
        this.tier = MathHelper.clamp(tier, 0, 15);
        this.initInventories();
    }

    public int getTier()
    {
        return this.tier;
    }

    public void setIsPulsed(boolean isPulsed)
    {
        this.isPulsed = isPulsed;
    }

    public boolean isPulsed()
    {
        return this.isPulsed;
    }

    public int getMaxStackSize()
    {
        return (int)Math.pow(2, this.tier);
    }

    @Override
    public void onLoad()
    {
        this.posBottom = this.getPos().down();
    }

    @Override
    protected void onRedstoneChange(boolean state)
    {
        if (this.isPulsed == true && state == true)
        {
            this.scheduleBlockTick(1, true);
        }
    }

    public boolean fillBarrel()
    {
        ItemStack stack = this.getBaseItemHandler().extractItem(0, 1, true);

        if (stack != null)
        {
            stack.stackSize = this.getMaxStackSize();
            this.getBaseItemHandler().setStackInSlot(0, stack);
            return true;
        }

        return false;
    }

    @Override
    public void onBlockTick(IBlockState state, Random rand)
    {
        super.onBlockTick(state, rand);

        if (this.isPulsed)
        {
            this.pushItemsToAdjacentInventory(this.itemHandlerBase, 0, this.posBottom, EnumFacing.UP, true);
        }
    }

    @Override
    protected Vec3d getSpawnedItemPosition()
    {
        return this.getSpawnedItemPosition(EnumFacing.DOWN);
    }

    @Override
    public void onLeftClickBlock(EntityPlayer player)
    {
        if (this.getWorld().isRemote == true)
        {
            return;
        }

        long time = this.getWorld().getTotalWorldTime();
        Long last = this.clickTimes.get(player.getUniqueID());

        //System.out.printf("plop start - time: %d diff: %d\n", time, time - last);

        // Double left clicked fast enough (< 5 ticks) - do the item moving action
        if (last != null && (time - last) < 5)
        {
            this.moveItems(player, true);
            this.clickTimes.remove(player.getUniqueID());
        }
        else
        {
            this.moveItems(player, false);
            this.clickTimes.put(player.getUniqueID(), this.getWorld().getTotalWorldTime());
        }
    }

    protected void moveItems(EntityPlayer player, boolean isDoubleClick)
    {
        IItemHandler playerInv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        IItemHandler barrelInv = this.getBaseItemHandler();

        // Sneaking: put items into the Barrel
        if (player.isSneaking() == true)
        {
            ItemStack stack = player.getHeldItemMainhand();
            if (stack != null)
            {
                player.setHeldItem(EnumHand.MAIN_HAND, InventoryUtils.tryInsertItemStackToInventory(barrelInv, stack));
            }

            if (isDoubleClick == true && barrelInv.getStackInSlot(0) != null)
            {
                InventoryUtils.tryMoveMatchingItems(playerInv, barrelInv);
                player.getEntityWorld().playSound(null, this.getPos(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.BLOCKS, 0.2f, 1.8f);
            }
        }
        // Not sneaking, take items out of the Barrel
        else
        {
            ItemStack stack = this.getBaseItemHandler().extractItem(0, 64, false);
            if (stack != null)
            {
                RayTraceResult rayTrace = EntityUtils.getRayTraceFromPlayer(this.getWorld(), player, false);
                if (rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK && this.getPos().equals(rayTrace.getBlockPos()))
                {
                    Vec3d pos = this.getSpawnedItemPosition(rayTrace.sideHit);
                    EntityUtils.dropItemStacksInWorld(this.getWorld(), pos, stack, -1, true, false);
                }
            }
        }
    }

    @Override
    protected NBTTagCompound getBlockEntityTag()
    {
        NBTTagCompound nbt = new NBTTagCompound();

        // Only include the inventory contents in the NBT
        if (this.getBaseItemHandler().getStackInSlot(0) != null)
        {
            nbt.merge(this.getBaseItemHandler().serializeNBT());
        }

        return nbt;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setBoolean("Pulsed", this.isPulsed);
        nbt.setByte("Tier", (byte)this.tier);

        return nbt;
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        this.isPulsed = nbt.getBoolean("Pulsed");
        this.setTier(nbt.getByte("Tier"));

        super.readFromNBTCustom(nbt);
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound tag)
    {
        tag = super.getUpdatePacketTag(tag);
        tag.setByte("d", (byte)(this.tier | (this.isPulsed ? 0x10 : 0)));
        return tag;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        super.handleUpdateTag(tag);

        byte data = tag.getByte("d");
        this.setTier(data & 0xF);
        this.setIsPulsed(data >= 16);
    }

    private class ItemHandlerWrapperExternal implements IItemHandler
    {
        private final TileEntityBarrel te;
        private final IItemHandler parent;

        public ItemHandlerWrapperExternal(IItemHandler parent, TileEntityBarrel te)
        {
            this.te = te;
            this.parent = parent;
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
            if (this.te.getWorld().isBlockPowered(TileEntityBarrel.this.getPos()))
            {
                return stack;
            }

            return this.parent.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            if (this.te.getWorld().isBlockPowered(TileEntityBarrel.this.getPos()))
            {
                return null;
            }

            return this.parent.extractItem(slot, amount, simulate);
        }
    }

    @Override
    public ContainerAutoverse getContainer(EntityPlayer player)
    {
        return new ContainerBarrel(player, this);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public GuiAutoverse getGui(EntityPlayer player)
    {
        return new GuiBarrel(this.getContainer(player), this);
    }
}
