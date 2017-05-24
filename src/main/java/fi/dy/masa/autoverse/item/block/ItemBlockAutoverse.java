package fi.dy.masa.autoverse.item.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.autoverse.Autoverse;
import fi.dy.masa.autoverse.block.base.BlockAutoverse;
import fi.dy.masa.autoverse.client.HotKeys;
import fi.dy.masa.autoverse.client.HotKeys.EnumKey;
import fi.dy.masa.autoverse.item.base.IKeyBound;
import fi.dy.masa.autoverse.item.base.ItemAutoverse;
import fi.dy.masa.autoverse.reference.Reference;
import fi.dy.masa.autoverse.reference.ReferenceNames;
import fi.dy.masa.autoverse.util.ItemType;
import fi.dy.masa.autoverse.util.PlacementProperties;

public class ItemBlockAutoverse extends ItemBlock implements IKeyBound
{
    private final BlockAutoverse blockEnu;
    protected String[] blockNames;
    protected String[] tooltipNames;
    private boolean hasPlacementProperties;
    private boolean placementPropertyNBTSensitive;
    private List<Pair<String, Integer>> placementPropertyTypes = new ArrayList<Pair<String, Integer>>();
    private List<Pair<Integer, Integer>> placementPropertyValueRange = new ArrayList<Pair<Integer, Integer>>();
    private Map<String, String[]> placementPropertyValueNames = new HashMap<String, String[]>();

    public ItemBlockAutoverse(BlockAutoverse block)
    {
        super(block);

        this.blockEnu = block;
        this.setHasSubtypes(true);
        this.setMaxDamage(0);

        this.setBlockNames(block.getUnlocalizedNames());
        this.setTooltipNames(block.getTooltipNames());
    }

    public void setBlockNames(String[] names)
    {
        this.blockNames = names;
    }

    public void setTooltipNames(String[] names)
    {
        this.tooltipNames = names;
    }

    public boolean hasPlacementProperties()
    {
        return this.hasPlacementProperties && this.placementPropertyTypes.isEmpty() == false;
    }

    public void setHasPlacementProperties(boolean hasProps)
    {
        this.hasPlacementProperties = hasProps;
    }

    public boolean getPlacementPropertyNBTSensitive()
    {
        return this.placementPropertyNBTSensitive;
    }

    public void setPlacementPropertyNBTSensitive(boolean checkNBT)
    {
        this.placementPropertyNBTSensitive = checkNBT;
    }

    public void addPlacementProperty(String key, int type, int minValue, int maxValue)
    {
        this.placementPropertyTypes.add(Pair.of(key, type));
        this.placementPropertyValueRange.add(Pair.of(minValue, maxValue));
    }

    public void addPlacementPropertyValueNames(String key, String[] names)
    {
        this.placementPropertyValueNames.put(key, names);
    }

    @Nullable
    public Pair<String, Integer> getPlacementProperty(int index)
    {
        return index >= 0 && index < this.placementPropertyTypes.size() ? this.placementPropertyTypes.get(index) : null;
    }

    @Nullable
    public Pair<Integer, Integer> getPlacementPropertyValueRange(int index)
    {
        return index >= 0 && index < this.placementPropertyValueRange.size() ? this.placementPropertyValueRange.get(index) : null;
    }

    @Nullable
    public String getPlacementPropertyValueName(String key, int index)
    {
        String[] names = this.placementPropertyValueNames.get(key);

        if (names != null && index >= 0 && index < names.length)
        {
            return names[index];
        }

        return null;
    }

    public int getPlacementPropertyCount()
    {
        return this.placementPropertyTypes.size();
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        ItemBlockAutoverse item = (ItemBlockAutoverse) stack.getItem();

        if (item.hasPlacementProperties() && player instanceof EntityPlayerMP)
        {
            ItemType type = new ItemType(stack, this.placementPropertyNBTSensitive);
            int index = PlacementProperties.getInstance().getPropertyIndex(player.getUniqueID(), type);

            if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_NONE) || EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT))
            {
                index += EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT) ? -1 : 1;
                if (index < 0) { index = Math.max(0, this.getPlacementPropertyCount() - 1); }
                else if (index >= this.getPlacementPropertyCount()) { index = 0; }

                PlacementProperties.getInstance().setPropertyIndex(player.getUniqueID(), type, index);
                PlacementProperties.getInstance().syncCurrentlyHeldItemDataForPlayer((EntityPlayerMP) player, stack);
            }
            else
            {
                Pair<String, Integer> pair = item.getPlacementProperty(index);
                Pair<Integer, Integer> range = item.getPlacementPropertyValueRange(index);
                int minValue = range != null ? range.getLeft() : 0;
                int maxValue = range != null ? range.getRight() : 1;

                if (pair != null && EnumKey.getBaseKey(key) == EnumKey.SCROLL.getKeyCode())
                {
                    int change = EnumKey.keypressActionIsReversed(key) ? -1 : 1;

                    if (EnumKey.keypressContainsControl(key)) { change *= 10;  }
                    if (EnumKey.keypressContainsAlt(key))     { change *= 100; }

                    int value = PlacementProperties.getInstance().getPropertyValue(player.getUniqueID(), type, pair.getLeft(), pair.getRight());
                    value += change;

                    if (value < minValue) { value = maxValue; }
                    if (value > maxValue) { value = minValue; }

                    PlacementProperties.getInstance().setPropertyValue(player.getUniqueID(), type, pair.getLeft(), pair.getRight(), value);
                    PlacementProperties.getInstance().syncCurrentlyHeldItemDataForPlayer((EntityPlayerMP) player, stack);
                }
            }
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = player.getHeldItem(hand);
        ItemType type = new ItemType(stack, this.placementPropertyNBTSensitive);
        stack = stack.copy();
        EnumActionResult result = super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);

        if (result == EnumActionResult.SUCCESS && this.hasPlacementProperties())
        {
            NBTTagCompound tag = PlacementProperties.getInstance().getPropertyTag(player.getUniqueID(), type);

            if (tag != null)
            {
                IBlockState state = worldIn.getBlockState(pos);

                if (state.getBlock().isReplaceable(worldIn, pos) == false)
                {
                    pos = pos.offset(facing);
                }

                if (worldIn.getBlockState(pos).getBlock() instanceof BlockAutoverse)
                {
                    this.blockEnu.setPlacementProperties(worldIn, pos, stack, tag);
                }
            }
        }

        return result;
    }

    @Override
    public int getMetadata(int meta)
    {
        return meta;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        if (this.blockNames != null && stack.getMetadata() < this.blockNames.length)
        {
            return "tile." + ReferenceNames.getDotPrefixedName(this.blockNames[stack.getMetadata()]);
        }

        return super.getUnlocalizedName(stack);
    }

    public String getTooltipName(ItemStack stack)
    {
        if (this.tooltipNames != null)
        {
            if (stack.getMetadata() < this.tooltipNames.length)
            {
                return "tile." + ReferenceNames.getDotPrefixedName(this.tooltipNames[stack.getMetadata()]);
            }
            // Some blocks may have a common tooltip for all different states/meta values,
            // by only including one entry in the array
            else if (this.tooltipNames.length == 1)
            {
                return "tile." + ReferenceNames.getDotPrefixedName(this.tooltipNames[0]);
            }
        }

        return this.getUnlocalizedName(stack);
    }

    @SideOnly(Side.CLIENT)
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips)
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
        this.addInformationSelective(stack, player, tmpList, advancedTooltips, true);

        // If we want the compact version of the tooltip, and the compact list has more than 2 lines, only show the first line
        // plus the "Hold Shift for more" tooltip.
        if (verbose == false && tmpList.size() > 2)
        {
            tmpList.clear();
            this.addInformationSelective(stack, player, tmpList, advancedTooltips, false);

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

    @SideOnly(Side.CLIENT)
    public void addTooltips(ItemStack stack, List<String> list, boolean verbose)
    {
        ItemAutoverse.addTooltips(this.getTooltipName(stack) + ".tooltips", list, verbose);

        if (this.hasPlacementProperties())
        {
            ItemAutoverse.addTooltips(Reference.MOD_ID + ".tooltip.placementproperties.tooltips", list, verbose);
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean isFull3D()
    {
        return true;
    }

    /*
    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
    {
        if (world.setBlockState(pos, newState, 3) == false)
        {
            return false;
        }

        IBlockState state = world.getBlockState(pos);

        if (state.getBlock() == this.block)
        {
            setTileEntityNBT(world, player, pos, stack);

            if (this.block instanceof BlockAutoverseTileEntity)
            {
                ((BlockAutoverseTileEntity) this.block).onBlockPlacedBy(world, pos, side, state, player, stack);
            }
            else
            {
                this.block.onBlockPlacedBy(world, pos, state, player, stack);
            }
        }

        return true;
    }
    */
}
