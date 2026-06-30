package fracture.mod.planets.moons.kona.biome.gen;

import java.util.Random;

import fracture.mod.init.BlockInit;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenKonaRustSpikes extends WorldGenerator {

    private final IBlockState rustState = BlockInit.KONA_RUST.getDefaultState();

    @Override
    public boolean generate(World world, Random rand, BlockPos position) {
        int height = 15 + rand.nextInt(25);   
        int depth = 5 + rand.nextInt(15);    
        float maxRadius = 2.5F + rand.nextFloat() * 2.0F; 

        float tiltX = (rand.nextFloat() - 0.5F) * 0.8F; 
        float tiltZ = (rand.nextFloat() - 0.5F) * 0.8F;

        for (int y = -depth; y <= height; y++) {
            
            int currentX = position.getX() + (int)(y * tiltX);
            int currentZ = position.getZ() + (int)(y * tiltZ);
            int currentY = position.getY() + y;

            float progress = (float)(y + depth) / (height + depth);
            float currentRadius = maxRadius * (1.0F - progress);

            int radiusInt = (int) Math.ceil(currentRadius);

            for (int dx = -radiusInt; dx <= radiusInt; dx++) {
                for (int dz = -radiusInt; dz <= radiusInt; dz++) {
                    if (dx * dx + dz * dz <= currentRadius * currentRadius) {
                        BlockPos targetPos = new BlockPos(currentX + dx, currentY, currentZ + dz);

                        IBlockState targetState = world.getBlockState(targetPos);
                        if (targetState.getMaterial() == Material.AIR ||
                            targetState.getBlock() == BlockInit.TRASH_PILE ||
                            targetState.getBlock() == BlockInit.SURFACE_KONA ||
                            targetState.getBlock() == BlockInit.STONE_KONA) {
                            
                            this.setBlockAndNotifyAdequately(world, targetPos, rustState);
                        }
                    }
                }
            }
        }
        return true;
    }
}