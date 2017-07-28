package fi.dy.masa.autoverse.gui.client;

import java.io.IOException;
import org.lwjgl.input.Keyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.autoverse.item.base.AutoverseItems;
import fi.dy.masa.autoverse.item.base.IStringInput;
import fi.dy.masa.autoverse.network.PacketHandler;
import fi.dy.masa.autoverse.network.message.MessageSendString;
import fi.dy.masa.autoverse.reference.ReferenceTextures;
import fi.dy.masa.autoverse.util.EntityUtils;

public class GuiScreenItemNameField extends GuiScreen
{
    protected final Minecraft mc;
    protected final ResourceLocation guiTexture;
    protected GuiTextField nameField;
    protected int guiLeft;
    protected int guiTop;
    protected int xSize;
    protected int ySize;

    public GuiScreenItemNameField()
    {
        this.mc = Minecraft.getMinecraft();
        this.guiTexture = ReferenceTextures.getGuiTexture("gui.name_field");
        this.xSize = 192;
        this.ySize = 82;
    }

    @Override
    public void initGui()
    {
        super.initGui();

        Keyboard.enableRepeatEvents(true);

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        this.nameField = new GuiTextField(0, this.fontRenderer, this.guiLeft + 10, this.guiTop + 25, 173, 12);
        this.nameField.setTextColor(-1);
        this.nameField.setDisabledTextColour(-1);
        this.nameField.setEnableBackgroundDrawing(false);
        this.nameField.setMaxStringLength(60);
        this.nameField.setEnabled(true);
        this.nameField.setText(this.getNameFromItem());
        this.nameField.setFocused(true);
        this.nameField.setCursorPositionEnd();

        this.buttonList.clear();
        this.buttonList.add(new GuiButton(1, this.guiLeft + 8, this.guiTop + 40, 80, 20, I18n.format("autoverse.gui.label.setname")));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawGuiBackground(partialTicks, mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);

        String s = I18n.format("autoverse.gui.label.current_name");
        int textWidth = this.fontRenderer.getStringWidth(s);
        int x = (this.width / 2);
        this.fontRenderer.drawString(s, x - (textWidth / 2), this.guiTop + 6, 0x404040);

        GlStateManager.disableLighting();
        GlStateManager.disableBlend();

        this.nameField.drawTextBox();
    }

    protected void drawGuiBackground(float gameTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        this.bindTexture(this.guiTexture);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();

        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == Keyboard.KEY_RETURN)
        {
            this.actionPerformed(this.buttonList.get(0));
        }
        else if (this.nameField.textboxKeyTyped(typedChar, keyCode) == false)
        {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        // Clear the field on right click
        if (mouseButton == 1)
        {
            if (mouseX >= this.nameField.x && mouseX < this.nameField.x + this.nameField.width &&
                mouseY >= this.nameField.y && mouseY < this.nameField.y + this.nameField.height)
            {
                this.nameField.setText("");
            }
        }
        else
        {
            super.mouseClicked(mouseX, mouseY, mouseButton);

            this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button.id == 1)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageSendString(MessageSendString.Type.ITEM, this.nameField.getText()));
            this.mc.displayGuiScreen(null);
        }
    }

    protected void bindTexture(ResourceLocation rl)
    {
        this.mc.renderEngine.bindTexture(rl);
    }

    private String getNameFromItem()
    {
        ItemStack stack = EntityUtils.getHeldItemOfType(this.mc.player, IStringInput.class);

        if (stack.isEmpty() == false && stack.getItem() == AutoverseItems.WAND)
        {
            return ((IStringInput) stack.getItem()).getCurrentString(this.mc.player, stack);
        }

        return "";
    }
}
