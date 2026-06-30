package fracture.mod.client.sky;

import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CFSkyProviderRegistry {

    // --- DIMENSION IDs ---
    private static final int IO_DIM_ID = -1500;
    private static final int EUROPA_DIM_ID = -1501;
    private static final int CALLISTO_DIM_ID = -1505;
    private static final int GANYMEDE_DIM_ID = -1506;
    private static final int TITAN_DIM_ID = -1508;
    private static final int OBERON_DIM_ID = -1509;
    private static final int ASTEROIDS_DIM_ID = -30; 
    private static final int OVERWORLD_DIM_ID = 0; 

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (!event.getWorld().isRemote) return;

        int dimId = event.getWorld().provider.getDimension();
        net.minecraft.world.WorldProvider provider = event.getWorld().provider;

        // EP overrides
        if (provider instanceof IGalacticraftWorldProvider) {
            IGalacticraftWorldProvider gcProvider = (IGalacticraftWorldProvider) provider;

            if (dimId == IO_DIM_ID) provider.setSkyRenderer(new IoSkyProvider(gcProvider));
            else if (dimId == EUROPA_DIM_ID) provider.setSkyRenderer(new EuropaSkyProvider(gcProvider));
            else if (dimId == CALLISTO_DIM_ID) provider.setSkyRenderer(new CallistoSkyProvider(gcProvider));
            else if (dimId == TITAN_DIM_ID) provider.setSkyRenderer(new TitanSkyProvider(gcProvider));
            else if (dimId == OBERON_DIM_ID) provider.setSkyRenderer(new OberonSkyProvider(gcProvider));
            else if (dimId == ASTEROIDS_DIM_ID) provider.setSkyRenderer(new AsteroidSkyProvider(gcProvider));
        }
        // These do not require the gcProvider instance
        if (dimId == GANYMEDE_DIM_ID) {
            provider.setSkyRenderer(new GanymedeSkyProvider());
        } 
        else if (dimId == OVERWORLD_DIM_ID) {
            provider.setSkyRenderer(new DestructionSkyProvider());
        } 

    }
}
