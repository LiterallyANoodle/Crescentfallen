package fracture.mod.planets.moons.kona.biome;

import java.util.Random;

import fracture.mod.init.BlockInit;
import fracture.mod.planets.moons.kona.biome.gen.BiomeKona;
import fracture.mod.planets.moons.kona.biome.gen.BiomeKonaTrashHeap;
import micdoodle8.mods.galacticraft.api.world.BiomeGenBaseGC;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

public class KonaBiomes extends BiomeGenBaseGC {

    public static final Biome BiomeKona = new BiomeKona(new BiomeProperties("Kona").setBaseHeight(0.125F).setHeightVariation(0.015F).setRainfall(0.0F).setRainDisabled());
    public static final Biome BiomeKonaTrashHeap = new BiomeKonaTrashHeap(new BiomeProperties("Kona Trash Heap").setBaseHeight(0.05F).setHeightVariation(0.1F).setRainfall(0.0F).setRainDisabled());

    public static final Biome[] biomes = {BiomeKona, BiomeKonaTrashHeap};

    protected KonaBiomes(BiomeProperties properties) {
        super(properties, true);
    }

    @Override
    public void genTerrainBlocks(World world, Random rand, ChunkPrimer chunk, int x, int z, double stoneNoise) {
        generateBiomeTerrain(rand, chunk, x, z, stoneNoise);
    }

    public final void generateBiomeTerrain(Random rand, ChunkPrimer chunk, int x, int z, double stoneNoise) {
        IBlockState iblockstate = this.topBlock;
        IBlockState iblockstate1 = this.fillerBlock;
        int j = -1;
        int k = (int) (stoneNoise / 3.0D + 3.0D + rand.nextDouble() * 5.25D);
        int l = x & 15;
        int i1 = z & 15;

        for (int j1 = 255; j1 >= 0; --j1) {
            if (j1 <= rand.nextInt(5)) {
                chunk.setBlockState(i1, j1, l, Blocks.BEDROCK.getDefaultState());
            } else {
                IBlockState iblockstate2 = chunk.getBlockState(i1, j1, l);
                if (iblockstate2.getMaterial() == Material.AIR) {
                    j = -1;
                } else if (iblockstate2.getBlock() == Blocks.STONE.getDefaultState().getBlock()) { 
                    if (j == -1) {
                        if (k <= 0) {
                            iblockstate = null;
                            iblockstate1 = BlockInit.STONE_KONA.getDefaultState();
                        } else if (j1 >= 63 - 4 && j1 <= 63 + 1) {
                            iblockstate = this.topBlock;
                            iblockstate1 = this.fillerBlock;
                        }

                        j = k;

                        if (j1 >= 63 - 1) {
                            chunk.setBlockState(i1, j1, l, iblockstate);
                        } else if (j1 < 63 - 7 - k) {
                            iblockstate = null;
                            iblockstate1 = BlockInit.STONE_KONA.getDefaultState();
                            chunk.setBlockState(i1, j1, l, Blocks.GRAVEL.getDefaultState());
                        } else {
                            chunk.setBlockState(i1, j1, l, iblockstate1);
                        }
                    } else if (j > 0) {
                        --j;
                        chunk.setBlockState(i1, j1, l, iblockstate1);
                    }
                }
            }
        }
    }
}