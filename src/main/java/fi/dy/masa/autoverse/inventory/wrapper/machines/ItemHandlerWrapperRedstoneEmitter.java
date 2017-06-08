package fi.dy.masa.autoverse.inventory.wrapper.machines;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.autoverse.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.autoverse.tileentity.TileEntityRedstoneEmitter;
import fi.dy.masa.autoverse.util.InventoryUtils;

public class ItemHandlerWrapperRedstoneEmitter extends ItemHandlerWrapperSequenceBase
{
    private final TileEntityRedstoneEmitter te;
    private final SequenceMatcher sequenceMarkerItem;
    private final SequenceMatcher sequenceSwitchOn;
    private final SequenceMatcher sequenceSwitchOff;
    private final IItemHandler markerInventory;
    private Mode mode = Mode.CONFIGURE_RESET;
    private boolean isOn;
    private int position;

    public ItemHandlerWrapperRedstoneEmitter(int resetSequenceLength,
            ItemStackHandlerTileEntity inventoryInput, TileEntityRedstoneEmitter te)
    {
        super(resetSequenceLength, inventoryInput);
        this.te = te;

        this.sequenceMarkerItem = new SequenceMatcher(1, "SequenceMarker");
        this.sequenceSwitchOn   = new SequenceMatcher(3, "SequenceOn");
        this.sequenceSwitchOff  = new SequenceMatcher(3, "SequenceOff");

        this.markerInventory = this.sequenceMarkerItem.getSequenceInventory(false);
    }

    @Override
    protected void handleInputItem(ItemStack inputStack)
    {
        switch (this.getMode())
        {
            case CONFIGURE_RESET:
                if (this.getResetSequence().configureSequence(inputStack))
                {
                    this.setMode(Mode.CONFIGURE_MARKER);
                }
                break;

            case CONFIGURE_MARKER:
                if (this.sequenceMarkerItem.configureSequence(inputStack))
                {
                    this.setMode(Mode.CONFIGURE_SIDES);
                }
                break;

            case CONFIGURE_SIDES:
                boolean enabled = InventoryUtils.areItemStacksEqual(inputStack, this.markerInventory.getStackInSlot(0));

                this.te.setSideEnabled(this.position, enabled);

                if (++this.position >= 6)
                {
                    this.position = 0;
                    this.setMode(Mode.CONFIGURE_SEQUENCE_ON);
                }
                break;

            case CONFIGURE_SEQUENCE_ON:
                if (this.sequenceSwitchOn.configureSequence(inputStack))
                {
                    this.setMode(Mode.CONFIGURE_SEQUENCE_OFF);
                }
                break;

            case CONFIGURE_SEQUENCE_OFF:
                if (this.sequenceSwitchOff.configureSequence(inputStack))
                {
                    this.setMode(Mode.NORMAL_OPERATION);
                }
                break;

            case NORMAL_OPERATION:
                if (this.getResetSequence().checkInputItem(inputStack))
                {
                    this.getResetSequence().reset();
                    this.sequenceMarkerItem.reset();
                    this.sequenceSwitchOn.reset();
                    this.sequenceSwitchOff.reset();
                    this.setIsOn(false);
                    this.te.setSideMask(0);
                    this.position = 0;
                    this.setMode(Mode.CONFIGURE_RESET);
                }
                else if (this.isOn == false && this.sequenceSwitchOn.checkInputItem(inputStack))
                {
                    this.setIsOn(true);
                }
                else if (this.isOn && this.sequenceSwitchOff.checkInputItem(inputStack))
                {
                    this.setIsOn(false);
                }
                break;

            default:
                break;
        }
    }

    public IItemHandler getMarkerInventory()
    {
        return this.markerInventory;
    }

    public SequenceMatcher getSwitchOnSequence()
    {
        return this.sequenceSwitchOn;
    }

    public SequenceMatcher getSwitchOffSequence()
    {
        return this.sequenceSwitchOff;
    }

    protected Mode getMode()
    {
        return this.mode;
    }

    protected void setMode(Mode mode)
    {
        this.mode = mode;
    }

    private void setIsOn(boolean isOn)
    {
        this.isOn = isOn;
        this.te.setIsPowered(isOn);
    }

    @Override
    protected NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
        tag = super.writeToNBT(tag);

        tag.setByte("State", (byte) ((this.isOn ? 0x80 : 0x00) | this.mode.getId()));
        tag.setByte("Position", (byte) this.position);

        this.sequenceMarkerItem.writeToNBT(tag);
        this.sequenceSwitchOn.writeToNBT(tag);
        this.sequenceSwitchOff.writeToNBT(tag);

        return tag;
    }

    @Override
    protected void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);

        int state = tag.getByte("State");
        this.setMode(Mode.fromId(state & 0x7));
        this.setIsOn((state & 0x80) != 0);
        this.position = tag.getByte("Position");

        this.sequenceMarkerItem.readFromNBT(tag);
        this.sequenceSwitchOn.readFromNBT(tag);
        this.sequenceSwitchOff.readFromNBT(tag);
    }

    public enum Mode
    {
        CONFIGURE_RESET         (0),
        CONFIGURE_MARKER        (1),
        CONFIGURE_SIDES         (2),
        CONFIGURE_SEQUENCE_ON   (3),
        CONFIGURE_SEQUENCE_OFF  (4),
        NORMAL_OPERATION        (5);

        private final int id;

        private Mode (int id)
        {
            this.id = id;
        }

        public int getId()
        {
            return this.id;
        }

        public static Mode fromId(int id)
        {
            return values()[id % values().length];
        }
    }
}