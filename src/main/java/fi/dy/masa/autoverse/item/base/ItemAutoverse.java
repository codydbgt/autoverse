package fi.dy.masa.autoverse.item.base;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.autoverse.Autoverse;
import fi.dy.masa.autoverse.gui.client.base.CreativeTab;
import fi.dy.masa.autoverse.reference.Reference;
import fi.dy.masa.autoverse.reference.ReferenceNames;

public class ItemAutoverse extends Item
{
    public static final String PRE_BLUE = TextFormatting.BLUE.toString();
    public static final String PRE_GREEN = TextFormatting.GREEN.toString();
    public static final String RST_GRAY = TextFormatting.RESET.toString() + TextFormatting.GRAY.toString();
    public static final String RST_WHITE = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

    protected final String name;
    protected String commonTooltip = null;
    protected boolean enabled = true;

    public ItemAutoverse(String name)
    {
        super();

        this.name = name;

        this.setCreativeTab(CreativeTab.AUTOVERSE_TAB);
        this.setUnlocalizedName(name);
        this.addItemOverrides();
    }

    public String getItemName()
    {
        return this.name;
    }

    @Override
    public Item setUnlocalizedName(String name)
    {
        return super.setUnlocalizedName(ReferenceNames.getDotPrefixedName(name));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return slotChanged || ItemStack.areItemStacksEqual(oldStack, newStack) == false;
    }

    public String getBaseItemDisplayName(ItemStack stack)
    {
        // If the item has been renamed, show that name
        if (stack.hasDisplayName())
        {
            String name = stack.getTagCompound().getCompoundTag("display").getString("Name");
            return TextFormatting.ITALIC.toString() + name + TextFormatting.RESET.toString();
        }

        return super.getItemStackDisplayName(stack);
    }

    /**
     * Custom addInformation() method, which allows selecting a subset of the tooltip strings.
     */
    public void addTooltipLines(ItemStack stack, EntityPlayer player, List<String> list, boolean verbose)
    {
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag advanced)
    {
        ArrayList<String> tmpList = new ArrayList<String>();
        boolean verbose = Autoverse.proxy.isShiftKeyDown();

        // "Fresh" items without NBT data: display the tips before the usual tooltip data
        if (stack.getTagCompound() == null)
        {
            this.addTooltips(stack, tmpList, verbose);

            if (verbose == false && tmpList.size() > 2)
            {
                list.add(I18n.format(Reference.MOD_ID + ".tooltip.item.holdshiftfordescription"));
            }
            else
            {
                list.addAll(tmpList);
            }
        }

        tmpList.clear();

        EntityPlayer player = Autoverse.proxy.getClientPlayer();

        this.addTooltipLines(stack, player, tmpList, true);

        // If we want the compact version of the tooltip, and the compact list has more than 2 lines, only show the first line
        // plus the "Hold Shift for more" tooltip.
        if (verbose == false && tmpList.size() > 2)
        {
            tmpList.clear();
            this.addTooltipLines(stack, player, tmpList, false);

            if (tmpList.size() > 0)
            {
                list.add(tmpList.get(0));
            }

            list.add(I18n.format(Reference.MOD_ID + ".tooltip.item.holdshift"));
        }
        else
        {
            list.addAll(tmpList);
        }
    }

    /**
     * Adds a translated tooltip.
     * @return true, if there was a translation found for the given key
     */
    public static boolean addTranslatedTooltip(String key, List<String> list, boolean verbose, Object... args)
    {
        String translated = I18n.format(key, args);

        // Translation found
        if (translated.equals(key) == false)
        {
            // We currently use '|lf' as a delimiter to split the string into multiple lines
            if (translated.contains("|lf"))
            {
                String[] lines = translated.split(Pattern.quote("|lf"));

                for (String line : lines)
                {
                    list.add(line);
                }
            }
            else
            {
                list.add(translated);
            }

            return true;
        }

        return false;
    }

    public void addTooltips(ItemStack stack, List<String> list, boolean verbose)
    {
        addTranslatedTooltip(this.getUnlocalizedName(stack) + ".tooltips", list, verbose);

        if (this.commonTooltip != null)
        {
            addTranslatedTooltip(this.commonTooltip, list, verbose);
        }
    }

    public void getSubItemsCustom(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        super.getSubItems(tab, items);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if (this.isInCreativeTab(tab))
        {
            this.getSubItemsCustom(tab, items);
        }
    }

    public boolean isEnabled()
    {
        return this.enabled;
    }

    public ItemAutoverse setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    public ResourceLocation[] getItemVariants()
    {
        return new ResourceLocation[] { ForgeRegistries.ITEMS.getKey(this) };
    }

    public ModelResourceLocation getModelLocation(ItemStack stack)
    {
        return null;
    }

    protected void addItemOverrides()
    {
    }
}
