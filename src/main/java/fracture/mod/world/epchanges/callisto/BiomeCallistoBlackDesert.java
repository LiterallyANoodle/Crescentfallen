package fracture.mod.world.epchanges.callisto;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class BiomeCallistoBlackDesert extends Biome {

    public BiomeCallistoBlackDesert() {
        super(new BiomeProperties("Callisto Black Desert")
            .setBaseHeight(0.2F)
            .setHeightVariation(0.1F)
            .setRainDisabled());

        Block stone = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "callisto"));
        this.topBlock = (stone != null) ? stone.getStateFromMeta(6) : Blocks.HARDENED_CLAY.getDefaultState();
        this.fillerBlock = (stone != null) ? stone.getStateFromMeta(7) : Blocks.OBSIDIAN.getDefaultState();
    }

    @Override
    public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal) {
        int localX = x & 15;
        int localZ = z & 15;

        for (int y = 255; y >= 0; --y) {
            if (y <= 5) {
                chunkPrimerIn.setBlockState(localX, y, localZ, Blocks.BEDROCK.getDefaultState());
            } else {
                IBlockState current = chunkPrimerIn.getBlockState(localX, y, localZ);
                if (current.getMaterial() != Material.AIR && current.getBlock() != Blocks.BEDROCK) {
                    int band = y % 4; 
                    chunkPrimerIn.setBlockState(localX, y, localZ, (band == 0 || band == 1) ? this.topBlock : this.fillerBlock);
                }
            }
        }
    }
}