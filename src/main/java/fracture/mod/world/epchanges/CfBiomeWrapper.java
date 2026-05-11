package fracture.mod.world.epchanges;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import java.util.List;
import java.util.Random;

public class CfBiomeWrapper extends BiomeProvider {
    
    //Used for Europa at the moment
    
    private final BiomeProvider original;
    private final long seed;
    private Biome saltSeaBiome;

    // Settings
    // Zoom 5 = 32x larger biomes (changed from 4)
    private final int zoomFactor = 5; 
    // Ocean distribution (Controls size of seas vs land)
    private final double noiseScale = 0.015; 
    //Ocean map concentration distribution(changed from 15)
    private final double oceanThreshold = -0.2; 
    // WIP

    public CfBiomeWrapper(BiomeProvider original, long seed) {
        this.original = original;
        this.seed = seed;
        
        // Attempt to find a specific Salt Sea biome
        this.saltSeaBiome = ForgeRegistries.BIOMES.getValue(new ResourceLocation("extraplanets", "europa_salt_sea"));
        
        if (this.saltSeaBiome == null) {
            this.saltSeaBiome = ForgeRegistries.BIOMES.getValue(new ResourceLocation("extraplanets", "europa_ice_valleys"));
        }
        // Fallback
        if (this.saltSeaBiome == null) {
            this.saltSeaBiome = ForgeRegistries.BIOMES.getValue(new ResourceLocation("extraplanets", "europa"));
        }

        // If all registry string lookups fail, we extract a guaranteed valid Europa biome 
        // directly from the original ExtraPlanets provider. This ensures saltSeaBiome is NEVER null.
        if (this.saltSeaBiome == null && original != null) {
            this.saltSeaBiome = original.getBiome(new BlockPos(0, 0, 0));
        }
    }

    private boolean isOcean(int x, int z) {
        double d1 = Math.sin(x * noiseScale) + 0.5 * Math.cos(z * noiseScale * 1.3);
        double d2 = Math.cos(x * noiseScale * 0.7) + 0.5 * Math.sin(z * noiseScale);
        return ((d1 + d2) / 3.0) > oceanThreshold;
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        if (isOcean(pos.getX(), pos.getZ())) return saltSeaBiome;
        return original.getBiome(new BlockPos(pos.getX() >> zoomFactor, pos.getY(), pos.getZ() >> zoomFactor));
    }

    @Override
    public Biome[] getBiomes(Biome[] listToReuse, int x, int z, int width, int height, boolean cacheFlag) {
        if (listToReuse == null || listToReuse.length < width * height) listToReuse = new Biome[width * height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int realX = x + i;
                int realZ = z + j;
                if (isOcean(realX, realZ)) {
                    listToReuse[i + j * width] = saltSeaBiome;
                } else {
                    listToReuse[i + j * width] = original.getBiome(new BlockPos(realX >> zoomFactor, 0, realZ >> zoomFactor));
                }
            }
        }
        return listToReuse;
    }

    @Override
    public Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int width, int height) {
        if (biomes == null || biomes.length < width * height) biomes = new Biome[width * height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int realX = x + i;
                int realZ = z + j;
                if (isOcean(realX, realZ)) {
                    biomes[i + j * width] = saltSeaBiome;
                } else {
                    biomes[i + j * width] = original.getBiome(new BlockPos(realX >> zoomFactor, 0, realZ >> zoomFactor));
                }
            }
        }
        return biomes;
    }
    
    @Override
    public List<Biome> getBiomesToSpawnIn() { return original.getBiomesToSpawnIn(); }
    @Override
    public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) { return original.areBiomesViable(x >> zoomFactor, z >> zoomFactor, radius, allowed); }
    @Override
    public BlockPos findBiomePosition(int x, int z, int range, List<Biome> biomes, Random random) { return original.findBiomePosition(x >> zoomFactor, z >> zoomFactor, range, biomes, random); }
}