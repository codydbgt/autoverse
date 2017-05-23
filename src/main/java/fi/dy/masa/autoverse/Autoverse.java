package fi.dy.masa.autoverse;

import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import fi.dy.masa.autoverse.block.base.AutoverseBlocks;
import fi.dy.masa.autoverse.commands.CommandLoadConfigs;
import fi.dy.masa.autoverse.config.Configs;
import fi.dy.masa.autoverse.gui.AutoverseGuiHandler;
import fi.dy.masa.autoverse.network.PacketHandler;
import fi.dy.masa.autoverse.proxy.CommonProxy;
import fi.dy.masa.autoverse.reference.Reference;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION,
    guiFactory = "fi.dy.masa.autoverse.config.AutoverseGuiFactory",
    updateJSON = "https://raw.githubusercontent.com/maruohon/autoverse/master/update.json",
    acceptedMinecraftVersions = "1.11.2")
public class Autoverse
{
    @Instance(Reference.MOD_ID)
    public static Autoverse instance;

    @SidedProxy(clientSide = "fi.dy.masa.autoverse.proxy.ClientProxy", serverSide = "fi.dy.masa.autoverse.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        instance = this;
        logger = event.getModLog();
        Configs.loadConfigsFromFile(event.getSuggestedConfigurationFile());
        AutoverseBlocks.init();
        PacketHandler.init();

        proxy.registerEventHandlers();
        proxy.registerModels();
        proxy.registerTileEntities();

        NetworkRegistry.INSTANCE.registerGuiHandler(this, new AutoverseGuiHandler());
    }

    @Mod.EventHandler
    public void onServerStart(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandLoadConfigs());
    }
}
