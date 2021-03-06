package fi.dy.masa.autoverse.gui.client;

import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Slot;
import fi.dy.masa.autoverse.gui.client.base.GuiAutoverseTile;
import fi.dy.masa.autoverse.gui.client.button.GuiButtonHoverText;
import fi.dy.masa.autoverse.inventory.container.base.ContainerAutoverse;
import fi.dy.masa.autoverse.inventory.container.base.SlotRange;
import fi.dy.masa.autoverse.tileentity.TileEntityBlockReaderNBT;

public class GuiBlockReaderNBT extends GuiAutoverseTile
{
    private final TileEntityBlockReaderNBT ter;

    public GuiBlockReaderNBT(ContainerAutoverse container, TileEntityBlockReaderNBT te)
    {
        super(container, 176, 213, "gui.container.block_reader_nbt", te);

        this.ter = te;
        //this.infoArea = new InfoArea(160, 5, 11, 11, "autoverse.gui.infoarea.block_reader_nbt");
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        String str = I18n.format("autoverse.container.block_reader_nbt");
        this.fontRenderer.drawString(str, this.xSize / 2 - this.fontRenderer.getStringWidth(str) / 2, 5, 0x404040);

        str = I18n.format("autoverse.gui.label.block_reader_nbt.length_num", this.ter.getMaxLength());
        this.fontRenderer.drawString(str, 41, 31, 0x404040);

        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, 119, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Draw the slot backgrounds for existing/enabled slots
        SlotRange range = this.container.getCustomInventorySlotRange();

        for (int i = range.first; i < range.lastExc; i++)
        {
            Slot slot = this.container.getSlot(i);

            if (slot != null)
            {
                this.drawTexturedModalRect(x + slot.xPos - 1, y + slot.yPos - 1, 7, 130, 18, 18);
            }
        }
    }

    @Override
    protected void createButtons()
    {
        this.addButton(new GuiButtonHoverText(0, this.guiLeft +  8, this.guiTop + 25, 14, 14, 24, 0,
                this.guiTextureWidgets, 14, 0, "autoverse.gui.label.block_reader.take_blocks"));

        this.addButton(new GuiButtonHoverText(1, this.guiLeft + 29, this.guiTop + 31, 8, 8, 0, 0,
                this.guiTextureWidgets, 8, 0, "autoverse.gui.label.block_reader.block_count"));

        this.setButtonMultipliers(8, 4);
    }
}
