package fi.dy.masa.autoverse.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.autoverse.inventory.ItemStackHandlerBasic;
import fi.dy.masa.autoverse.inventory.container.base.ContainerTile;
import fi.dy.masa.autoverse.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.autoverse.inventory.wrapper.machines.ItemHandlerWrapperFilter;
import fi.dy.masa.autoverse.tileentity.TileEntityFilter;

public class ContainerFilter extends ContainerTile
{
    private final TileEntityFilter tefi;

    public ContainerFilter(EntityPlayer player, TileEntityFilter te)
    {
        super(player, te);

        this.tefi = te;
        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 174);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        // Add the input slot as a merge slot range, but no other slots
        this.addMergeSlotRangePlayerToExt(this.inventorySlots.size(), 1, false);

        if (this.isClient)
        {
            this.addSlotToContainer(new SlotItemHandlerGeneric(this.tefi.getInventoryInput(), 0, 8, 16));
        }
        else
        {
            this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 0, 8, 16));
        }

        int posX = 98;
        int posY = 16;
        ItemHandlerWrapperFilter filter = this.tefi.getInventoryFilter();
        IItemHandler inv = filter.getResetSequence().getSequenceInventory(false);

        // Add the Reset Sequence slots
        for (int slot = 0; slot < inv.getSlots(); slot++)
        {
            this.addSpecialSlot(new SlotItemHandlerGeneric(inv, slot, posX + slot * 18, posY));
        }

        posY = 34;
        // Use a basic inventory to hold the items on the client side
        inv = this.isClient ? new ItemStackHandlerBasic(inv.getSlots()) : filter.getResetSequence().getSequenceInventory(true);

        // Add the Reset Sequence matched slots
        for (int slot = 0; slot < inv.getSlots(); slot++)
        {
            this.addSpecialSlot(new SlotItemHandlerGeneric(inv, slot, posX + slot * 18, posY));
        }

        posX = 8;
        posY = 56;
        inv = filter.getFilterSequenceInventory();

        // Add the Filter slots
        for (int slot = 0, col = 0, row = 0; slot < inv.getSlots(); slot++)
        {
            this.addSpecialSlot(new SlotItemHandlerGeneric(inv, slot, posX + col * 18, posY + row * 18));

            if (++col >= 9)
            {
                col = 0;
                row++;
            }
        }

        posY = 103;
        inv = this.tefi.getInventoryOutFiltered();

        // Add the filter buffer slots
        for (int slot = 0; slot < inv.getSlots(); slot++)
        {
            this.addSlotToContainer(new SlotItemHandlerGeneric(inv, slot, posX + (slot % 9) * 18, posY));

            if (slot == 8)
            {
                posY += 18;
            }
        }

        posY = 151;

        // Add the output buffer slot
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.tefi.getInventoryOutNormal(), 0, posX, posY));
    }
}
