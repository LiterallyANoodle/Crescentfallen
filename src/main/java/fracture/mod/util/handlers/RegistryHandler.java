package fracture.mod.util.handlers;

import fracture.mod.init.BlockInit;
import fracture.mod.init.ItemInit;
import fracture.mod.util.IHasModel;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class RegistryHandler 
{
    @SubscribeEvent
    public static void onItemRegister(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(ItemInit.ITEMS.toArray(new Item[0]));
    }
    
    @SubscribeEvent
    public static void onBlockRegister(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().registerAll(BlockInit.BLOCKS.toArray(new Block[0]));
    }

    
    
    // *** DELETED THE onBiomeRegister METHOD HERE! ***
    // CFEPBiomeInit.java handles it now.

    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event)
    {
        for(Item item : ItemInit.ITEMS)
        {
            if(item instanceof IHasModel)
            {
                ((IHasModel)item).registerModels();
            }
            //ADDED ELSE BLOCK GUNS TESTING
            else {
                 ModelLoader.setCustomModelResourceLocation(item, 0, 
                     new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        }
        
        for(Block block : BlockInit.BLOCKS)
        {
            if(block instanceof IHasModel)
            {
                ((IHasModel)block).registerModels();
            }
        }
    }

    public static void preInitRegistries()
    {
        //THIS IS FOR FUTURE USE! DO NOT REMOVE STUB!
        //GameRegistry.registerWorldGenerator(new WorldGenCustomOres(), 0);
        //GameRegistry.registerWorldGenerator(new WorldGenCustomStructures(), 0);
        
        //Biomeinit.registerBiomes();
        //DimensionInit.registerDimensions();
        //GalacticraftRegistry.registerDimension("AddonDimensions.dimPlanetTwoS1", "_addonDimensions.dimplanettwos1,", -36, WorldProviderTheFracture.class, false);
    }

    public static void initRegistries()
    {
        
    }
    
    public static void postInitRegistries()
    {
        
    }
}