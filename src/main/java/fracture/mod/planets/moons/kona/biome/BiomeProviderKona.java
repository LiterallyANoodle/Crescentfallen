package fracture.mod.planets.moons.kona.biome;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import fracture.mod.init.CFplanets;
import fracture.mod.planets.moons.kona.biome.gen.GenLayerKona;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class BiomeProviderKona extends BiomeProvider {
    private GenLayer baseBiomes;
    private GenLayer zoomedBiomes;
    private BiomeCache biomeCache;
    private List<Biome> biomesToSpawnIn;
    private CelestialBody body;

    protected BiomeProviderKona() {
        this.body = CFplanets.kona;
        this.biomeCache = new BiomeCache(this);
        this.biomesToSpawnIn = new ArrayList<>();
        this.biomesToSpawnIn.add(KonaBiomes.BiomeKona);
        this.biomesToSpawnIn.add(KonaBiomes.BiomeKonaTrashHeap);
    }

    public BiomeProviderKona(long seed, WorldType type) {
        this();
        GenLayer[] layers = GenLayerKona.createWorld(seed);
        this.baseBiomes = layers[0];
        this.zoomedBiomes = layers[1];
    }

    public BiomeProviderKona(World world) {
        this(world.getSeed(), world.getWorldInfo().getTerrainType());
    }

    @Override
    public List<Biome> getBiomesToSpawnIn() {
        return this.biomesToSpawnIn;
    }

    @Override
    public Biome getBiome(BlockPos pos, Biome defaultBiome) {
        Biome b = this.biomeCache.getBiome(pos.getX(), pos.getZ(), KonaBiomes.BiomeKona);
        return b != null ? b : KonaBiomes.BiomeKona;
    }

    @Override
    public Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int height, int width) {
        IntCache.resetIntCache();

        if (biomes == null || biomes.length < width * height) {
            biomes = new Biome[width * height];
        }

        int[] biomeIds = this.baseBiomes.getInts(x, z, width, height);

        for (int i = 0; i < width * height; i++) {
            Biome b = biomeIds[i] >= 0 ? Biome.getBiome(biomeIds[i]) : null;
            biomes[i] = b != null ? b : KonaBiomes.BiomeKona; 
        }
        return biomes;
    }

    @Override
    public Biome[] getBiomes(@Nullable Biome[] reuse, int x, int z, int width, int height, boolean cacheFlag) {
        IntCache.resetIntCache();

        if (reuse == null || reuse.length < width * height) {
            reuse = new Biome[width * height];
        }

        if (cacheFlag && width == 16 && height == 16 && (x & 15) == 0 && (z & 15) == 0) {
            Biome[] cached = this.biomeCache.getCachedBiomes(x, z);
            System.arraycopy(cached, 0, reuse, 0, width * height);
            return reuse;
        }

        int[] biomeIds = this.zoomedBiomes.getInts(x, z, width, height);
        for (int i = 0; i < width * height; i++) {
            Biome b = biomeIds[i] >= 0 ? Biome.getBiome(biomeIds[i]) : null;
            reuse[i] = b != null ? b : KonaBiomes.BiomeKona; 
        }
        return reuse;
    }
}