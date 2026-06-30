package fracture.mod.init;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

	//Note: Remake as CFsounds

@Mod.EventBusSubscriber(modid = "fracture")
public class CFSounds {
    
    public static final SoundEvent CF_EXPLODE = new SoundEvent(new ResourceLocation("fracture", "cfexplode")).setRegistryName("cfexplode");

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().register(CF_EXPLODE);
    }
}