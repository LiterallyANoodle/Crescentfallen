package fracture.mod.world.epchanges;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class CfGanymedeChunkgen implements IChunkGenerator {

    private final World world;
    private final Random rand;
    
    private final IBlockState AIR = Blocks.AIR.getDefaultState();
    private final IBlockState STONE = Blocks.STONE.getDefaultState();
    
    private IBlockState GANYMEDE_STONE;   // Meta 2 
    private IBlockState GANYMEDE_SURFACE; // Meta 0 
    private IBlockState ASH_ROCK;
    private IBlockState NICKEL_BLOCK;     // Meta 13 

    private NoiseGeneratorOctaves minLimitPerlinNoise;
    private NoiseGeneratorOctaves maxLimitPerlinNoise;
    private NoiseGeneratorOctaves mainPerlinNoise;
    private NoiseGeneratorPerlin spikeNoise;
    private NoiseGeneratorPerlin spikeWarpNoise;
    private NoiseGeneratorPerlin ashVeinNoise;
    private NoiseGeneratorPerlin depthNoise; 
    
    // Generates standard caves through other generation layers
    private MapGenBase caveGenerator = new MapGenCaves(); 
    
    private double[] buffer;

    public CfGanymedeChunkgen(World world, long seed) {
        this.world = world;
        this.rand = new Random(seed);
        
        Block gStone = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "ganymede"));
        this.GANYMEDE_STONE = (gStone != null) ? gStone.getStateFromMeta(2) : Blocks.STONE.getDefaultState();
        this.GANYMEDE_SURFACE = (gStone != null) ? gStone.getStateFromMeta(0) : Blocks.COBBLESTONE.getDefaultState();
        
        Block ash = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "ash_rock"));
        this.ASH_ROCK = (ash != null) ? ash.getDefaultState() : Blocks.OBSIDIAN.getDefaultState();

        Block jupiter = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "jupiter"));
        this.NICKEL_BLOCK = (jupiter != null) ? jupiter.getStateFromMeta(13) : Blocks.IRON_BLOCK.getDefaultState();

        // Initialize Noise
        this.minLimitPerlinNoise = new NoiseGeneratorOctaves(this.rand, 16);
        this.maxLimitPerlinNoise = new NoiseGeneratorOctaves(this.rand, 16);
        this.mainPerlinNoise = new NoiseGeneratorOctaves(this.rand, 8);
        this.spikeNoise = new NoiseGeneratorPerlin(this.rand, 4);
        this.spikeWarpNoise = new NoiseGeneratorPerlin(this.rand, 5); 
        this.ashVeinNoise = new NoiseGeneratorPerlin(this.rand, 4);
        this.depthNoise = new NoiseGeneratorPerlin(this.rand, 4); 
    }

    @Override
    public Chunk generateChunk(int x, int z) {
        this.rand.setSeed((long) x * 341873128712L + (long) z * 132897987541L);
        ChunkPrimer primer = new ChunkPrimer();
        
        // Create shape
        this.setBlocksInChunk(x, z, primer);

        // Generate caves
        this.caveGenerator.generate(this.world, x, z, primer);

        // Decorate surface
        this.replaceBiomeBlocksCustom(x, z, primer);

        Chunk chunk = new Chunk(this.world, primer, x, z);
        chunk.generateSkylightMap();
        return chunk;
    }

    public void setBlocksInChunk(int x, int z, ChunkPrimer primer) {
        this.buffer = this.getHeights(this.buffer, x * 4, 0, z * 4, 5, 33, 5);

        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                for (int k = 0; k < 32; ++k) {
                    double d1 = this.buffer[((i + 0) * 5 + j + 0) * 33 + k + 0];
                    double d2 = this.buffer[((i + 0) * 5 + j + 1) * 33 + k + 0];
                    double d3 = this.buffer[((i + 1) * 5 + j + 0) * 33 + k + 0];
                    double d4 = this.buffer[((i + 1) * 5 + j + 1) * 33 + k + 0];
                    double d5 = this.buffer[((i + 0) * 5 + j + 0) * 33 + k + 1];
                    double d6 = this.buffer[((i + 0) * 5 + j + 1) * 33 + k + 1];
                    double d7 = this.buffer[((i + 1) * 5 + j + 0) * 33 + k + 1];
                    double d8 = this.buffer[((i + 1) * 5 + j + 1) * 33 + k + 1];

                    double stepX1 = (d3 - d1) / 4.0D;
                    double stepX2 = (d4 - d2) / 4.0D;
                    double stepX3 = (d7 - d5) / 4.0D;
                    double stepX4 = (d8 - d6) / 4.0D;
                    
                    double val1 = d1;
                    double val2 = d2;
                    double val3 = d5;
                    double val4 = d6;

                    for (int l = 0; l < 4; ++l) { 
                        double stepZ1 = (val2 - val1) / 4.0D;
                        double stepZ2 = (val4 - val3) / 4.0D;
                        double valZ1 = val1;
                        double valZ2 = val3;

                        for (int m = 0; m < 4; ++m) { 
                            double stepY = (valZ2 - valZ1) / 8.0D;
                            double currentDensity = valZ1;

                            for (int n = 0; n < 8; ++n) {
                                int realX = l + i * 4;
                                int realY = n + k * 8;
                                int realZ = m + j * 4;
                                
                                if (currentDensity > 0.0D) {
                                    primer.setBlockState(realX, realY, realZ, STONE);
                                }
                                currentDensity += stepY;
                            }
                            valZ1 += stepZ1;
                            valZ2 += stepZ2;
                        }
                        val1 += stepX1;
                        val2 += stepX2;
                        val3 += stepX3;
                        val4 += stepX4;
                    }
                }
            }
        }
    }

    private double[] getHeights(double[] noiseArray, int xOffset, int yOffset, int zOffset, int xSize, int ySize, int zSize) {
        if (noiseArray == null) noiseArray = new double[xSize * ySize * zSize];

        double coordinateScale = 400.0D; // Increased from 300
        double heightScale = 300.0D;      
        
        double[] mainNoiseRegion = this.mainPerlinNoise.generateNoiseOctaves(null, xOffset, yOffset, zOffset, xSize, ySize, zSize, coordinateScale / 80.0D, heightScale / 160.0D, coordinateScale / 80.0D);
        double[] minLimitRegion = this.minLimitPerlinNoise.generateNoiseOctaves(null, xOffset, yOffset, zOffset, xSize, ySize, zSize, coordinateScale, heightScale, coordinateScale);
        double[] maxLimitRegion = this.maxLimitPerlinNoise.generateNoiseOctaves(null, xOffset, yOffset, zOffset, xSize, ySize, zSize, coordinateScale, heightScale, coordinateScale);

        int index = 0;

        for (int i = 0; i < xSize; ++i) {
            for (int j = 0; j < zSize; ++j) {
                
                double realX = (xOffset + i) * 1.5;
                double realZ = (zOffset + j) * 1.5;
                double warp = this.spikeWarpNoise.getValue(realX * 0.05, realZ * 0.05) * 4.0;
                double spikeVal = this.spikeNoise.getValue(realX * 0.02 + warp, realZ * 0.02 + warp);
                
                boolean isSpike = spikeVal > 0.62;
                
                for (int k = 0; k < ySize; ++k) {
                    double density;
                    double min = minLimitRegion[index] / 512.0D;
                    double max = maxLimitRegion[index] / 512.0D;
                    double main = (mainNoiseRegion[index] / 10.0D + 1.0D) / 2.0D;
                    
                    if (main < 0.0D) density = min;
                    else if (main > 1.0D) density = max;
                    else density = min + (max - min) * main;

                    double yReal = (double) k * 8.0D;
                    
                    if (yReal < 2) {
                        density += 20.0D; 
                    }

                    double surfaceHeight = 85.0D;
                    if (isSpike) {
                        double spikeHeight = (spikeVal - 0.62) * 320.0; 
                        surfaceHeight += spikeHeight;
                    }

                    double distFromSurface = surfaceHeight - yReal;
                    density += distFromSurface * 0.25D;

                    // Density Caves (may not be working)
                    if (yReal < 60 && yReal > 5) {
                         density -= 4.0D; 
                    }
                    
                    // Crust
                    if (yReal > 160 && yReal < 190) {
                        double crustThickness = 12.0D;
                        double distFromCrustCenter = Math.abs(yReal - 175.0D);
                        double holeFactor = (spikeVal > 0.60) ? -60.0D : 15.0D; 
                        
                        if (distFromCrustCenter < crustThickness) {
                            density += holeFactor;
                        }
                    }

                    if (yReal > 230) {
                        density = -1000.0D;
                    } else if (yReal > 200) {
                        density -= (yReal - 200) * 3.5D;
                    }

                    noiseArray[index] = density;
                    index++;
                }
            }
        }
        return noiseArray;
    }

    public void replaceBiomeBlocksCustom(int x, int z, ChunkPrimer primer) {
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                int realX = x * 16 + i;
                int realZ = z * 16 + j;
                
                double ashVal = this.ashVeinNoise.getValue(realX * 0.05, realZ * 0.05);
                
                double depthJitter = this.depthNoise.getValue(realX * 0.1, realZ * 0.1);
                int bedrockThreshold = 29 + (int)(depthJitter * 4.0);
                
                Random localRand = new Random((long)realX * 3122L + (long)realZ * 871L);

                for (int y = 0; y < 256; ++y) {
                    IBlockState current = primer.getBlockState(i, y, j);
                    
                    if (current.getBlock() == Blocks.STONE) {
                    	
                        // Deep stone
                        if (y < bedrockThreshold) {
                            primer.setBlockState(i, y, j, GANYMEDE_STONE);
                        } else {
                            // Ash Veins check
                            double veinCheck = ashVal + Math.sin(y * 0.1); 
                            if (veinCheck > 0.6) {
                                primer.setBlockState(i, y, j, ASH_ROCK);
                            } else {
                                primer.setBlockState(i, y, j, GANYMEDE_SURFACE);
                            }
                        }

                        // Nickel blocks
                        // Rare spawn near bedrock and below y level 15
                        if (y < 15 && localRand.nextFloat() < 0.00015F) {
                            int clusterSize = 3 + localRand.nextInt(5); 
                            for (int c = 0; c < clusterSize; c++) {
                                int ox = localRand.nextInt(3) - 1;
                                int oy = localRand.nextInt(3) - 1;
                                int oz = localRand.nextInt(3) - 1;
                                if (i+ox >= 0 && i+ox < 16 && j+oz >= 0 && j+oz < 16 && y+oy >= 0) {
                                    primer.setBlockState(i+ox, y+oy, j+oz, NICKEL_BLOCK);
                                }
                            }
                        }

                    } else if (current.getBlock() == Blocks.AIR || current.getBlock() == Blocks.BEDROCK) {
                        if (current.getBlock() == Blocks.BEDROCK) {
                            primer.setBlockState(i, y, j, (y < bedrockThreshold) ? GANYMEDE_STONE : GANYMEDE_SURFACE);
                        }

                        // Floating blocks in the sky void
                        if (y > 100 && y < 220) {
                            if (localRand.nextFloat() < 0.0005F) { 
                                primer.setBlockState(i, y, j, GANYMEDE_SURFACE);
                                if (localRand.nextBoolean()) {
                                    int yOff = localRand.nextBoolean() ? 1 : -1;
                                    primer.setBlockState(i, Math.min(255, Math.max(0, y + yOff)), j, GANYMEDE_SURFACE);
                                }
                            }
                        }
                    }
                }
                primer.setBlockState(i, 0, j, GANYMEDE_STONE);
            }
        }
    }

    @Override public void populate(int x, int z) {}
    @Override public boolean generateStructures(Chunk chunkIn, int x, int z) { return false; }
    @Override public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return this.world.getBiome(pos).getSpawnableList(creatureType);
    }
    @Nullable @Override public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) { return null; }
    @Override public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) { return false; }
    @Override public void recreateStructures(Chunk chunkIn, int x, int z) {}
}