package fi.dy.masa.autoverse.block.base;

import net.minecraft.block.material.Material;
import net.minecraftforge.fml.common.registry.GameRegistry;
import fi.dy.masa.autoverse.block.*;
import fi.dy.masa.autoverse.reference.Reference;
import fi.dy.masa.autoverse.reference.ReferenceNames;

public class AutoverseBlocks
{
    public static final BlockAutoverse BARREL                   = new BlockBarrel(ReferenceNames.NAME_BLOCK_BARREL,                     4.0f, 10f, 1, Material.ROCK);
    public static final BlockAutoverse BLOCK_READER             = new BlockReader(ReferenceNames.NAME_BLOCK_BLOCK_READER,               4.0f, 10f, 1, Material.ROCK);
    public static final BlockAutoverse BREAKER                  = new BlockBreaker(ReferenceNames.NAME_BLOCK_BREAKER,                   4.0f, 10f, 1, Material.ROCK);
    public static final BlockAutoverse BUFFER                   = new BlockBuffer(ReferenceNames.NAME_BLOCK_BUFFER,                     4.0f, 10f, 1, Material.IRON);
    public static final BlockAutoverse CRAFTER                  = new BlockCrafter(ReferenceNames.NAME_BLOCK_CRAFTER,                   4.0f, 10f, 1, Material.ROCK);
    public static final BlockAutoverse FILTER_BASIC             = new BlockFilter(ReferenceNames.NAME_BLOCK_FILTER,                     4.0f, 10f, 1, Material.IRON);
    public static final BlockAutoverse FILTER_SEQUENTIAL        = new BlockFilterSequential(ReferenceNames.NAME_BLOCK_FILTER_SEQUENTIAL,4.0f, 10f, 1, Material.IRON);
    public static final BlockAutoverse INVENTORY_READER         = new BlockInventoryReader(ReferenceNames.NAME_BLOCK_INVENTORY_READER,  4.0f, 10f, 1, Material.ROCK);
    public static final BlockAutoverse PLACER                   = new BlockPlacer(ReferenceNames.NAME_BLOCK_PLACER,                     4.0f, 10f, 1, Material.ROCK);
    public static final BlockAutoverse REDSTONE_EMITTER         = new BlockRedstoneEmitter(ReferenceNames.NAME_BLOCK_REDSTONE_EMITTER,  4.0f, 10f, 1, Material.IRON);
    public static final BlockAutoverse SEQUENCE_DETECTOR        = new BlockSequenceDetector(ReferenceNames.NAME_BLOCK_SEQUENCE_DETECTOR,4.0f, 10f, 1, Material.IRON);
    public static final BlockAutoverse SEQUENCER                = new BlockSequencer(ReferenceNames.NAME_BLOCK_SEQUENCER,               4.0f, 10f, 1, Material.IRON);
    public static final BlockAutoverse SEQUENCER_PROGRAMMABLE   = new BlockSequencerProgrammable(ReferenceNames.NAME_BLOCK_SEQUENCER_PROGRAMMABLE, 4.0f, 10f, 1, Material.IRON);
    public static final BlockAutoverse SPLITTER                 = new BlockSplitter(ReferenceNames.NAME_BLOCK_SPLITTER,                 4.0f, 10f, 1, Material.IRON);

    public static void init()
    {
        registerBlock(BARREL,                   false);
        registerBlock(BLOCK_READER,             false);
        registerBlock(BREAKER,                  false);
        registerBlock(BUFFER,                   false);
        registerBlock(CRAFTER,                  false);
        registerBlock(FILTER_BASIC,             false);
        registerBlock(FILTER_SEQUENTIAL,        false);
        registerBlock(INVENTORY_READER,         false);
        registerBlock(PLACER,                   false);
        registerBlock(REDSTONE_EMITTER,         false);
        registerBlock(SEQUENCE_DETECTOR,        false);
        registerBlock(SEQUENCER,                false);
        registerBlock(SEQUENCER_PROGRAMMABLE,   false);
        registerBlock(SPLITTER,                 false);
    }

    private static void registerBlock(BlockAutoverse block, boolean isDisabled)
    {
        registerBlock(block, isDisabled, true);
    }

    private static void registerBlock(BlockAutoverse block, boolean isDisabled, boolean createItemBlock)
    {
        registerBlock(block, isDisabled, createItemBlock, true);
    }

    private static void registerBlock(BlockAutoverse block, boolean isDisabled, boolean createItemBlock, boolean hasSubtypes)
    {
        if (isDisabled == false)
        {
            block.setRegistryName(Reference.MOD_ID + ":" + block.getBlockName());
            GameRegistry.register(block);

            if (createItemBlock)
            {
                GameRegistry.register(block.createItemBlock().setHasSubtypes(hasSubtypes).setRegistryName(Reference.MOD_ID, block.getBlockName()));
            }
        }
        else
        {
            block.setEnabled(false);
        }
    }
}
