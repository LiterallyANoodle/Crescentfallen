package fracture.mod.planets.moons.kona;

import java.util.List;
import java.util.Random;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import fracture.mod.init.BlockInit;
import fracture.mod.planets.moons.kona.biome.BiomeDecoratorKona;
import fracture.mod.planets.moons.kona.biome.KonaBiomes;
import fracture.mod.planets.moons.kona.biome.gen.MapGenKonaTrashCaves;
import fracture.mod.planets.moons.kona.biome.gen.MapGenTrashDunes;
import fracture.mod.world.MapGenAddonCaveGen;
import fracture.mod.world.MapGenAddonRavinGen;
import fracture.mod.world.chunk.ChunkProviderKonaBase;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.MapGenBaseMeta;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;

public class ChunkProviderKona extends ChunkProviderKonaBase {

    private final BiomeDecoratorKona decorator = new BiomeDecoratorKona();
    private final MapGenTrashDunes duneGenerator;
    private final MapGenKonaTrashCaves trashCaveGenerator;
    
    private final MapGenAddonRavinGen ravineGenerator = new MapGenAddonRavinGen();
    private final MapGenAddonCaveGen caveGenerator = new MapGenAddonCaveGen(
            BlockInit.SURFACE_KONA.getDefaultState(), Blocks.LAVA.getDefaultState(),
            Sets.newHashSet(BlockInit.SURFACE_KONA, BlockInit.STONE_KONA));

    public ChunkProviderKona(World par1World, long seed, boolean mapFeaturesEnabled) {
        super(par1World, seed, mapFeaturesEnabled);
        this.duneGenerator = new MapGenTrashDunes(seed);
        this.trashCaveGenerator = new MapGenKonaTrashCaves();
    }
    
    @Override
    protected void buildSurfaceStructures(int x, int z, ChunkPrimer primer) {
        this.duneGenerator.generate(this.world, x, z, primer);
        this.trashCaveGenerator.generate(this.world, x, z, primer);
        this.generateCraters(x, z, primer);
    }

    private void generateCraters(int chunkX, int chunkZ, ChunkPrimer primer) {
        int craterRange = 2; 
        for (int cX = chunkX - craterRange; cX <= chunkX + craterRange; cX++) {
            for (int cZ = chunkZ - craterRange; cZ <= chunkZ + craterRange; cZ++) {
                Random rand = new Random(this.world.getSeed() ^ (cX * 341873128712L) ^ (cZ * 132897987541L));
                
                if (rand.nextInt(Math.max(5, this.getCraterProbability() / 10)) == 0) { 
                    int cx = rand.nextInt(16);
                    int cz = rand.nextInt(16);
                    int radius = 8 + rand.nextInt(12); 
                    
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            int realX = (chunkX * 16) + x;
                            int realZ = (chunkZ * 16) + z;
                            int craterX = (cX * 16) + cx;
                            int craterZ = (cZ * 16) + cz;

                            double dist = Math.sqrt(Math.pow(realX - craterX, 2) + Math.pow(realZ - craterZ, 2));
                            
                            if (dist <= radius) {
                                Biome biome = this.world.getBiomeProvider().getBiome(new BlockPos(realX, 0, realZ));
                                
                                if (biome == KonaBiomes.BiomeKona) {
                                    
                                    int surfaceY = 255;
                                    while (surfaceY > 0 && primer.getBlockState(x, surfaceY, z).getBlock() == Blocks.AIR) {
                                        surfaceY--;
                                    }
                                    
                                    if (surfaceY > 10) {
                                        double depthRatio = 1.0D - (dist / radius);
                                        int carveDepth = (int) (Math.sin(depthRatio * Math.PI / 2.0D) * (radius / 2.0D));

                                        if (carveDepth > 0) {
                                            for (int y = 0; y < carveDepth; y++) {
                                                primer.setBlockState(x, surfaceY - y, z, Blocks.AIR.getDefaultState());
                                            }
                                            primer.setBlockState(x, surfaceY - carveDepth, z, biome.topBlock);
                                            primer.setBlockState(x, surfaceY - carveDepth - 1, z, biome.fillerBlock);
                                            primer.setBlockState(x, surfaceY - carveDepth - 2, z, biome.fillerBlock);
                                        } 
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected List<MapGenBaseMeta> getWorldGenerators() {
        List<MapGenBaseMeta> generators = Lists.newArrayList();
        generators.add(this.caveGenerator);
        return generators;
    }

    @Override
    public int getCraterProbability() {
        return 500; 
    }

    @Override
    public void onChunkProvide(int cX, int cZ, ChunkPrimer primer) {
        this.ravineGenerator.generate(this.world, cX, cZ, primer);
    }

    @Override
    public void onPopulate(int cX, int cZ) { }

    @Override
    public void recreateStructures(Chunk chunk, int x, int z) { }

    @Override
    protected void decoratePlanet(World world, Random rand, int x, int z) {
        this.decorator.decorate(world, rand, x, z);
    }
}