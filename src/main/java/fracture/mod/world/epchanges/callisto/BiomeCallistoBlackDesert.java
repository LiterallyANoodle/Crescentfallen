package fracture.mod.world.epchanges.callisto;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class BiomeCallistoBlackDesert extends Biome {

    private NoiseGeneratorPerlin hoodooNoise;
    private IBlockState liquidSaltState;

    public BiomeCallistoBlackDesert() {
        super(new BiomeProperties("Callisto Black Desert")
            .setBaseHeight(0.2F)
            .setHeightVariation(0.1F)
            .setRainDisabled());
        this.spawnableMonsterList.clear();
        this.spawnableCreatureList.clear();
        this.spawnableWaterCreatureList.clear();
        this.spawnableCaveCreatureList.clear();
        
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(micdoodle8.mods.galacticraft.core.entities.EntityEvolvedEnderman.class,100,1,2));
        
        Block stone = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "callisto"));
        this.topBlock = (stone != null) ? stone.getStateFromMeta(6) : Blocks.HARDENED_CLAY.getDefaultState();
        this.fillerBlock = (stone != null) ? stone.getStateFromMeta(7) : Blocks.OBSIDIAN.getDefaultState();
        
        Block liquidSalt = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "salt"));
        if (liquidSalt != null) {
            this.liquidSaltState = liquidSalt.getDefaultState();
        }
    }

    @Override
    public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal) {
        if (this.hoodooNoise == null) {
            this.hoodooNoise = new NoiseGeneratorPerlin(new Random(worldIn.getSeed()), 4);
        }

        int localX = x & 15;
        int localZ = z & 15;

        double d0 = this.hoodooNoise.getValue((double)x * 0.12D, (double)z * 0.12D);
        int spikeHeight = 0;
        
        if (d0 > 0.80D && rand.nextInt(16) == 0) { 
            if (isCenterOfDesert(worldIn, x, z)) {
                spikeHeight = 10 + rand.nextInt(15); 
            }
        }

        int actualTop = -1;
        for (int y = 255; y >= 0; --y) {
            IBlockState current = chunkPrimerIn.getBlockState(localX, y, localZ);
            if (current.getMaterial() != Material.AIR && current.getBlock() != Blocks.BEDROCK) {
                actualTop = y;
                break;
            }
        }

        if (actualTop > 0 && spikeHeight > 0) {
            int topLimit = Math.min(255, actualTop + spikeHeight); 
            for (int y = actualTop + 1; y <= topLimit; y++) {
                int band = y % 4;
                chunkPrimerIn.setBlockState(localX, y, localZ, (band == 0 || band == 1) ? this.topBlock : this.fillerBlock);
            }
        }

        for (int y = 255; y >= 0; --y) {
            IBlockState current = chunkPrimerIn.getBlockState(localX, y, localZ);
            
            if (this.liquidSaltState != null && current.getBlock() == this.liquidSaltState.getBlock()) {
                chunkPrimerIn.setBlockState(localX, y, localZ, Blocks.AIR.getDefaultState());
                continue;
            }

            if (y <= 5) {
                chunkPrimerIn.setBlockState(localX, y, localZ, Blocks.BEDROCK.getDefaultState());
            } else if (current.getMaterial() != Material.AIR && current.getBlock() != Blocks.BEDROCK) {
                int band = y % 4; 
                chunkPrimerIn.setBlockState(localX, y, localZ, (band == 0 || band == 1) ? this.topBlock : this.fillerBlock);
            }
        }
    }

    private boolean isCenterOfDesert(World world, int x, int z) {
        int checkDist = 24; 
        return world.getBiomeProvider().getBiome(new BlockPos(x + checkDist, 0, z)) == this &&
               world.getBiomeProvider().getBiome(new BlockPos(x - checkDist, 0, z)) == this &&
               world.getBiomeProvider().getBiome(new BlockPos(x, 0, z + checkDist)) == this &&
               world.getBiomeProvider().getBiome(new BlockPos(x, 0, z - checkDist)) == this;
    }
}