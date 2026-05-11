package fracture.mod.world.epchanges.callisto;

import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Field;

public class CfCallistoBiomeAddon {

    private static final int CALLISTO_DIM_ID = -1505;

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld();
        
        if (world.isRemote || world.provider.getDimension() != CALLISTO_DIM_ID) {
            return;
        }

        try {
            Field providerField = ReflectionHelper.findField(WorldProvider.class, "biomeProvider", "field_76578_c");
            CfCallistoBiomeProvider customProvider = new CfCallistoBiomeProvider(world);
            providerField.set(world.provider, customProvider);
            System.out.println("[Fracture] Successfully isolated Callisto BiomeProvider.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}