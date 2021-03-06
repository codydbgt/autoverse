package fi.dy.masa.autoverse.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import fi.dy.masa.autoverse.inventory.container.base.ContainerTile;
import fi.dy.masa.autoverse.inventory.container.base.MergeSlotRange;
import fi.dy.masa.autoverse.tileentity.TileEntityBlockBreaker;

public class ContainerBlockBreaker extends ContainerTile
{
    public ContainerBlockBreaker(EntityPlayer player, TileEntityBlockBreaker te)
    {
        super(player, te);

        this.reAddSlots(8, 84);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        this.customInventorySlots = new MergeSlotRange(this.inventorySlots.size(), 9);

        SlotPlacer.create(62, 17, this.te.getBaseItemHandler(), this).setMaxSlotsPerRow(3).place();
    }
}
