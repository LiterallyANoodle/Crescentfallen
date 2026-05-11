package fracture.mod.world.epchanges.callisto;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CfCallistoPyramidGen implements IWorldGenerator {
	
	// This is an easter egg that *should* spawn on callisto. It generates an infdev brick pyramid 
	// out of pluto stone bricks. (WIP)

    private static final int CALLISTO_DIM_ID = -1505;
    private IBlockState ceresBrickState;

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != CALLISTO_DIM_ID) return;

        // 1 in 10,000 chunks. (Approx. 1 every 16,000 blocks traveled)
        if (random.nextInt(10000) != 0) return;

        if (ceresBrickState == null) initBlocks();

        // Center coordinates
        int x = chunkX * 16 + 8;
        int z = chunkZ * 16 + 8;
        
        // Find surface height at the center
        BlockPos centerPos = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
        
        int baseSize = 64; 
        
        generatePyramid(world, centerPos, baseSize);
        System.out.println("Generated Legendary Ceres Pyramid at: " + centerPos);
    }

    private void generatePyramid(World world, BlockPos center, int size) {
        for (int i = 0; i < size; i++) {
            // Calculate the width of the current layer
            // Layer 0 (Bottom) = Size 64
            // Layer 63 (Top) = Size 1
            int layerWidth = size - i;
            int radius = layerWidth / 2; // Radius from center
            int y = center.getY() + i;

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = new BlockPos(center.getX() + dx, y, center.getZ() + dz);
                    world.setBlockState(pos, ceresBrickState, 2);
                }
            }
        }
    }

    private void initBlocks() {
        net.minecraft.block.Block ceres = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "ceres"));
        if (ceres != null) {
            ceresBrickState = ceres.getStateFromMeta(8);
        } else {
            // Fallback
            ceresBrickState = Blocks.BRICK_BLOCK.getDefaultState();
        }
    }
}