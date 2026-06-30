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

public class BiomeCallistoOilShale extends Biome {

    private IBlockState liquidSaltState;
	
    public BiomeCallistoOilShale() {
        super(new BiomeProperties("Callisto Oil Shale")
            .setBaseHeight(0.1F)
            .setHeightVariation(0.05F)
            .setRainDisabled()
            .setTemperature(2.0F));
        this.spawnableMonsterList.clear();
        this.spawnableCreatureList.clear();
        this.spawnableWaterCreatureList.clear();
        this.spawnableCaveCreatureList.clear();
        
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(micdoodle8.mods.galacticraft.core.entities.EntityEvolvedEnderman.class,100,1,2));


        Block stone = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "callisto"));
        this.topBlock = (stone != null) ? stone.getStateFromMeta(6) : Blocks.STONE.getDefaultState();
        this.fillerBlock = this.topBlock;
        
        Block liquidSalt = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "salt"));
        if (liquidSalt != null) {
            this.liquidSaltState = liquidSalt.getDefaultState();
        }
    }

    @Override
    public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal) {
        int seaLevel = 63;
        int localX = x & 15;
        int localZ = z & 15;
        int depthCounter = -1;
        int noise = (int)(noiseVal / 3.0D + 3.0D + rand.nextDouble() * 0.25D);

        // Custom terrain generation pass
        for (int y = 255; y >= 0; --y) {
            if (y <= rand.nextInt(5)) {
                chunkPrimerIn.setBlockState(localX, y, localZ, Blocks.BEDROCK.getDefaultState());
            } else {
                IBlockState current = chunkPrimerIn.getBlockState(localX, y, localZ);
                if (current.getMaterial() == Material.AIR) {
                    depthCounter = -1;
                } else if (current.getBlock() == Blocks.STONE || (current.getBlock().getRegistryName() != null && current.getBlock().getRegistryName().getNamespace().equals("extraplanets"))) {
                    if (depthCounter == -1) {
                        depthCounter = noise;
                        IBlockState state = (y >= seaLevel - 1) ? this.topBlock : this.fillerBlock;
                        chunkPrimerIn.setBlockState(localX, y, localZ, (noise <= 0) ? Blocks.AIR.getDefaultState() : state);
                    } else if (depthCounter > 0) {
                        depthCounter--;
                        chunkPrimerIn.setBlockState(localX, y, localZ, this.fillerBlock);
                    }
                }
            }
        }

        // Liquid deletion pass
        if (this.liquidSaltState != null) {
            for (int y = 255; y >= 0; --y) {
                IBlockState current = chunkPrimerIn.getBlockState(localX, y, localZ);
                if (current.getBlock() == this.liquidSaltState.getBlock()) {
                    chunkPrimerIn.setBlockState(localX, y, localZ, Blocks.AIR.getDefaultState());
                }
            }
        }
    }
}