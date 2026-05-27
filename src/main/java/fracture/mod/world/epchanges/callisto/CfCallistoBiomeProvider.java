package fracture.mod.world.epchanges.callisto;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
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
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class CfCallistoBiomeProvider extends BiomeProvider {

    private static final List<Biome> CALLISTO_BIOMES = Arrays.asList(
            CFEPBiomeInit.CALLISTO_BLACK_DESERT,
            CFEPBiomeInit.CALLISTO_OIL_SHALE,
            CFEPBiomeInit.CALLISTO_ROCKIES,
            CFEPBiomeInit.CALLISTO_GRAVEL_BED,
            // Transition biomes(WIP)
            CFEPBiomeInit.CALLISTO_FOOTHILLS, 
            CFEPBiomeInit.CALLISTO_SANDS      
        );

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
            
            System.out.println("[Fracture] Callisto BiomeProvider: Injected custom layers.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Biome> getBiomesToSpawnIn() {
        return CALLISTO_BIOMES;
    }

    @Override
    public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {
        for (Biome b : allowed) {
            if (!CALLISTO_BIOMES.contains(b)) return false;
        }
        return true;
    }

    private static GenLayer[] makeCustomLayers(long seed) {
        GenLayer biomes = new GenLayerCallistoLand(1L);
        
        // Edge/Transition layer to insert Foothills
        biomes = new GenLayerCallistoEdge(1000L, biomes);

        biomes = new GenLayerFuzzyZoom(2000L, biomes);
        biomes = new GenLayerFuzzyZoom(2001L, biomes);
        
        biomes = new GenLayerZoom(1000L, biomes);
        biomes = new GenLayerSmooth(1000L, biomes);
        
        biomes = new GenLayerZoom(1001L, biomes);
        biomes = new GenLayerSmooth(1001L, biomes);
        
        biomes = new GenLayerZoom(1002L, biomes);
        biomes = new GenLayerZoom(1003L, biomes);
        biomes = new GenLayerSmooth(1002L, biomes);
        
        biomes = new GenLayerZoom(1004L, biomes);
        biomes = new GenLayerZoom(1005L, biomes);
        biomes = new GenLayerSmooth(1003L, biomes);

        GenLayer voronoi = new GenLayerVoronoiZoom(1L, biomes);
        biomes.initWorldGenSeed(seed);
        voronoi.initWorldGenSeed(seed);
        return new GenLayer[] { biomes, voronoi };
    }


     //Edge Layer to insert Foothills between Rockies and lower biomes(WIP)

    static class GenLayerCallistoEdge extends GenLayer {
        public GenLayerCallistoEdge(long seed, GenLayer parent) {
            super(seed);
            this.parent = parent;
        }

        @Override
        public int[] getInts(int x, int z, int width, int height) {
            int[] parentInts = this.parent.getInts(x - 1, z - 1, width + 2, height + 2);
            int[] dest = IntCache.getIntCache(width * height);

            int rockies = Biome.getIdForBiome(CFEPBiomeInit.CALLISTO_ROCKIES);
            int foothills = Biome.getIdForBiome(CFEPBiomeInit.CALLISTO_FOOTHILLS);

            for (int dz = 0; dz < height; ++dz) {
                for (int dx = 0; dx < width; ++dx) {
                    this.initChunkSeed(x + dx, z + dz);
                    int center = parentInts[dx + 1 + (dz + 1) * (width + 2)];

                    if (center == rockies) {
                        int up = parentInts[dx + 1 + dz * (width + 2)];
                        int down = parentInts[dx + 1 + (dz + 2) * (width + 2)];
                        int left = parentInts[dx + (dz + 1) * (width + 2)];
                        int right = parentInts[dx + 2 + (dz + 1) * (width + 2)];

                        // If any neighbor is NOT rockies, turn this edge into Foothills
                        if (up != rockies || down != rockies || left != rockies || right != rockies) {
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

    static class GenLayerCallistoLand extends GenLayer {
        public GenLayerCallistoLand(long seed) { super(seed); }
        @Override
        public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
            int[] dest = IntCache.getIntCache(areaWidth * areaHeight);
            int oilShale = Biome.getIdForBiome(CFEPBiomeInit.CALLISTO_OIL_SHALE);
            int blackDesert = Biome.getIdForBiome(CFEPBiomeInit.CALLISTO_BLACK_DESERT);
            int rockies = Biome.getIdForBiome(CFEPBiomeInit.CALLISTO_ROCKIES);
            int gravel = Biome.getIdForBiome(CFEPBiomeInit.CALLISTO_GRAVEL_BED);

            for (int dz = 0; dz < areaHeight; ++dz) {
                for (int dx = 0; dx < areaWidth; ++dx) {
                    this.initChunkSeed((long)(areaX + dx), (long)(areaY + dz));
                    int r = this.nextInt(30); 
                    
                    int biomeID;
                    if (r == 0) biomeID = rockies;
                    else if (r == 1) biomeID = gravel;
                    else if (r <= 10) biomeID = blackDesert;
                    else biomeID = oilShale;

                    dest[dx + dz * areaWidth] = biomeID;
                }
            }
            return dest;
        }
    }
}