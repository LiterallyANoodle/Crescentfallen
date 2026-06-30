package fracture.mod.world.gen;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import fracture.mod.init.BlockInit; 

public class AlphaRoseGenerator extends WorldGenerator {

    private final int minPerPatch;
    private final int maxPerPatch;

    public AlphaRoseGenerator(int minPerPatch, int maxPerPatch) {
        this.minPerPatch = Math.max(1, minPerPatch);
        this.maxPerPatch = Math.max(this.minPerPatch, maxPerPatch);
    }

    public AlphaRoseGenerator() {
        this(2, 9); 
    }

    @Override
    public boolean generate(World world, Random rand, BlockPos pos) {
        if (world.isRemote) return false;

        final int count = this.minPerPatch + rand.nextInt(this.maxPerPatch - this.minPerPatch + 1);
        boolean placedAny = false;

        for (int i = 0; i < count; i++) {
            int dx = rand.nextInt(8) - rand.nextInt(8); 
            int dz = rand.nextInt(8) - rand.nextInt(8);
            int x = pos.getX() + dx;
            int z = pos.getZ() + dz;

            int surfaceY = world.getHeight(x, z); 
            
            for (int y = surfaceY; y > 0; y--) {
                BlockPos spawn = new BlockPos(x, y, z);
                BlockPos below = spawn.down();

                if (!world.isAirBlock(spawn)) continue;

                IBlockState belowState = world.getBlockState(below);
                boolean soilOk = belowState.getBlock() == Blocks.GRASS ||
                                 belowState.getBlock() == Blocks.DIRT ||
                                 belowState.getBlock() == Blocks.MYCELIUM;

                if (soilOk) {
                    int light = world.getLight(spawn);
                    boolean darkEnough = light <= 15; 

                    if (darkEnough) {
                        world.setBlockState(spawn, BlockInit.ALPHA_ROSE.getDefaultState(), 2);
                        placedAny = true;
                    }
                    
                    break; 
                }
            }
        }

        return placedAny;
    }
}