package fracture.mod.world.epchanges.callisto;

import java.util.Arrays;
import java.util.List;
import java.lang.reflect.Field;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerSmooth;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;
import net.minecraft.world.gen.layer.IntCache;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class CfCallistoBiomeProvider extends BiomeProvider {

    private static final List<Biome> CALLISTO_BIOMES = Arrays.asList(
            CFEPBiomeInit.CALLISTO_BLACK_DESERT,
            CFEPBiomeInit.CALLISTO_OIL_SHALE,
            CFEPBiomeInit.CALLISTO_ROCKIES,
            CFEPBiomeInit.CALLISTO_GRAVEL_BED,
            CFEPBiomeInit.CALLISTO_FOOTHILLS, 
            CFEPBiomeInit.CALLISTO_SANDS      
        );

    // Pseudo-IDs to avoid Forge registry conflicts during generation
    private static final int MASSIVE_SHALE_ID = 10200;
    private static final int LARGE_SHALE_ID = 10201;
    private static final int LARGE_ROCKIES_ID = 10202;

    public CfCallistoBiomeProvider(World world) {
        super(world.getWorldInfo()); 
        GenLayer[] layers = makeCustomLayers(world.getSeed());
        
        try {
            Field genBiomesField = ReflectionHelper.findField(BiomeProvider.class, "genBiomes", "field_76944_d");
            Field biomeIndexField = ReflectionHelper.findField(BiomeProvider.class, "biomeIndexLayer", "field_76945_e");
            
            genBiomesField.setAccessible(true);
            biomeIndexField.setAccessible(true);
            
            genBiomesField.set(this, layers[0]);
            biomeIndexField.set(this, layers[1]);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Biome> getBiomesToSpawnIn() { return CALLISTO_BIOMES; }

    @Override
    public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {
        for (Biome b : allowed) {
            if (!CALLISTO_BIOMES.contains(b)) return false;
        }
        return true;
    }

    private static GenLayer[] makeCustomLayers(long seed) {
        GenLayer biomes = new GenLayerCallistoLand(1L);
        
        biomes = new GenLayerCallistoGrow(1000L, biomes);
        biomes = new GenLayerCallistoGrow(1001L, biomes);

        biomes = new GenLayerFuzzyZoom(2000L, biomes);
        biomes = new GenLayerZoom(2001L, biomes);

        biomes = new GenLayerCallistoSubBiomes(1002L, biomes);

        biomes = new GenLayerZoom(2002L, biomes);

        biomes = new GenLayerCallistoEdge(1000L, biomes);

        biomes = new GenLayerCallistoResolve(1003L, biomes);


        GenLayer canyons = new GenLayerCallistoCanyonsInit(100L);
        canyons = new GenLayerZoom(1000L, canyons);
        canyons = new GenLayerZoom(1001L, canyons);
        canyons = new GenLayerZoom(1002L, canyons); 
        canyons = new GenLayerCallistoCanyons(1003L, canyons); 

        biomes = new GenLayerCallistoCanyonMix(100L, biomes, canyons);

        biomes = new GenLayerZoom(2003L, biomes);
        biomes = new GenLayerZoom(2004L, biomes);
        biomes = new GenLayerZoom(2005L, biomes);
        
        biomes = new GenLayerSmooth(1002L, biomes);
        GenLayer voronoi = new GenLayerVoronoiZoom(10L, biomes);

        biomes.initWorldGenSeed(seed);
        voronoi.initWorldGenSeed(seed);
        return new GenLayer[] { biomes, voronoi };
    }


    static class GenLayerCallistoLand extends GenLayer {
        public GenLayerCallistoLand(long seed) { super(seed); }
        @Override
        public int[] getInts(int x, int z, int width, int height) {
            int[] dest = IntCache.getIntCache(width * height);
            int oilShale = Biome.getIdForBiome(CFEPBiomeInit.CALLISTO_OIL_SHALE);
            int blackDesert = Biome.getIdForBiome(CFEPBiomeInit.CALLISTO_BLACK_DESERT);
            int rockies = Biome.getIdForBiome(CFEPBiomeInit.CALLISTO_ROCKIES);

            for (int dz = 0; dz < height; ++dz) {
                for (int dx = 0; dx < width; ++dx) {
                    this.initChunkSeed(x + dx, z + dz);
                    int r = this.nextInt(100); 
                    
                    if (r < 5) dest[dx + dz * width] = LARGE_ROCKIES_ID;       // 5% Large Rockies
                    else if (r < 15) dest[dx + dz * width] = rockies;          // 10% Rockies
                    else if (r < 20) dest[dx + dz * width] = MASSIVE_SHALE_ID; // 5% Large Shale
                    else if (r < 35) dest[dx + dz * width] = LARGE_SHALE_ID;   // 15% Large Shale
                    else if (r < 60) dest[dx + dz * width] = oilShale;         // 25% Shale
                    else dest[dx + dz * width] = blackDesert;                  // 40% Black Desert
                }
            }
            return dest;
        }
    }

    // Forces large designated biomes to aggressively overwrite their neighbors
    static class GenLayerCallistoGrow extends GenLayer {
        public GenLayerCallistoGrow(long seed, GenLayer parent) { super(seed); this.parent = parent; }
        @Override
        public int[] getInts(int x, int z, int width, int height) {
            int[] parentInts = this.parent.getInts(x - 1, z - 1, width + 2, height + 2);
            int[] dest = IntCache.getIntCache(width * height);
            for (int dz = 0; dz < height; ++dz) {
                for (int dx = 0; dx < width; ++dx) {
                    this.initChunkSeed(x + dx, z + dz);
                    int center = parentInts[dx + 1 + (dz + 1) * (width + 2)];
                    int up = parentInts[dx + 1 + dz * (width + 2)];
                    int down = parentInts[dx + 1 + (dz + 2) * (width + 2)];
                    int left = parentInts[dx + (dz + 1) * (width + 2)];
                    int right = parentInts[dx + 2 + (dz + 1) * (width + 2)];

                    dest[dx + dz * width] = center;
                    
                    //add spread chance (z66%)
                    if (center != MASSIVE_SHALE_ID && (up == MASSIVE_SHALE_ID || down == MASSIVE_SHALE_ID || left == MASSIVE_SHALE_ID || right == MASSIVE_SHALE_ID)) {
                        if (this.nextInt(3) != 0) dest[dx + dz * width] = MASSIVE_SHALE_ID; 
                    }
                    else if (center != LARGE_SHALE_ID && center != LARGE_ROCKIES_ID) {
                         if ((up == LARGE_SHALE_ID || down == LARGE_SHALE_ID || left == LARGE_SHALE_ID || right == LARGE_SHALE_ID) && this.nextInt(2) == 0) dest[dx + dz * width] = LARGE_SHALE_ID;
                         else if ((up == LARGE_ROCKIES_ID || down == LARGE_ROCKIES_ID || left == LARGE_ROCKIES_ID || right == LARGE_ROCKIES_ID) && this.nextInt(2) == 0) dest[dx + dz * width] = LARGE_ROCKIES_ID;
                    }
                }
            }
            return dest;
        }
    }

    // Injects Black Desert inside shale areas
    static class GenLayerCallistoSubBiomes extends GenLayer {
        public GenLayerCallistoSubBiomes(long seed, GenLayer parent) { super(seed); this.parent = parent; }
        @Override
        public int[] getInts(int x, int z, int width, int height) {
            int[] parentInts = this.parent.getInts(x - 1, z - 1, width + 2, height + 2);
            int[] dest = IntCache.getIntCache(width * height);
            int blackDesert = Biome.getIdForBiome(CFEPBiomeInit.CALLISTO_BLACK_DESERT);
            
            for (int dz = 0; dz < height; ++dz) {
                for (int dx = 0; dx < width; ++dx) {
                    this.initChunkSeed(x + dx, z + dz);
                    int center = parentInts[dx + 1 + (dz + 1) * (width + 2)];
                    int up = parentInts[dx + 1 + dz * (width + 2)];
                    int down = parentInts[dx + 1 + (dz + 2) * (width + 2)];
                    int left = parentInts[dx + (dz + 1) * (width + 2)];
                    int right = parentInts[dx + 2 + (dz + 1) * (width + 2)];
                    
                    if (center == MASSIVE_SHALE_ID && up == MASSIVE_SHALE_ID && down == MASSIVE_SHALE_ID && left == MASSIVE_SHALE_ID && right == MASSIVE_SHALE_ID) {
                        if (this.nextInt(10) == 0) dest[dx + dz * width] = blackDesert;
                        else dest[dx + dz * width] = center;
                    } else {
                        dest[dx + dz * width] = center;
                    }
                }
            }
            return dest;
        }
    }

    // Wrap Foothills around both normal and large Rockies
    static class GenLayerCallistoEdge extends GenLayer {
        public GenLayerCallistoEdge(long seed, GenLayer parent) { super(seed); this.parent = parent; }
        @Override
        public int[] getInts(int x, int z, int width, int height) {
            int[] parentInts = this.parent.getInts(x - 1, z - 1, width + 2, height + 2);
            int[] dest = IntCache.getIntCache(width * height);
            int rockies = Biome.getIdForBiome(CFEPBiomeInit.CALLISTO_ROCKIES);
            int foothills = Biome.getIdForBiome(CFEPBiomeInit.CALLISTO_FOOTHILLS);

            for (int dz = 0; dz < height; ++dz) {
                for (int dx = 0; dx < width; ++dx) {
                    int center = parentInts[dx + 1 + (dz + 1) * (width + 2)];
                    if (center == rockies || center == LARGE_ROCKIES_ID) {
                        int up = parentInts[dx + 1 + dz * (width + 2)];
                        int down = parentInts[dx + 1 + (dz + 2) * (width + 2)];
                        int left = parentInts[dx + (dz + 1) * (width + 2)];
                        int right = parentInts[dx + 2 + (dz + 1) * (width + 2)];

                        if ((up != rockies && up != LARGE_ROCKIES_ID) || 
                            (down != rockies && down != LARGE_ROCKIES_ID) || 
                            (left != rockies && left != LARGE_ROCKIES_ID) || 
                            (right != rockies && right != LARGE_ROCKIES_ID)) {
                            dest[dx + dz * width] = foothills;
                        } else {
                            dest[dx + dz * width] = center;
                        }
                    } else {
                        dest[dx + dz * width] = center;
                    }
                }
            }
            return dest;
        }
    }

    // Converts Pseudo-IDs back to legitimate biomes
    static class GenLayerCallistoResolve extends GenLayer {
        public GenLayerCallistoResolve(long seed, GenLayer parent) { super(seed); this.parent = parent; }
        @Override
        public int[] getInts(int x, int z, int width, int height) {
            int[] parentInts = this.parent.getInts(x, z, width, height);
            int[] dest = IntCache.getIntCache(width * height);
            int oilShale = Biome.getIdForBiome(CFEPBiomeInit.CALLISTO_OIL_SHALE);
            int rockies = Biome.getIdForBiome(CFEPBiomeInit.CALLISTO_ROCKIES);

            for (int i = 0; i < width * height; ++i) {
                int id = parentInts[i];
                if (id == MASSIVE_SHALE_ID || id == LARGE_SHALE_ID) dest[i] = oilShale;
                else if (id == LARGE_ROCKIES_ID) dest[i] = rockies;
                else dest[i] = id;
            }
            return dest;
        }
    }

    // apl canyon noise
    static class GenLayerCallistoCanyonsInit extends GenLayer {
        public GenLayerCallistoCanyonsInit(long seed) { super(seed); }
        @Override
        public int[] getInts(int x, int z, int width, int height) {
            int[] dest = IntCache.getIntCache(width * height);
            for (int dz = 0; dz < height; ++dz) {
                for (int dx = 0; dx < width; ++dx) {
                    this.initChunkSeed(x + dx, z + dz);
                    dest[dx + dz * width] = this.nextInt(2) + 2; 
                }
            }
            return dest;
        }
    }

    // detect edges to draw canyons
    static class GenLayerCallistoCanyons extends GenLayer {
        public GenLayerCallistoCanyons(long seed, GenLayer parent) { super(seed); this.parent = parent; }
        @Override
        public int[] getInts(int x, int z, int width, int height) {
            int[] parentInts = this.parent.getInts(x - 1, z - 1, width + 2, height + 2);
            int[] dest = IntCache.getIntCache(width * height);
            int gravelBed = Biome.getIdForBiome(CFEPBiomeInit.CALLISTO_GRAVEL_BED);

            for (int dz = 0; dz < height; ++dz) {
                for (int dx = 0; dx < width; ++dx) {
                    int center = parentInts[dx + 1 + (dz + 1) * (width + 2)];
                    int up = parentInts[dx + 1 + dz * (width + 2)];
                    int down = parentInts[dx + 1 + (dz + 2) * (width + 2)];
                    int left = parentInts[dx + (dz + 1) * (width + 2)];
                    int right = parentInts[dx + 2 + (dz + 1) * (width + 2)];

                    if (center == up && center == down && center == left && center == right) {
                        dest[dx + dz * width] = -1;
                    } else {
                        dest[dx + dz * width] = gravelBed; 
                    }
                }
            }
            return dest;
        }
    }

    // Overlay canyons onto the landmasses
    static class GenLayerCallistoCanyonMix extends GenLayer {
        private final GenLayer canyonLayer;
        public GenLayerCallistoCanyonMix(long seed, GenLayer biomeLayer, GenLayer canyonLayer) {
            super(seed);
            this.parent = biomeLayer;
            this.canyonLayer = canyonLayer;
        }
        @Override
        public int[] getInts(int x, int z, int width, int height) {
            int[] biomes = this.parent.getInts(x, z, width, height);
            int[] canyons = this.canyonLayer.getInts(x, z, width, height);
            int[] dest = IntCache.getIntCache(width * height);

            for (int i = 0; i < width * height; ++i) {
                if (canyons[i] > 0) dest[i] = canyons[i];
                else dest[i] = biomes[i];
            }
            return dest;
        }
    }
}