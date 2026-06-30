package fracture.mod.world.epchanges.callisto;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CfCallistoPyramidGen {
	
	//Note: make dynamic in the future
    private static final int CALLISTO_DIM_ID = -1505;
    private IBlockState ceresBrickState;

    @SubscribeEvent
    public void onChunkPopulate(PopulateChunkEvent.Post event) {
        World world = event.getWorld();
        if (world.isRemote || world.provider.getDimension() != CALLISTO_DIM_ID) return;

        Random random = event.getRand();
        if (random.nextInt(1000) != 0) return;

        if (ceresBrickState == null) initBlocks();

        int chunkX = event.getChunkX();
        int chunkZ = event.getChunkZ();
        int x = chunkX * 16 + 8;
        int z = chunkZ * 16 + 8;
        
        BlockPos centerPos = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
        int baseSize = 64; 
        
        generatePyramid(world, centerPos, baseSize);
    }

    private void generatePyramid(World world, BlockPos center, int size) {
        for (int i = 0; i < size; i++) {
            int layerWidth = size - i;
            int radius = layerWidth / 2; 
            int y = center.getY() + i;

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = new BlockPos(center.getX() + dx, y, center.getZ() + dz);
                    if (pos.getY() < 255) {
                        world.setBlockState(pos, ceresBrickState, 2);
                    }
                }
            }
        }
    }

    private void initBlocks() {
        net.minecraft.block.Block ceres = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("extraplanets", "ceres"));
        if (ceres != null) {
            ceresBrickState = ceres.getStateFromMeta(8);
        } else {
            ceresBrickState = Blocks.BRICK_BLOCK.getDefaultState();
        }
    }
}