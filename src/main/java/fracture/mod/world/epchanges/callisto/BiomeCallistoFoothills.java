package fracture.mod.world.epchanges.callisto;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class BiomeCallistoFoothills extends Biome {

    private IBlockState liquidSaltState;

    public BiomeCallistoFoothills() {
        super(new BiomeProperties("Callisto Foothills")
            .setBaseHeight(0.6F)
            .setHeightVariation(0.2F)
            .setRainDisabled());
        this.spawnableMonsterList.clear();
        this.spawnableCreatureList.clear();
        this.spawnableWaterCreatureList.clear();
        this.spawnableCaveCreatureList.clear();
        
        Block stone = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "callisto"));
        this.topBlock = (stone != null) ? stone.getDefaultState() : Blocks.STONE.getDefaultState();
        this.fillerBlock = this.topBlock;

        Block liquidSalt = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "salt"));
        if (liquidSalt != null) {
            this.liquidSaltState = liquidSalt.getDefaultState();
        }
    }

    @Override
    public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal) {
        super.genTerrainBlocks(worldIn, rand, chunkPrimerIn, x, z, noiseVal);

        if (this.liquidSaltState != null) {
            int localX = x & 15;
            int localZ = z & 15;
            for (int y = 255; y >= 0; --y) {
                IBlockState current = chunkPrimerIn.getBlockState(localX, y, localZ);
                if (current.getBlock() == this.liquidSaltState.getBlock()) {
                    chunkPrimerIn.setBlockState(localX, y, localZ, Blocks.AIR.getDefaultState());
                }
            }
        }
    }
}