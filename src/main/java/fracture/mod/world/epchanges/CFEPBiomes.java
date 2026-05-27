//package fracture.mod.world.epchanges;
//
//import fracture.mod.world.epchanges.callisto.*;
//import micdoodle8.mods.galacticraft.api.prefab.world.gen.BiomeAdaptive;
//import micdoodle8.mods.galacticraft.api.world.BiomeGenBaseGC;
//import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
//import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry; // Correct Import
//import net.minecraft.world.biome.Biome;
//import net.minecraftforge.registries.IForgeRegistry;
//
//
// Depreciated 
//
//public class CFEPBiomes {
//
//    public static final BiomeCallistoBlackDesert BLACK_DESERT = new BiomeCallistoBlackDesert();
//    public static final BiomeCallistoOilShale OIL_SHALE = new BiomeCallistoOilShale();
//    public static final BiomeCallistoRockies ROCKIES = new BiomeCallistoRockies();
//    public static final BiomeCallistoGravelBed GRAVEL_BED = new BiomeCallistoGravelBed();
//
//    public static void registerBiomes(IForgeRegistry<Biome> registry) {
//        registry.register(BLACK_DESERT.setRegistryName("callisto_black_desert"));
//        registry.register(OIL_SHALE.setRegistryName("callisto_oil_shale"));
//        registry.register(ROCKIES.setRegistryName("callisto_rockies"));
//        registry.register(GRAVEL_BED.setRegistryName("callisto_gravel_bed"));
//    }
//
//    public static void adaptBiomes() {
//        // Register slots in the Adaptive system
//        BiomeAdaptive.register(0, BLACK_DESERT);
//        BiomeAdaptive.register(1, OIL_SHALE);
//        BiomeAdaptive.register(2, ROCKIES);
//        BiomeAdaptive.register(3, GRAVEL_BED);
//
//        // Link to Callisto
//        CelestialBody callisto = GalaxyRegistry.getRegisteredMoons().get("callisto");
//        if (callisto != null) {
//            callisto.biomesToAdapt = new BiomeGenBaseGC[] { BLACK_DESERT, OIL_SHALE, ROCKIES, GRAVEL_BED };
//        }
//    }
//}