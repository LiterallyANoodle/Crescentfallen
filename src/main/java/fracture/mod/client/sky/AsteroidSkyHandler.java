package fracture.mod.client.sky;

import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AsteroidSkyHandler {
	
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onAsteroidsLoad(WorldEvent.Load event) {
        // -30 is the Asteroids Dimension ID
        if (event.getWorld().isRemote && event.getWorld().provider.getDimension() == -30) {
            if (event.getWorld().provider instanceof IGalacticraftWorldProvider) {
            	//injector
                event.getWorld().provider.setSkyRenderer(
                    new AsteroidSkyProvider((IGalacticraftWorldProvider) event.getWorld().provider)
                );
            }
        }
    }
}