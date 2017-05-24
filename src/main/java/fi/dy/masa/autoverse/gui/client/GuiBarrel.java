package fi.dy.masa.autoverse.gui.client;

import net.minecraft.client.resources.I18n;
import fi.dy.masa.autoverse.inventory.container.base.ContainerAutoverse;
import fi.dy.masa.autoverse.tileentity.TileEntityBarrel;

public class GuiBarrel extends GuiAutoverse
{
    protected final TileEntityBarrel te;
    protected final int max;

    public GuiBarrel(ContainerAutoverse container, TileEntityBarrel te)
    {
        super(container, 176, 134, "gui.container.barrel");

        this.te = te;
        this.max = te.getMaxStackSize();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        String s = this.te.hasCustomName() ? this.te.getName() : I18n.format(this.te.getName());
        this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 5, 0x404040);
        s = I18n.format("autoverse.gui.label.max") + this.max;
        this.fontRenderer.drawString(s, 112, 32, 0x404040);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, 41, 0x404040);
    }
}
