package fi.dy.masa.autoverse.inventory.wrapper;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.autoverse.inventory.IItemHandlerSize;

public class ItemHandlerWrapperSize implements IItemHandler, IItemHandlerSize
{
    protected final IItemHandler baseHandler;

    public ItemHandlerWrapperSize(IItemHandler baseHandler)
    {
        this.baseHandler = baseHandler;
    }

    @Override
    public int getSlots()
    {
        return this.baseHandler.getSlots();
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return this.baseHandler.getSlotLimit(slot);
    }

    @Override
    public ItemStack getStackInSlot(int slot)
    {
        return this.baseHandler.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        return this.baseHandler.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        return this.baseHandler.extractItem(slot, amount, simulate);
    }

    @Override
    public int getInventoryStackLimit()
    {
        if (this.baseHandler instanceof IItemHandlerSize)
        {
            return ((IItemHandlerSize) this.baseHandler).getInventoryStackLimit();
        }

        return 64;
    }

    @Override
    public int getItemStackLimit(int slot, ItemStack stack)
    {
        if (this.baseHandler instanceof IItemHandlerSize)
        {
            return ((IItemHandlerSize) this.baseHandler).getItemStackLimit(slot, stack);
        }

        return this.getInventoryStackLimit();
    }
}
