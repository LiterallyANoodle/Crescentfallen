package fracture.mod.planets.moons.kona.biome.gen;

import fracture.mod.planets.moons.kona.biome.KonaBiomes;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class GenLayerKonaBiomes extends GenLayer {

    // 60% Konatrashheap, 40% Kona
    private static final Biome[] weightedBiomes = {
        KonaBiomes.BiomeKonaTrashHeap, 
        KonaBiomes.BiomeKonaTrashHeap, 
        KonaBiomes.BiomeKonaTrashHeap, 
        KonaBiomes.BiomeKona, 
        KonaBiomes.BiomeKona
    };

    public GenLayerKonaBiomes(long l) {
        super(l);
    }

    @Override
    public int[] getInts(int x, int z, int width, int depth) {
        int[] dest = IntCache.getIntCache(width * depth);

        for (int dz = 0; dz < depth; ++dz) {
            for (int dx = 0; dx < width; ++dx) {
                initChunkSeed(x + dx, z + dz);
                Biome chosen = weightedBiomes[nextInt(weightedBiomes.length)];
                dest[dx + dz * width] = Biome.getIdForBiome(chosen);
            }
        }
        return dest;
    }
}