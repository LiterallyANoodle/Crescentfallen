package fracture.mod.util.handlers;

import fracture.mod.client.sky.OberonSkyProvider;
import fracture.mod.client.sky.EuropaSkyProvider;
import fracture.mod.client.sky.IoSkyProvider;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientEventHandler {

    private static final int IO_DIMENSION_ID = -1500; 
    private static final int EUROPA_DIMENSION_ID = -1501;
    private static final int OBERON_DIMENSION_ID = -1509;

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (!event.getWorld().isRemote) return;

        int dimID = event.getWorld().provider.getDimension();

        if (event.getWorld().provider instanceof IGalacticraftWorldProvider) {
            IGalacticraftWorldProvider gcProvider = (IGalacticraftWorldProvider) event.getWorld().provider;

            if (dimID == IO_DIMENSION_ID) {
                event.getWorld().provider.setSkyRenderer(new IoSkyProvider(gcProvider));
                System.out.println("[Fracture] Io Sky Provider ATTACHED.");
            } 
            else if (dimID == EUROPA_DIMENSION_ID) {
                event.getWorld().provider.setSkyRenderer(new EuropaSkyProvider(gcProvider));
                System.out.println("[Fracture] Europa Sky Provider ATTACHED.");
            } 
            else if (dimID == OBERON_DIMENSION_ID) {
                event.getWorld().provider.setSkyRenderer(new OberonSkyProvider(gcProvider));
                System.out.println("[Fracture] Oberon Sky Provider ATTACHED.");
            }
        }
    }
}