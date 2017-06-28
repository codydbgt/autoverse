package fi.dy.masa.autoverse.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import fi.dy.masa.autoverse.inventory.container.base.ContainerTile;
import fi.dy.masa.autoverse.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.autoverse.tileentity.TileEntitySplitter;

public class ContainerSplitterRedstone extends ContainerTile
{
    private final TileEntitySplitter tesp;
    public boolean secondaryOutput;

    public ContainerSplitterRedstone(EntityPlayer player, TileEntitySplitter te)
    {
        super(player, te);

        this.tesp = te;
        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 64);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        // Add the input slot as a merge slot range, but no other slots
        this.addMergeSlotRangePlayerToExt(this.inventorySlots.size(), 1, false);

        // Input slot
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 0, 8, 30));

        // Add the output buffer 1 slot
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.tesp.getInventoryOut1(), 0, 62, 30));

        // Add the output buffer 2 slot
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.tesp.getInventoryOut2(), 0, 116, 30));
    }

    @Override
    public void detectAndSendChanges()
    {
        if (this.isClient == false)
        {
            boolean isSecondary = this.tesp.outputIsSecondaryCached();

            for (int i = 0; i < this.listeners.size(); i++)
            {
                if (this.secondaryOutput != isSecondary)
                {
                    this.listeners.get(i).sendWindowProperty(this, 0, isSecondary ? 0x1 : 0x0);
                }
            }

            this.secondaryOutput = isSecondary;
        }

        super.detectAndSendChanges();
    }

    @Override
    public void updateProgressBar(int id, int data)
    {
        switch (id)
        {
            case 0:
                this.secondaryOutput = data != 0;
                break;
        }

        super.updateProgressBar(id, data);
    }
}
