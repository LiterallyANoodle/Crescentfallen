package fracture.mod.init;

import fracture.mod.planets.moons.kona.biome.KonaBiomes;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class CFBiomes {

	//Note: this is a test class to try and fix a bug with kona. do it less bad in the future.
	
    @SubscribeEvent
    public static void registerBiomes(RegistryEvent.Register<Biome> event) {
        event.getRegistry().register(KonaBiomes.BiomeKona);
        event.getRegistry().register(KonaBiomes.BiomeKonaTrashHeap);
        
        System.out.println("Kona Biomes registered");
    }
}