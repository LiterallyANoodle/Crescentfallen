package fracture.mod.planets.moons.kona.biome.gen;

import java.util.Random;

import fracture.mod.init.BlockInit;
import fracture.mod.planets.moons.kona.biome.KonaBiomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

public class MapGenTrashDunes {
    private NoiseGeneratorPerlin baseNoise;
    private NoiseGeneratorPerlin detailNoise;
    private NoiseGeneratorPerlin shapeNoise;

    public MapGenTrashDunes(long seed) {
        Random rand = new Random(seed);
        this.baseNoise = new NoiseGeneratorPerlin(rand, 4);
        this.detailNoise = new NoiseGeneratorPerlin(rand, 2);
        this.shapeNoise = new NoiseGeneratorPerlin(rand, 1);
    }

    private double getTrashWeight(World world, int x, int z) {
        int radius = 32; 
        int step = 8;
        int trashCount = 0;
        int totalCount = 0;
        
        for (int dx = -radius; dx <= radius; dx += step) {
            for (int dz = -radius; dz <= radius; dz += step) {
                Biome b = world.getBiomeProvider().getBiome(new BlockPos(x + dx, 0, z + dz));
                if (b == KonaBiomes.BiomeKonaTrashHeap) {
                    trashCount++;
                }
                totalCount++;
            }
        }
        return (double) trashCount / totalCount;
    }

    public void generate(World worldIn, int chunkX, int chunkZ, ChunkPrimer primer) {
        int cx = chunkX * 16;
        int cz = chunkZ * 16;

        double w00 = getTrashWeight(worldIn, cx, cz);
        double w10 = getTrashWeight(worldIn, cx + 16, cz);
        double w01 = getTrashWeight(worldIn, cx, cz + 16);
        double w11 = getTrashWeight(worldIn, cx + 16, cz + 16);

        if (w00 == 0 && w10 == 0 && w01 == 0 && w11 == 0) {
            return;
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int realX = cx + x;
                int realZ = cz + z;

                double lerpX0 = w00 + (w10 - w00) * (x / 16.0D);
                double lerpX1 = w01 + (w11 - w01) * (x / 16.0D);
                double trashWeight = lerpX0 + (lerpX1 - lerpX0) * (z / 16.0D);

                if (trashWeight > 0.01D) {
                    
                    double macroNoise = (this.baseNoise.getValue(realX * 0.006D, realZ * 0.006D) + 1.0D) / 2.0D;
                    
                    int baseHeight = 0;
                    int peakHeight = 0;

                    if (macroNoise > 0.35D) {
                        double baseCurve = MathHelper.clamp((macroNoise - 0.35D) / 0.25D, 0.0D, 1.0D);
                        baseCurve = baseCurve * baseCurve * (3.0D - 2.0D * baseCurve); 
                        baseHeight = (int) (baseCurve * 14.0D); 

                        if (macroNoise > 0.45D) {
                            double rawRidge = this.detailNoise.getValue(realX * 0.015D, realZ * 0.015D);
                            double ridge = 1.0D - Math.abs(rawRidge); 
                            double shape = (this.shapeNoise.getValue(realX * 0.01D, realZ * 0.01D) + 1.0D) / 2.0D;
                            
                            double combinedPeak = Math.pow((ridge * 0.7D) + (shape * 0.3D), 2.0D);
                            double peakTaper = MathHelper.clamp((macroNoise - 0.45D) / 0.15D, 0.0D, 1.0D);
                            
                            peakHeight = (int) (combinedPeak * 28.0D * peakTaper); 
                        }
                    }

                    int totalDuneHeight = baseHeight + peakHeight;

                    if (totalDuneHeight > 0) {
                        
                        int actualHeight = (int) (totalDuneHeight * trashWeight);

                        if (actualHeight == 0 && trashWeight > 0.02D) {
                            actualHeight = 1;
                        }

                        if (actualHeight > 0) {
                            int surfaceY = 255;
                            while (surfaceY > 0 && primer.getBlockState(x, surfaceY, z).getBlock() == Blocks.AIR) {
                                surfaceY--;
                            }

                            for (int h = 1; h <= actualHeight; h++) {
                                if (surfaceY + h < 255) { 
                                    primer.setBlockState(x, surfaceY + h, z, BlockInit.TRASH_PILE.getDefaultState());
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}