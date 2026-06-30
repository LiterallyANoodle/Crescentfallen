package fracture.mod.world.epchanges.callisto;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CfCallistoStructureGen {

    private static final int CALLISTO_DIM_ID = -1505;
    private IBlockState ashRockState;
    private boolean initialized = false;

    @SubscribeEvent
    public void onChunkPopulate(PopulateChunkEvent.Post event) {
        World world = event.getWorld();
        if (world.isRemote || world.provider.getDimension() != CALLISTO_DIM_ID) return;
        if (!initialized) initBlocks();

        Random random = event.getRand();
        int chunkX = event.getChunkX();
        int chunkZ = event.getChunkZ();

        // 1 in 18 chunks
        if (random.nextInt(18) != 0) return;

        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);
        BlockPos surfacePos = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
        Biome biome = world.getBiome(surfacePos);

        if (biome == CFEPBiomeInit.CALLISTO_OIL_SHALE) {
            if (surfacePos.getY() > 50 && world.getBlockState(surfacePos.down()).isFullBlock()) {
                generateSpikeAsh(world, surfacePos, random);
            }
        }
    }

    private void generateSpikeAsh(World world, BlockPos pos, Random rand) {
        int height = 15 + rand.nextInt(20); 
        float maxRadius = 2.5f + rand.nextFloat() * 2.0f; 

        for (int y = 0; y < height; y++) {
            float progress = (float) y / height;
            float currentRadius = maxRadius * (1.0f - (float)Math.pow(progress, 0.6));

            int iRadius = (int) Math.ceil(currentRadius);
            for (int dx = -iRadius; dx <= iRadius; dx++) {
                for (int dz = -iRadius; dz <= iRadius; dz++) {
                    double noise = rand.nextDouble() * 0.4;
                    if (dx * dx + dz * dz <= (currentRadius * currentRadius) + noise) {
                        BlockPos target = pos.add(dx, y, dz);
                        if (target.getY() < 255) {
                            world.setBlockState(target, ashRockState, 2);
                        }
                    }
                }
            }
        }
    }

    private void initBlocks() {
        net.minecraft.block.Block stone = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "callisto"));
        if (stone != null) {
            ashRockState = stone.getStateFromMeta(2); 
        } else {
            ashRockState = Blocks.OBSIDIAN.getDefaultState();
        }
        initialized = true;
    }
}