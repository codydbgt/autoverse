package fi.dy.masa.autoverse.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.GameRegistry;
import fi.dy.masa.autoverse.Autoverse;
import fi.dy.masa.autoverse.reference.Reference;
import fi.dy.masa.autoverse.reference.ReferenceNames;
import fi.dy.masa.autoverse.tileentity.*;

public class CommonProxy
{
    public EntityPlayer getPlayerFromMessageContext(MessageContext ctx)
    {
        switch (ctx.side)
        {
            case SERVER:
                return ctx.getServerHandler().player;
            default:
                Autoverse.logger.warn("Invalid side in getPlayerFromMessageContext(): " + ctx.side);
                return null;
        }
    }

    public ModFixs getDataFixer()
    {
        // On a server, the DataFixer gets created for and is stored inside MinecraftServer,
        // but in single player the DataFixer is stored in the client Minecraft class
        // over world reloads.
        return FMLCommonHandler.instance().getDataFixer().init(Reference.MOD_ID, Autoverse.DATA_FIXER_VERSION);
    }

    public boolean isShiftKeyDown()
    {
        return false;
    }

    public boolean isControlKeyDown()
    {
        return false;
    }

    public boolean isAltKeyDown()
    {
        return false;
    }

    public void registerColorHandlers() { }

    public void registerEventHandlers() { }

    public void registerKeyBindings() { }

    public void registerModels() { }

    public void registerTileEntities()
    {
        this.registerTileEntity(TileEntityBarrel.class,                 ReferenceNames.NAME_BLOCK_BARREL);
        this.registerTileEntity(TileEntityBlockReaderNBT.class,         ReferenceNames.NAME_TILE_ENTITY_BLOCK_READER_NBT);
        this.registerTileEntity(TileEntityBreaker.class,                ReferenceNames.NAME_BLOCK_BREAKER);
        this.registerTileEntity(TileEntityBufferFifo.class,             ReferenceNames.NAME_TILE_ENTITY_BUFFER_FIFO);
        this.registerTileEntity(TileEntityBufferFifoPulsed.class,       ReferenceNames.NAME_TILE_ENTITY_BUFFER_FIFO_PULSED);
        this.registerTileEntity(TileEntityCrafter.class,                ReferenceNames.NAME_BLOCK_CRAFTER);
        this.registerTileEntity(TileEntityFilter.class,                 ReferenceNames.NAME_BLOCK_FILTER);
        this.registerTileEntity(TileEntityFilterSequential.class,       ReferenceNames.NAME_BLOCK_FILTER_SEQUENTIAL);
        this.registerTileEntity(TileEntityInventoryReader.class,        ReferenceNames.NAME_BLOCK_INVENTORY_READER);
        this.registerTileEntity(TileEntityPlacer.class,                 ReferenceNames.NAME_BLOCK_PLACER);
        this.registerTileEntity(TileEntityRedstoneEmitter.class,        ReferenceNames.NAME_BLOCK_REDSTONE_EMITTER);
        this.registerTileEntity(TileEntitySequenceDetector.class,       ReferenceNames.NAME_BLOCK_SEQUENCE_DETECTOR);
        this.registerTileEntity(TileEntitySequencer.class,              ReferenceNames.NAME_BLOCK_SEQUENCER);
        this.registerTileEntity(TileEntitySequencerProgrammable.class,  ReferenceNames.NAME_BLOCK_SEQUENCER_PROGRAMMABLE);
        this.registerTileEntity(TileEntitySplitter.class,               ReferenceNames.NAME_BLOCK_SPLITTER);
    }

    private void registerTileEntity(Class<? extends TileEntity> clazz, String id)
    {
        GameRegistry.registerTileEntity(clazz, Reference.MOD_ID + ":" + id);
    }
}
