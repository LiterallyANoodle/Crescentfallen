package fracture.mod.world.epchanges.callisto;

import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.world.BiomeGenBaseGC;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class CFEPBiomeInit {

    public static final Biome CALLISTO_BLACK_DESERT = new BiomeCallistoBlackDesert();
    public static final Biome CALLISTO_OIL_SHALE = new BiomeCallistoOilShale();
    public static final Biome CALLISTO_GRAVEL_BED = new BiomeCallistoGravelBed();
    public static final Biome CALLISTO_ROCKIES = new BiomeCallistoRockies(); 
    public static final Biome CALLISTO_FOOTHILLS = new BiomeCallistoFoothills();
    public static final Biome CALLISTO_SANDS = new BiomeCallistoSands();
    
    public static final BiomeDictionary.Type CALLISTO_TYPE = BiomeDictionary.Type.getType("CALLISTO");

    @SubscribeEvent
    public static void registerBiomes(RegistryEvent.Register<Biome> event) {
        initCallistoBiome(event, CALLISTO_BLACK_DESERT, "callisto_black_desert");
        initCallistoBiome(event, CALLISTO_OIL_SHALE, "callisto_oil_shale");
        initCallistoBiome(event, CALLISTO_GRAVEL_BED, "callisto_gravel_bed");
        initCallistoBiome(event, CALLISTO_ROCKIES, "callisto_rockies"); 
        
        initCallistoBiome(event, CALLISTO_FOOTHILLS, "callisto_foothills");
        initCallistoBiome(event, CALLISTO_SANDS, "callisto_sands");
    }

    private static void initCallistoBiome(RegistryEvent.Register<Biome> event, Biome biome, String name) {
        if (biome.getRegistryName() == null) {
            biome.setRegistryName("fracture", name);
        }
        event.getRegistry().register(biome);
        BiomeDictionary.addTypes(biome, CALLISTO_TYPE);
    }

    public static void adaptBiomes() {
        CelestialBody callisto = GalaxyRegistry.getRegisteredMoons().get("callisto");
        if (callisto != null) {
            callisto.biomesToAdapt = new BiomeGenBaseGC[] { 
                (BiomeGenBaseGC) CALLISTO_BLACK_DESERT, 
                (BiomeGenBaseGC) CALLISTO_OIL_SHALE, 
                (BiomeGenBaseGC) CALLISTO_ROCKIES, 
                (BiomeGenBaseGC) CALLISTO_GRAVEL_BED 
            };
        }
    }
}