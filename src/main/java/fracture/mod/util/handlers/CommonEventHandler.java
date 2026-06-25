package fracture.mod.util.handlers;

import fracture.mod.world.epchanges.CfBiomeWrapper;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class CommonEventHandler {

    private static final int EUROPA_DIMENSION_ID = -1501;

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        
        int dimID = event.getWorld().provider.getDimension();

        if (dimID == EUROPA_DIMENSION_ID) {
            if (event.getWorld().provider instanceof IGalacticraftWorldProvider) {
                
                // Check if already wrapped
                if (!(event.getWorld().provider.getBiomeProvider() instanceof CfBiomeWrapper)) {
                    
                    // Parse world seed
                    long seed = event.getWorld().getSeed();
                    
                    CfBiomeWrapper newWrapper = new CfBiomeWrapper(event.getWorld().provider.getBiomeProvider(), seed);
                    
                    ReflectionHelper.setPrivateValue(WorldProvider.class, event.getWorld().provider, newWrapper, "biomeProvider", "field_76578_c");
                    
                    System.out.println("[Fracture] Europa Biomes OVERRIDDEN (Europa salt sea) on side: " + (event.getWorld().isRemote ? "CLIENT" : "SERVER"));
                }
            }
        }
    }
}