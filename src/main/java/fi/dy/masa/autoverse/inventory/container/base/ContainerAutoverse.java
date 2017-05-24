package fi.dy.masa.autoverse.inventory.container.base;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;
import fi.dy.masa.autoverse.Autoverse;
import fi.dy.masa.autoverse.inventory.slot.SlotItemHandlerCraftResult;
import fi.dy.masa.autoverse.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.autoverse.inventory.wrapper.PlayerInvWrapperNoSync;
import fi.dy.masa.autoverse.network.PacketHandler;
import fi.dy.masa.autoverse.network.message.MessageSyncCustomSlot;
import fi.dy.masa.autoverse.network.message.MessageSyncSlot;

public class ContainerAutoverse extends Container
{
    protected final EntityPlayer player;
    protected final boolean isClient;
    protected final InventoryPlayer inventoryPlayer;
    protected final IItemHandlerModifiable playerInv;
    protected final IItemHandler inventory;
    private List<Slot> specialSlots = new ArrayList<Slot>();
    private NonNullList<ItemStack> specialSlotStacks = NonNullList.create();
    protected MergeSlotRange customInventorySlots;
    protected MergeSlotRange playerMainSlots;
    protected MergeSlotRange playerHotbarSlots;
    protected MergeSlotRange playerMainSlotsIncHotbar;
    protected MergeSlotRange playerOffhandSlots;
    protected MergeSlotRange playerArmorSlots;
    protected List<MergeSlotRange> mergeSlotRangesExtToPlayer;
    protected List<MergeSlotRange> mergeSlotRangesPlayerToExt;
    protected boolean forceSyncAll;

    public ContainerAutoverse(EntityPlayer player, IItemHandler inventory)
    {
        this.player = player;
        this.isClient = player.getEntityWorld().isRemote;
        this.inventoryPlayer = player.inventory;
        this.playerInv = new PlayerInvWrapperNoSync(player.inventory);
        this.inventory = inventory;
        this.mergeSlotRangesExtToPlayer = new ArrayList<MergeSlotRange>();
        this.mergeSlotRangesPlayerToExt = new ArrayList<MergeSlotRange>();

        // Init the ranges to an empty range by default
        this.customInventorySlots       = new MergeSlotRange(0, 0);
        this.playerMainSlotsIncHotbar   = new MergeSlotRange(0, 0);
        this.playerMainSlots            = new MergeSlotRange(0, 0);
        this.playerHotbarSlots          = new MergeSlotRange(0, 0);
        this.playerOffhandSlots         = new MergeSlotRange(0, 0);
        this.playerArmorSlots           = new MergeSlotRange(0, 0);
    }

    /**
     * Adds the "custom inventory" slots to the container (ie. the inventory that this container is for).
     * This must be called before addPlayerInventorySlots() (ie. the order of slots in the container
     * is important for the transferStackInSlot() method)!
     */
    protected void addCustomInventorySlots()
    {
    }

    /**
     * Adds the player inventory slots to the container.
     * posX and posY are the positions of the top-left-most slot of the player inventory.
     */
    protected void addPlayerInventorySlots(int posX, int posY)
    {
        // This should usually be sufficient, assuming the custom slots are added first
        //this.customInventorySlots = new SlotRange(0, this.inventorySlots.size());

        int playerInvStart = this.inventorySlots.size();

        // Player inventory
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.playerInv, i * 9 + j + 9, posX + j * 18, posY + i * 18));
            }
        }

        this.playerMainSlots = new MergeSlotRange(playerInvStart, 27);
        int playerHotbarStart = this.inventorySlots.size();

        // Player inventory hotbar
        for (int i = 0; i < 9; i++)
        {
            this.addSlotToContainer(new SlotItemHandlerGeneric(this.playerInv, i, posX + i * 18, posY + 58));
        }

        this.playerMainSlotsIncHotbar = new MergeSlotRange(playerInvStart, 36);
        this.playerHotbarSlots = new MergeSlotRange(playerHotbarStart, 9);
    }

    protected void addOffhandSlot(int posX, int posY)
    {
        this.playerOffhandSlots = new MergeSlotRange(this.inventorySlots.size(), 1);

        // Add the Offhand slot
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.playerInv, 40, posX, posY)
        {
            @SideOnly(Side.CLIENT)
            public String getSlotTexture()
            {
                return "minecraft:items/empty_armor_slot_shield";
            }
        });
    }

    public EntityPlayer getPlayer()
    {
        return this.player;
    }

    public SlotRange getPlayerMainInventorySlotRange()
    {
        return this.playerMainSlotsIncHotbar;
    }

    public SlotRange getPlayerArmorSlots()
    {
        return this.playerArmorSlots;
    }

    public SlotRange getCustomInventorySlotRange()
    {
        return this.customInventorySlots;
    }

    public List<Slot> getSpecialSlots()
    {
        return this.specialSlots;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return true;
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slot)
    {
        return (slot instanceof SlotItemHandler) &&
                (slot instanceof SlotItemHandlerCraftResult) == false &&
                //(slot instanceof SlotItemHandlerFurnaceOutput) == false &&
                this.inventoryPlayer.getItemStack().isEmpty() == false;
    }

    @Override
    public Slot getSlot(int slotId)
    {
        return slotId >= 0 && slotId < this.inventorySlots.size() ? super.getSlot(slotId) : null;
    }

    public SlotItemHandlerGeneric getSlotItemHandler(int slotId)
    {
        Slot slot = this.getSlot(slotId);

        return (slot instanceof SlotItemHandlerGeneric) ? (SlotItemHandlerGeneric) slot : null;
    }

    /**
     * Will put the given stack into the slot, ignoring any validity checks.
     * This will and should only be used for syncing slots to the client.
     * @param slot
     * @param stack
     */
    public void syncStackInSlot(int slotId, ItemStack stack)
    {
        Slot slot = this.getSlot(slotId);

        if (slot instanceof SlotItemHandlerGeneric)
        {
            ((SlotItemHandlerGeneric) slot).syncStack(stack);
        }
        else
        {
            this.putStackInSlot(slotId, stack);
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotNum)
    {
        this.transferStackFromSlot(player, slotNum);
        return ItemStack.EMPTY;
    }

    /**
     * Transfers the stack from the given slot into other parts of the inventory,
     * or other inventories in this Container.
     * The player's inventory and the armor slots have highest "swap priority",
     * after that come player inventory to the "priority slots" that can be added to
     * the list of "priority slot" SlotRanges, and after that come the rest of the "custom inventory".
     * Returns false if no items were moved, true otherwise
     */
    protected boolean transferStackFromSlot(EntityPlayer player, int slotNum)
    {
        Slot slot = this.getSlot(slotNum);

        if (slot == null || slot.getHasStack() == false || slot.canTakeStack(player) == false)
        {
            return false;
        }

        // From player armor or offhand slots to the player main inventory
        if (this.playerArmorSlots.contains(slotNum) || this.playerOffhandSlots.contains(slotNum))
        {
            return this.transferStackToSlotRange(player, slotNum, this.playerMainSlotsIncHotbar, false);
        }
        // From player main inventory to armor slots or the "external" inventory
        else if (this.playerMainSlotsIncHotbar.contains(slotNum))
        {
            return this.transferStackFromPlayerMainInventory(player, slotNum);
        }

        // From external inventory to player inventory
        return this.transferStackToSlotRange(player, slotNum, this.playerMainSlotsIncHotbar, true);
    }

    protected boolean transferStackFromPlayerMainInventory(EntityPlayer player, int slotNum)
    {
        if (this.transferStackToSlotRange(player, slotNum, this.playerArmorSlots, false))
        {
            return true;
        }

        if (this.transferStackToPrioritySlots(player, slotNum, false))
        {
            return true;
        }

        return this.transferStackToSlotRange(player, slotNum, this.customInventorySlots, false);
    }

    protected boolean transferStackToPrioritySlots(EntityPlayer player, int slotNum, boolean reverse)
    {
        boolean ret = false;

        for (MergeSlotRange slotRange : this.mergeSlotRangesPlayerToExt)
        {
            ret |= this.transferStackToSlotRange(player, slotNum, slotRange, reverse);
        }

        return ret;
    }

    protected boolean transferStackToSlotRange(EntityPlayer player, int slotNum, MergeSlotRange slotRange, boolean reverse)
    {
        SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotNum);

        if (slot == null || slot.getHasStack() == false || slot.canTakeStack(player) == false)
        {
            return false;
        }

        ItemStack stack = slot.getStack().copy();
        int amount = Math.min(stack.getCount(), stack.getMaxStackSize());
        stack.setCount(amount);

        // Simulate the merge
        stack = this.mergeItemStack(stack, slotRange, reverse, true);

        if (stack.isEmpty() == false)
        {
            // If the item can't be put back to the slot, then we need to make sure that the whole
            // stack can be merged elsewhere before trying to (partially) merge it. Important for crafting slots!
            // Or if nothing could be merged, then also abort.
            if (slot.isItemValid(stack) == false || stack.getCount() == amount)
            {
                return false;
            }

            // Can merge at least some of the items, get the amount that can be merged
            amount -= stack.getCount();
        }

        // Get the actual stack for non-simulated merging
        stack = slot.decrStackSize(amount);
        slot.onTake(player, stack);

        // Actually merge the items
        stack = this.mergeItemStack(stack, slotRange, reverse, false);

        // If they couldn't fit after all, then return them.
        // This shouldn't happen, and will cause some issues like gaining XP from nothing in furnaces.
        if (stack.isEmpty() == false)
        {
            slot.insertItem(stack, false);

            Autoverse.logger.warn("Failed to merge all items in '{}'. This shouldn't happen and should be reported.",
                    this.getClass().getSimpleName());
        }

        return true;
    }

    /**
     * Returns the maximum allowed stack size, based on the given ItemStack and the inventory's max stack size.
     */
    protected int getMaxStackSizeFromSlotAndStack(Slot slot, ItemStack stack)
    {
        return stack.isEmpty() == false ? Math.min(slot.getItemStackLimit(stack), stack.getMaxStackSize()) : slot.getSlotStackLimit();
    }

    /**
     * This should NOT be called from anywhere in this mod, but just in case...
     */
    @Override
    protected boolean mergeItemStack(ItemStack stack, int slotStart, int slotEndExclusive, boolean reverse)
    {
        return false;
    }

    /**
     * Merge the given ItemStack to the slot range provided.
     * If simulate is true, then we are checking if the WHOLE stack can be merged.
     * @return If simulate is false, then true is returned if at least some of the items were merged.
     * If simulate is true, then true is returned only if ALL the items were successfully merged.
     */
    protected ItemStack mergeItemStack(ItemStack stack, MergeSlotRange slotRange, boolean reverse, boolean simulate)
    {
        int slotStart = slotRange.first;
        int slotEndExclusive = slotRange.lastExc;
        int slotIndex = (reverse ? slotEndExclusive - 1 : slotStart);

        // First try to merge the stack into existing stacks in the container
        while (stack.isEmpty() == false && slotIndex >= slotStart && slotIndex < slotEndExclusive)
        {
            SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotIndex);

            if (slot != null && slot.getHasStack() && slot.isItemValid(stack))
            {
                stack = slot.insertItem(stack, simulate);
            }

            slotIndex = (reverse ? slotIndex - 1 : slotIndex + 1);
        }

        // If there are still items to merge after merging to existing stacks, then try to add it to empty slots
        if (stack.isEmpty() == false && slotRange.existingOnly == false)
        {
            slotIndex = (reverse ? slotEndExclusive - 1 : slotStart);

            while (stack.isEmpty() == false && slotIndex >= slotStart && slotIndex < slotEndExclusive)
            {
                SlotItemHandlerGeneric slot = this.getSlotItemHandler(slotIndex);

                if (slot != null && slot.getHasStack() == false && slot.isItemValid(stack))
                {
                    stack = slot.insertItem(stack, simulate);
                }

                slotIndex = (reverse ? slotIndex - 1 : slotIndex + 1);
            }
        }

        return stack;
    }

    protected void addMergeSlotRangeExtToPlayer(int start, int numSlots)
    {
        this.addMergeSlotRangeExtToPlayer(start, numSlots, false);
    }

    protected void addMergeSlotRangeExtToPlayer(int start, int numSlots, boolean existingOnly)
    {
        this.mergeSlotRangesExtToPlayer.add(new MergeSlotRange(start, numSlots, existingOnly));
    }

    protected void addMergeSlotRangePlayerToExt(int start, int numSlots)
    {
        this.addMergeSlotRangePlayerToExt(start, numSlots, false);
    }

    protected void addMergeSlotRangePlayerToExt(int start, int numSlots, boolean existingOnly)
    {
        this.mergeSlotRangesPlayerToExt.add(new MergeSlotRange(start, numSlots, existingOnly));
    }

    public void performGuiAction(EntityPlayer player, int action, int element)
    {
    }

    /**
     * Syncs the locked status and the current template ItemStack in a lockable inventory.
     * The current values are cached into the provided boolean and ItemStack arrays.
     * The locked status is sent via a sendProgressBarUpdate, using the id <b>progressBarId</b>.
     * The value for that is in the form '(locked ? 0x8000 : 0) | slot'.
     *
     * @param inv the lockable inventory to sync the locked status and template stacks from
     * @param typeId The id that is used in the MessageSyncCustomSlot packet
     * @param progressBarId The id to use in the sendProgressBarUpdate for the locked status
     * @param lockedLast an array for caching the locked status
     * @param templateStacksLast a list for caching the template stacks
     */
    /*
    protected void syncLockableSlots(ItemStackHandlerLockable inv, int typeId, int progressBarId,
            boolean[] lockedLast, NonNullList<ItemStack> templateStacksLast)
    {
        final int numSlots = inv.getSlots();

        for (int slot = 0; slot < numSlots; slot++)
        {
            boolean locked = inv.isSlotLocked(slot);

            if (lockedLast[slot] != locked)
            {
                for (int i = 0; i < this.listeners.size(); i++)
                {
                    this.listeners.get(i).sendProgressBarUpdate(this, progressBarId, (locked ? 0x8000 : 0) | slot);
                }

                lockedLast[slot] = locked;
            }

            ItemStack templateStack = inv.getTemplateStackInSlot(slot);

            if (InventoryUtils.areItemStacksEqual(templateStacksLast.get(slot), templateStack) == false)
            {
                for (int i = 0; i < this.listeners.size(); i++)
                {
                    IContainerListener listener = this.listeners.get(i);

                    if (listener instanceof EntityPlayerMP)
                    {
                        PacketHandler.INSTANCE.sendTo(
                            new MessageSyncCustomSlot(this.windowId, typeId, slot, templateStack), (EntityPlayerMP) listener);
                    }
                }

                templateStacksLast.set(slot, templateStack.copy());
            }
        }
    }
    */

    /**
     * Adds a special slot to the Container, which can't be interacted with
     */
    protected Slot addSpecialSlot(Slot slotIn)
    {
        slotIn.slotNumber = this.inventorySlots.size();
        this.specialSlots.add(slotIn);
        this.specialSlotStacks.add(ItemStack.EMPTY);
        return slotIn;
    }

    @Override
    public void addListener(IContainerListener listener)
    {
        if (this.listeners.contains(listener))
        {
            throw new IllegalArgumentException("Listener already listening");
        }

        this.listeners.add(listener);

        if (listener instanceof EntityPlayerMP)
        {
            EntityPlayerMP player = (EntityPlayerMP) listener;
            player.connection.sendPacket(new SPacketSetSlot(-1, -1, player.inventory.getItemStack()));
        }

        this.forceSyncAll = true;
        this.detectAndSendChanges();
        this.forceSyncAll = false;
    }

    protected void syncAllSlots()
    {
        if (this.isClient == false)
        {
            final int invSize = this.inventorySlots.size();

            for (int slot = 0; slot < invSize; slot++)
            {
                ItemStack currentStack = this.inventorySlots.get(slot).getStack();
                ItemStack prevStack = this.inventoryItemStacks.get(slot);

                if (this.forceSyncAll || ItemStack.areItemStacksEqual(prevStack, currentStack) == false)
                {
                    prevStack = currentStack.isEmpty() ? ItemStack.EMPTY : currentStack.copy();
                    this.inventoryItemStacks.set(slot, prevStack);

                    for (int i = 0; i < this.listeners.size(); i++)
                    {
                        IContainerListener listener = this.listeners.get(i);

                        if (listener instanceof EntityPlayerMP)
                        {
                            PacketHandler.INSTANCE.sendTo(new MessageSyncSlot(this.windowId, slot, prevStack), (EntityPlayerMP) listener);
                        }
                    }
                }
            }

            final int specialSize = this.specialSlots.size();

            for (int slot = 0; slot < specialSize; slot++)
            {
                ItemStack currentStack = this.specialSlots.get(slot).getStack();
                ItemStack prevStack = this.specialSlotStacks.get(slot);

                if (this.forceSyncAll || ItemStack.areItemStacksEqual(prevStack, currentStack) == false)
                {
                    prevStack = currentStack.isEmpty() ? ItemStack.EMPTY : currentStack.copy();
                    this.specialSlotStacks.set(slot, prevStack);

                    for (int i = 0; i < this.listeners.size(); i++)
                    {
                        IContainerListener listener = this.listeners.get(i);
                        if (listener instanceof EntityPlayerMP)
                        {
                            PacketHandler.INSTANCE.sendTo(
                                new MessageSyncCustomSlot(this.windowId, 10, slot, prevStack), (EntityPlayerMP) listener);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void detectAndSendChanges()
    {
        this.syncAllSlots();
    }
}
